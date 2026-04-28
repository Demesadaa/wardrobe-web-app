import type { ClothingCategory, ClothingPiece, Outfit, Profile, User } from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = options.body instanceof FormData
    ? options.headers
    : { 'Content-Type': 'application/json', ...(options.headers as Record<string, string> | undefined) };

  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers,
    ...options,
  });

  if (!response.ok) {
    const message = await response.text();
    if (response.status === 401 || response.status === 403) {
      throw new Error('Your session expired. Please log out and log in again.');
    }
    throw new Error(message || `Request failed with ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export function imageUrl(path: string) {
  return path.startsWith('http') ? path : `${API_BASE}${path}`;
}

export const api = {
  me: () => request<User>('/api/me'),
  register: (body: { username: string; email: string; password: string }) =>
    request<User>('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login: (body: { username: string; password: string }) =>
    request<User>('/api/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  logout: () => request<void>('/api/auth/logout', { method: 'POST' }),
  profile: () => request<Profile>('/api/profile'),
  updateProfile: (body: { displayName: string; bio: string }) =>
    request<Profile>('/api/profile', { method: 'PUT', body: JSON.stringify(body) }),
  pieces: () => request<ClothingPiece[]>('/api/pieces'),
  uploadPiece: (photo: Blob, category: ClothingCategory) => {
    const form = new FormData();
    form.append('category', category);
    form.append('photo', photo, `wardrobe-${category.toLowerCase()}.jpg`);
    return request<ClothingPiece>('/api/pieces', { method: 'POST', body: form });
  },
  deletePiece: (id: number) => request<void>(`/api/pieces/${id}`, { method: 'DELETE' }),
  randomOutfit: () => request<Outfit>('/api/outfit/random'),
};
