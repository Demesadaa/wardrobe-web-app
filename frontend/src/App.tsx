import { ChangeEvent, FormEvent, useEffect, useMemo, useRef, useState } from 'react';
import { api, imageUrl } from './api';
import { processClothingPhoto } from './photoAi';
import type { ClothingCategory, ClothingPiece, Profile, User } from './types';

const categories: ClothingCategory[] = ['HEADWEAR', 'TORSO', 'PANTS', 'FOOTWEAR'];
const categoryLabels: Record<ClothingCategory, string> = {
  HEADWEAR: 'Headwear',
  TORSO: 'Torso',
  PANTS: 'Pants',
  FOOTWEAR: 'Footwear',
};

type Screen = 'lobby' | 'capture' | 'profile';

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [screen, setScreen] = useState<Screen>('lobby');

  useEffect(() => {
    api
      .me()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <main className="center-card">Loading wardrobe...</main>;
  }

  if (!user) {
    return <AuthPage onAuthed={setUser} />;
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Wardrobe Lab</p>
          <h1>Hello, {user.displayName}</h1>
        </div>
        <nav>
          <button className={screen === 'lobby' ? 'active' : ''} onClick={() => setScreen('lobby')}>
            Lobby
          </button>
          <button className={screen === 'capture' ? 'active' : ''} onClick={() => setScreen('capture')}>
            Add Piece
          </button>
          <button className={screen === 'profile' ? 'active' : ''} onClick={() => setScreen('profile')}>
            Profile
          </button>
          <button
            onClick={() =>
              api.logout().finally(() => {
                setUser(null);
                setScreen('lobby');
              })
            }
          >
            Logout
          </button>
        </nav>
      </header>

      {screen === 'lobby' && <LobbyPage />}
      {screen === 'capture' && <CapturePage />}
      {screen === 'profile' && <ProfilePage onProfileSaved={(profile) => setUser({ ...user, displayName: profile.displayName })} />}
    </main>
  );
}

function AuthPage({ onAuthed }: { onAuthed: (user: User) => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      if (mode === 'register') {
        await api.register({ username, email, password });
      }
      onAuthed(await api.login({ username, password }));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not sign in');
    }
  }

  return (
    <main className="auth-layout">
      <section className="hero-card">
        <p className="eyebrow">Your closet, remixed</p>
        <h1>Build outfits from photos of your own clothes.</h1>
        <p>Capture a piece, tag it, and let the lobby character try random combinations.</p>
      </section>
      <form className="panel auth-panel" onSubmit={submit}>
        <h2>{mode === 'login' ? 'Log in' : 'Create account'}</h2>
        <label>
          Username
          <input value={username} onChange={(event) => setUsername(event.target.value)} required minLength={3} />
        </label>
        {mode === 'register' && (
          <label>
            Email
            <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          </label>
        )}
        <label>
          Password
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required minLength={8} />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="primary" type="submit">
          {mode === 'login' ? 'Log in' : 'Register'}
        </button>
        <button className="link-button" type="button" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
          {mode === 'login' ? 'Need an account?' : 'Already have an account?'}
        </button>
      </form>
    </main>
  );
}

function ProfilePage({ onProfileSaved }: { onProfileSaved: (profile: Profile) => void }) {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [displayName, setDisplayName] = useState('');
  const [bio, setBio] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    api.profile().then((loaded) => {
      setProfile(loaded);
      setDisplayName(loaded.displayName);
      setBio(loaded.bio ?? '');
    });
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const updated = await api.updateProfile({ displayName, bio });
    setProfile(updated);
    onProfileSaved(updated);
    setMessage('Profile saved.');
  }

  return (
    <section className="panel">
      <h2>Profile</h2>
      {profile && <p className="muted">@{profile.username} · {profile.email}</p>}
      <form className="stack-form" onSubmit={submit}>
        <label>
          Display name
          <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} required />
        </label>
        <label>
          Bio
          <textarea value={bio} onChange={(event) => setBio(event.target.value)} rows={4} maxLength={280} />
        </label>
        <button className="primary" type="submit">Save profile</button>
        {message && <p className="success">{message}</p>}
      </form>
    </section>
  );
}

