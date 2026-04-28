import type { ClothingCategory } from './types';

type MobileNetPrediction = {
  className: string;
  probability: number;
};

export type ProcessedPhoto = {
  blob: Blob;
  category: ClothingCategory;
  confidence: number;
  labels: string[];
  backgroundRemoved: boolean;
};

let modelPromise: Promise<{ classify: (image: HTMLImageElement) => Promise<MobileNetPrediction[]> }> | null = null;

export async function processClothingPhoto(photo: Blob): Promise<ProcessedPhoto> {
  const cutout = await removePhotoBackground(photo);
  const predictions = await classifyPhoto(cutout.blob);
  const category = categoryFromPredictions(predictions);

  return {
    blob: cutout.blob,
    category: category.category,
    confidence: category.confidence,
    labels: predictions.map((prediction) => prediction.className),
    backgroundRemoved: cutout.backgroundRemoved,
  };
}

async function removePhotoBackground(photo: Blob) {
  try {
    const { removeBackground } = await import('@imgly/background-removal');
    const blob = await removeBackground(photo, {
      output: { format: 'image/png' },
    });
    return { blob, backgroundRemoved: true };
  } catch (error) {
    console.warn('Background removal failed; using original photo.', error);
    return { blob: photo, backgroundRemoved: false };
  }
}

async function classifyPhoto(photo: Blob): Promise<MobileNetPrediction[]> {
  try {
    await import('@tensorflow/tfjs');
    if (!modelPromise) {
      modelPromise = import('@tensorflow-models/mobilenet').then((mobilenet) => mobilenet.load());
    }
    const model = await modelPromise;
    const image = await blobToImage(photo);
    try {
      return await model.classify(image);
    } finally {
      URL.revokeObjectURL(image.src);
    }
  } catch (error) {
    console.warn('Clothing recognition failed; defaulting to torso.', error);
    return [];
  }
}

function blobToImage(blob: Blob): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(blob);
    const image = new Image();
    image.onload = () => resolve(image);
    image.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error('Could not load captured photo for recognition'));
    };
    image.src = url;
  });
}

function categoryFromPredictions(predictions: MobileNetPrediction[]) {
  const scores: Record<ClothingCategory, number> = {
    HEADWEAR: 0,
    TORSO: 0,
    PANTS: 0,
    FOOTWEAR: 0,
  };

  for (const prediction of predictions) {
    const label = prediction.className.toLowerCase();
    addScore(scores, prediction.probability, label, 'FOOTWEAR', ['shoe', 'sneaker', 'sandal', 'boot', 'loafer']);
    addScore(scores, prediction.probability, label, 'PANTS', ['jean', 'trouser', 'pants', 'shorts', 'legging']);
    addScore(scores, prediction.probability, label, 'TORSO', ['shirt', 'jersey', 'sweater', 'cardigan', 'coat', 'jacket', 'suit', 'vest', 'dress']);
    addScore(scores, prediction.probability, label, 'HEADWEAR', ['hat', 'cap', 'bonnet', 'helmet', 'sombrero', 'cowboy']);
  }

  const [category, confidence] = Object.entries(scores).reduce<[ClothingCategory, number]>(
    (best, [candidate, score]) => (score > best[1] ? [candidate as ClothingCategory, score] : best),
    ['TORSO', 0],
  );

  return {
    category,
    confidence,
  };
}

function addScore(
  scores: Record<ClothingCategory, number>,
  probability: number,
  label: string,
  category: ClothingCategory,
  keywords: string[],
) {
  if (keywords.some((keyword) => label.includes(keyword))) {
    scores[category] += probability;
  }
}
