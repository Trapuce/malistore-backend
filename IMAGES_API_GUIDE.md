# Guide API - Gestion des Images de Produits

## Vue d'ensemble

L'API MaliStore supporte maintenant la gestion de plusieurs images par produit. Chaque produit peut avoir :
- Une image principale (primary)
- Plusieurs images secondaires
- Un ordre de tri personnalisé
- Des métadonnées (nom de fichier, texte alternatif)

## Endpoints disponibles

### 1. Créer un produit avec plusieurs images

**POST** `/api/v1/products`

```json
{
  "name": "iPhone 15 Pro Max",
  "description": "Smartphone Apple avec galerie d'images",
  "price": 1299.99,
  "stock": 15,
  "categoryId": 1,
  "images": [
    {
      "filename": "iphone15pro-front.jpg",
      "imageUrl": "https://example.com/iphone-front.jpg",
      "altText": "iPhone 15 Pro Max - Vue de face",
      "isPrimary": true,
      "sortOrder": 0
    },
    {
      "filename": "iphone15pro-back.jpg",
      "imageUrl": "https://example.com/iphone-back.jpg",
      "altText": "iPhone 15 Pro Max - Vue arrière",
      "isPrimary": false,
      "sortOrder": 1
    }
  ]
}
```

### 2. Récupérer un produit avec ses images

**GET** `/api/v1/products/{id}`

```json
{
  "id": 1,
  "name": "iPhone 15 Pro Max",
  "description": "Smartphone Apple avec galerie d'images",
  "price": 1299.99,
  "imageUrl": null, // Remplacé par le tableau images
  "images": [
    {
      "id": 1,
      "filename": "iphone15pro-front.jpg",
      "imageUrl": "https://example.com/iphone-front.jpg",
      "altText": "iPhone 15 Pro Max - Vue de face",
      "isPrimary": true,
      "sortOrder": 0
    },
    {
      "id": 2,
      "filename": "iphone15pro-back.jpg",
      "imageUrl": "https://example.com/iphone-back.jpg",
      "altText": "iPhone 15 Pro Max - Vue arrière",
      "isPrimary": false,
      "sortOrder": 1
    }
  ],
  "stock": 15,
  "active": true,
  "category": { ... },
  "createdAt": "2025-10-28T01:43:33.55645",
  "updatedAt": "2025-10-28T01:43:33.556464"
}
```

### 3. Gestion des images d'un produit

#### Récupérer toutes les images d'un produit
**GET** `/api/v1/products/{productId}/images`

#### Récupérer l'image principale
**GET** `/api/v1/products/{productId}/images/primary`

#### Ajouter des images à un produit existant
**POST** `/api/v1/products/{productId}/images`

```json
[
  {
    "filename": "new-image.jpg",
    "imageUrl": "https://example.com/new-image.jpg",
    "altText": "Nouvelle image du produit",
    "isPrimary": false,
    "sortOrder": 2
  }
]
```

#### Modifier une image
**PUT** `/api/v1/products/{productId}/images/{imageId}`

```json
{
  "filename": "updated-image.jpg",
  "imageUrl": "https://example.com/updated-image.jpg",
  "altText": "Image mise à jour",
  "isPrimary": true,
  "sortOrder": 0
}
```

#### Définir une image comme principale
**PUT** `/api/v1/products/{productId}/images/{imageId}/primary`

#### Supprimer une image
**DELETE** `/api/v1/products/{productId}/images/{imageId}`

#### Supprimer toutes les images d'un produit
**DELETE** `/api/v1/products/{productId}/images`

## Structure des données

### ProductImageDto
```typescript
interface ProductImageDto {
  id: number;
  filename: string;           // Nom original du fichier
  imageUrl: string;          // URL publique de l'image
  altText?: string;          // Texte alternatif pour l'accessibilité
  isPrimary: boolean;        // Image principale (une seule par produit)
  sortOrder: number;         // Ordre d'affichage (0 = premier)
}
```

### ProductResponse (mis à jour)
```typescript
interface ProductResponse {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;         // Rétrocompatibilité (peut être null)
  images: ProductImageDto[]; // Nouvelles images multiples
  stock: number;
  active: boolean;
  category: CategoryResponse;
  createdAt: string;
  updatedAt: string;
}
```

## Règles métier

1. **Image principale** : Un seul produit peut avoir une seule image marquée comme `isPrimary: true`
2. **Ordre de tri** : Les images sont triées par `sortOrder` (ascendant)
3. **Rétrocompatibilité** : Le champ `imageUrl` est conservé pour la compatibilité
4. **Validation** : 
   - `filename` : requis, max 255 caractères
   - `imageUrl` : requis, max 500 caractères
   - `altText` : optionnel, max 100 caractères

## Exemples d'utilisation Frontend

### React/TypeScript
```typescript
// Récupérer un produit avec ses images
const fetchProduct = async (productId: number) => {
  const response = await fetch(`/api/v1/products/${productId}`);
  const data = await response.json();
  return data.data;
};

// Afficher les images
const ProductGallery = ({ product }: { product: ProductResponse }) => {
  const primaryImage = product.images.find(img => img.isPrimary);
  const otherImages = product.images.filter(img => !img.isPrimary);
  
  return (
    <div className="product-gallery">
      {primaryImage && (
        <img 
          src={primaryImage.imageUrl} 
          alt={primaryImage.altText}
          className="primary-image"
        />
      )}
      <div className="thumbnail-gallery">
        {otherImages.map(image => (
          <img 
            key={image.id}
            src={image.imageUrl} 
            alt={image.altText}
            className="thumbnail"
          />
        ))}
      </div>
    </div>
  );
};
```

### JavaScript/Vanilla
```javascript
// Récupérer les images d'un produit
async function getProductImages(productId) {
  const response = await fetch(`/api/v1/products/${productId}/images`);
  const data = await response.json();
  return data.data;
}

// Afficher la galerie d'images
function displayProductImages(images) {
  const gallery = document.getElementById('product-gallery');
  
  images.forEach(image => {
    const img = document.createElement('img');
    img.src = image.imageUrl;
    img.alt = image.altText;
    img.className = image.isPrimary ? 'primary-image' : 'thumbnail';
    gallery.appendChild(img);
  });
}
```

## Stockage des images

Actuellement, l'API stocke les URLs d'images externes. Pour le stockage local :

1. **Configuration** : Les images sont stockées dans `uploads/images/`
2. **URLs publiques** : Accessibles via `http://localhost:8080/images/{filename}`
3. **Upload de fichiers** : Endpoint `/api/v1/products/{productId}/images/upload` (à implémenter)

## Migration depuis l'ancien système

Les produits existants avec `imageUrl` continuent de fonctionner. Pour migrer :

1. Récupérer les produits existants
2. Créer des `ProductImage` à partir des `imageUrl` existants
3. Marquer la première image comme `isPrimary: true`

## Notes importantes

- Tous les endpoints d'images nécessitent une authentification
- Les images sont triées automatiquement par `sortOrder`
- Une seule image peut être marquée comme principale à la fois
- La suppression d'un produit supprime automatiquement toutes ses images



