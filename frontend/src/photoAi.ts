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
    return { blob: await cropTransparentPadding(blob), backgroundRemoved: true };
  } catch (error) {
    console.warn('Background removal failed; using original photo.', error);
    return { blob: photo, backgroundRemoved: false };
  }
}

async function cropTransparentPadding(photo: Blob): Promise<Blob> {
  const image = await blobToImage(photo);
  try {
    const sourceCanvas = document.createElement('canvas');
    sourceCanvas.width = image.naturalWidth;
    sourceCanvas.height = image.naturalHeight;
    const sourceContext = sourceCanvas.getContext('2d', { willReadFrequently: true });
    if (!sourceContext) return photo;

    sourceContext.drawImage(image, 0, 0);
    const imageData = sourceContext.getImageData(0, 0, sourceCanvas.width, sourceCanvas.height);
    const bounds = findOpaqueBounds(imageData);
    if (!bounds) return photo;

    const padding = Math.round(Math.max(bounds.width, bounds.height) * 0.04);
    const x = Math.max(0, bounds.x - padding);
    const y = Math.max(0, bounds.y - padding);
    const width = Math.min(sourceCanvas.width - x, bounds.width + padding * 2);
    const height = Math.min(sourceCanvas.height - y, bounds.height + padding * 2);

    const outputCanvas = document.createElement('canvas');
    outputCanvas.width = width;
    outputCanvas.height = height;
    outputCanvas.getContext('2d')?.drawImage(sourceCanvas, x, y, width, height, 0, 0, width, height);
    return await canvasToBlob(outputCanvas);
  } finally {
    URL.revokeObjectURL(image.src);
  }
}

function findOpaqueBounds(imageData: ImageData) {
  const { data, width, height } = imageData;
  let minX = width;
  let minY = height;
  let maxX = -1;
  let maxY = -1;

  for (let y = 0; y < height; y += 1) {
    for (let x = 0; x < width; x += 1) {
      const alpha = data[(y * width + x) * 4 + 3];
      if (alpha > 12) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
      }
    }
  }

  if (maxX < minX || maxY < minY) {
    return null;
  }

  return {
    x: minX,
    y: minY,
    width: maxX - minX + 1,
    height: maxY - minY + 1,
  };
}

function canvasToBlob(canvas: HTMLCanvasElement): Promise<Blob> {
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) {
        resolve(blob);
      } else {
        reject(new Error('Could not crop transparent image padding'));
      }
    }, 'image/png');
  });
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
