export type ClothingCategory = 'FOOTWEAR' | 'PANTS' | 'TORSO' | 'HEADWEAR';

export type User = {
  id: number;
  username: string;
  email: string;
  displayName: string;
};

export type Profile = {
  id: number;
  username: string;
  email: string;
  displayName: string;
  bio: string | null;
};

export type ClothingPiece = {
  id: number;
  category: ClothingCategory;
  imageUrl: string;
  originalFilename: string;
  createdAt: string;
};

export type Outfit = Record<ClothingCategory, ClothingPiece | null>;