function CapturePage() {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [photo, setPhoto] = useState<Blob | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [category, setCategory] = useState<ClothingCategory>('TORSO');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [processing, setProcessing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [aiSummary, setAiSummary] = useState('');

  useEffect(() => {
    return () => {
      stream?.getTracks().forEach((track) => track.stop());
    };
  }, [stream]);

  async function startCamera() {
    setError('');
    try {
      const cameraStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false });
      setStream(cameraStream);
      if (videoRef.current) {
        videoRef.current.srcObject = cameraStream;
        await videoRef.current.play();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not access camera');
    }
  }

  async function capturePhoto() {
    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas) return;
    setError('');
    setMessage('Capturing photo...');
    setProcessing(true);
    setAiSummary('');
    if (!video.videoWidth || !video.videoHeight) {
      setProcessing(false);
      setMessage('');
      setError('Camera is still starting. Wait a moment, then take the photo again.');
      return;
    }
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    canvas.getContext('2d')?.drawImage(video, 0, 0);

    try {
      const capturedPhoto = await canvasToBlob(canvas, 'image/jpeg', 0.92);
      await processAndPreviewPhoto(capturedPhoto);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not process photo');
      setMessage('');
    } finally {
      setProcessing(false);
    }
  }

  async function handleFileUpload(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      setError('Please choose an image file.');
      return;
    }

    setError('');
    setMessage(`Processing ${file.name}...`);
    setProcessing(true);
    setAiSummary('');

    try {
      await processAndPreviewPhoto(file);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not process uploaded photo');
      setMessage('');
    } finally {
      setProcessing(false);
    }
  }

  async function processAndPreviewPhoto(sourcePhoto: Blob) {
    setMessage('Recognizing clothing and removing background...');
    const processed = await processClothingPhoto(sourcePhoto);
    setPhoto(processed.blob);
    setCategory(processed.category);
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(URL.createObjectURL(processed.blob));
    setAiSummary(summaryForProcessedPhoto(processed.backgroundRemoved, processed.category, processed.confidence, processed.labels));
    setMessage('Photo processed. Review the tag, then save it.');
  }

  async function uploadPhoto() {
    if (!photo) return;
    setUploading(true);
    setError('');
    try {
      await api.uploadPiece(photo, category);
      setMessage('Piece saved to your wardrobe.');
      setPhoto(null);
      setAiSummary('');
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      setPreviewUrl(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save piece');
    } finally {
      setUploading(false);
    }
  }

  return (
    <section className="panel capture-grid">
      <div>
        <h2>Add a clothing piece</h2>
        <p className="muted">
          Take a photo or upload an existing image. The app will try to remove the background and recognize the clothing
          category automatically.
        </p>
        <label className="upload-dropzone">
          <span>Upload a pre-taken clothing photo</span>
          <small>Use this for pieces you already photographed, wishlist items, or clothes you do not have nearby.</small>
          <input type="file" accept="image/*" onChange={handleFileUpload} disabled={processing || uploading} />
        </label>
        <div className="section-divider">or use the camera</div>
        <div className="camera-box">
          <video ref={videoRef} autoPlay playsInline muted />
          {!stream && <button className="primary floating" onClick={startCamera}>Enable camera</button>}
        </div>
        <div className="button-row">
          <button onClick={capturePhoto} disabled={!stream || processing}>
            {processing ? 'Processing...' : 'Take photo'}
          </button>
          <button className="primary" onClick={uploadPhoto} disabled={!photo || processing || uploading}>
            {uploading ? 'Saving...' : 'Save piece'}
          </button>
        </div>
        {error && <p className="error">{error}</p>}
      </div>
      <aside className="preview-panel">
        <h3>Tag this piece</h3>
        <div className="category-grid">
          {categories.map((item) => (
            <button key={item} className={category === item ? 'active' : ''} onClick={() => setCategory(item)}>
              {categoryLabels[item]}
            </button>
          ))}
        </div>
        {previewUrl ? <img className="photo-preview" src={previewUrl} alt="Captured clothing" /> : <p className="empty-state">No photo yet.</p>}
        {aiSummary && <p className="ai-summary">{aiSummary}</p>}
        {message && <p className="success">{message}</p>}
      </aside>
      <canvas ref={canvasRef} hidden />
    </section>
  );
}

function canvasToBlob(canvas: HTMLCanvasElement, type: string, quality: number) {
  return new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) {
        resolve(blob);
      } else {
        reject(new Error('Could not capture photo from camera'));
      }
    }, type, quality);
  });
}

function summaryForProcessedPhoto(
  backgroundRemoved: boolean,
  category: ClothingCategory,
  confidence: number,
  labels: string[],
) {
  const removalText = backgroundRemoved ? 'Background removed' : 'Background removal failed, original photo kept';
  const confidenceText = confidence > 0 ? `${Math.round(confidence * 100)}% confidence` : 'low confidence';
  const labelText = labels.length ? ` Labels: ${labels.slice(0, 3).join(', ')}.` : '';
  return `${removalText}. Guessed ${categoryLabels[category]} with ${confidenceText}.${labelText}`;
}

function LobbyPage() {
  const [pieces, setPieces] = useState<ClothingPiece[]>([]);
  const [selected, setSelected] = useState<Record<ClothingCategory, number>>({
    HEADWEAR: 0,
    TORSO: 0,
    PANTS: 0,
    FOOTWEAR: 0,
  });

  useEffect(() => {
    api.pieces().then((loaded) => {
      setPieces(loaded);
      setSelected(randomSelection(loaded));
    });
  }, []);

  const grouped = useMemo(() => groupPieces(pieces), [pieces]);
  const outfit = useMemo(() => {
    return Object.fromEntries(categories.map((category) => [category, grouped[category][selected[category]] ?? null])) as Record<
      ClothingCategory,
      ClothingPiece | null
    >;
  }, [grouped, selected]);

  function reroll() {
    setSelected(randomSelection(pieces));
  }

  function cycle(category: ClothingCategory, direction: -1 | 1) {
    const count = grouped[category].length;
    if (count === 0) return;
    setSelected((current) => ({
      ...current,
      [category]: (current[category] + direction + count) % count,
    }));
  }

  return (
    <section className="lobby-grid">
      <div className="panel character-panel">
        <div className="character-stage">
          <div className="stickman">
            <div className="head" />
            <div className="body" />
            <div className="arms" />
            <div className="legs" />
          </div>
          {categories.map((category) =>
            outfit[category] ? (
              <img
                key={category}
                className={`wearable wearable-${category.toLowerCase()}`}
                src={imageUrl(outfit[category]!.imageUrl)}
                alt={categoryLabels[category]}
              />
            ) : null,
          )}
        </div>
        <button className="primary" onClick={reroll}>Random reroll</button>
      </div>

      <div className="panel">
        <h2>Outfit controls</h2>
        {categories.map((category) => (
          <div className="category-control" key={category}>
            <button onClick={() => cycle(category, -1)} disabled={grouped[category].length === 0}>Left</button>
            <div>
              <strong>{categoryLabels[category]}</strong>
              <p className="muted">
                {outfit[category] ? outfit[category]?.originalFilename : 'No piece saved yet'}
              </p>
            </div>
            <button onClick={() => cycle(category, 1)} disabled={grouped[category].length === 0}>Right</button>
          </div>
        ))}
      </div>
    </section>
  );
}

function groupPieces(pieces: ClothingPiece[]) {
  return categories.reduce<Record<ClothingCategory, ClothingPiece[]>>(
    (acc, category) => {
      acc[category] = pieces.filter((piece) => piece.category === category);
      return acc;
    },
    { HEADWEAR: [], TORSO: [], PANTS: [], FOOTWEAR: [] },
  );
}

function randomSelection(pieces: ClothingPiece[]) {
  const grouped = groupPieces(pieces);
  return categories.reduce<Record<ClothingCategory, number>>(
    (acc, category) => {
      acc[category] = grouped[category].length ? Math.floor(Math.random() * grouped[category].length) : 0;
      return acc;
    },
    { HEADWEAR: 0, TORSO: 0, PANTS: 0, FOOTWEAR: 0 },
  );
}
