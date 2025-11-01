# Guide API - Upload d'Images MaliStore

## 🎯 **Fonctionnalités d'Upload Implémentées**

### ✅ **Upload de fichiers vers le backend**
- **Stockage local** : Images stockées dans `uploads/images/`
- **URLs publiques** : Accessibles via `http://localhost:8080/images/{filename}`
- **Validation** : Types de fichiers image uniquement
- **Taille limitée** : 10MB par fichier, 50MB par requête

### ✅ **Endpoints d'upload disponibles**
- Créer un produit avec images multiples
- Ajouter des images à un produit existant
- Upload d'une seule image avec options

## 📋 **Endpoints API d'Upload**

### **1. Créer un produit avec images multiples**

```http
POST /api/v1/products/upload
Content-Type: multipart/form-data

FormData:
- name: "iPhone 15 Pro Max"
- description: "Smartphone avec galerie d'images"
- price: 1299.99
- stock: 15
- categoryId: 1
- files: [File, File, File] (images)
- altTexts: ["Vue de face", "Vue arrière", "Vue de côté"] (optionnel)
```

**Réponse :**
```json
{
  "status": "success",
  "message": "Operation successful",
  "data": {
    "id": 12,
    "name": "iPhone 15 Pro Max",
    "description": "Smartphone avec galerie d'images",
    "price": 1299.99,
    "imageUrl": null,
    "images": [
      {
        "id": 5,
        "filename": "iphone-front.jpg",
        "imageUrl": "http://localhost:8080/images/uuid1.jpg",
        "altText": "Vue de face",
        "isPrimary": true,
        "sortOrder": 0
      },
      {
        "id": 6,
        "filename": "iphone-back.jpg",
        "imageUrl": "http://localhost:8080/images/uuid2.jpg",
        "altText": "Vue arrière",
        "isPrimary": false,
        "sortOrder": 1
      }
    ],
    "stock": 15,
    "active": true,
    "category": { ... },
    "createdAt": "2025-10-28T02:30:00.000Z",
    "updatedAt": "2025-10-28T02:30:00.000Z"
  }
}
```

### **2. Ajouter des images à un produit existant**

```http
POST /api/v1/products/upload/{productId}/images
Content-Type: multipart/form-data

FormData:
- files: [File, File] (images)
- altTexts: ["Nouvelle image 1", "Nouvelle image 2"] (optionnel)
```

### **3. Upload d'une seule image**

```http
POST /api/v1/products/{productId}/images/upload-single
Content-Type: multipart/form-data

FormData:
- file: File (image)
- isPrimary: true/false (optionnel, défaut: false)
- altText: "Description de l'image" (optionnel)
```

### **4. Upload multiple d'images à un produit existant**

```http
POST /api/v1/products/{productId}/images/upload
Content-Type: multipart/form-data

FormData:
- files: [File, File, File] (images)
```

## 🎨 **Exemples d'Implémentation Frontend**

### **React/TypeScript - Composant d'Upload**

```tsx
import React, { useState, useRef } from 'react';

interface ImageUploadProps {
  productId?: number;
  onUploadComplete: (images: ProductImage[]) => void;
}

const ImageUpload: React.FC<ImageUploadProps> = ({ productId, onUploadComplete }) => {
  const [files, setFiles] = useState<File[]>([]);
  const [altTexts, setAltTexts] = useState<string[]>([]);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(e.target.files || []);
    setFiles(selectedFiles);
    setAltTexts(selectedFiles.map(() => ''));
  };

  const handleAltTextChange = (index: number, value: string) => {
    const newAltTexts = [...altTexts];
    newAltTexts[index] = value;
    setAltTexts(newAltTexts);
  };

  const uploadImages = async () => {
    if (files.length === 0) return;

    setUploading(true);
    const formData = new FormData();
    
    files.forEach(file => {
      formData.append('files', file);
    });
    
    altTexts.forEach(altText => {
      if (altText.trim()) {
        formData.append('altTexts', altText);
      }
    });

    try {
      const endpoint = productId 
        ? `/api/v1/products/upload/${productId}/images`
        : '/api/v1/products/upload';
      
      const response = await fetch(endpoint, {
        method: 'POST',
        body: formData
      });

      const result = await response.json();
      
      if (response.ok) {
        onUploadComplete(result.data.images || [result.data]);
        setFiles([]);
        setAltTexts([]);
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
      } else {
        console.error('Erreur upload:', result.message);
      }
    } catch (error) {
      console.error('Erreur upload:', error);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="image-upload">
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="image/*"
        onChange={handleFileChange}
        className="file-input"
      />
      
      {files.length > 0 && (
        <div className="file-preview">
          <h4>Images sélectionnées ({files.length})</h4>
          {files.map((file, index) => (
            <div key={index} className="file-item">
              <img
                src={URL.createObjectURL(file)}
                alt={file.name}
                className="preview-image"
              />
              <div className="file-info">
                <p>{file.name}</p>
                <input
                  type="text"
                  placeholder="Texte alternatif (optionnel)"
                  value={altTexts[index]}
                  onChange={(e) => handleAltTextChange(index, e.target.value)}
                  className="alt-text-input"
                />
              </div>
            </div>
          ))}
          
          <button
            onClick={uploadImages}
            disabled={uploading}
            className="upload-button"
          >
            {uploading ? 'Upload en cours...' : 'Uploader les images'}
          </button>
        </div>
      )}
    </div>
  );
};

export default ImageUpload;
```

### **CSS pour l'Upload**

```css
.image-upload {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: border-color 0.3s;
}

.image-upload:hover {
  border-color: #007bff;
}

.file-input {
  margin-bottom: 20px;
}

.file-preview {
  margin-top: 20px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
}

.preview-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.file-info {
  flex: 1;
}

.file-info p {
  margin: 0 0 5px 0;
  font-weight: bold;
}

.alt-text-input {
  width: 100%;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.upload-button {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.upload-button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
```

### **JavaScript/Vanilla - Upload d'Images**

```javascript
class ImageUploader {
  constructor(baseUrl = 'http://localhost:8080') {
    this.baseUrl = baseUrl;
  }

  // Upload multiple d'images
  async uploadImages(productId, files, altTexts = []) {
    const formData = new FormData();
    
    files.forEach(file => {
      formData.append('files', file);
    });
    
    altTexts.forEach(altText => {
      if (altText && altText.trim()) {
        formData.append('altTexts', altText);
      }
    });

    try {
      const response = await fetch(`${this.baseUrl}/api/v1/products/upload/${productId}/images`, {
        method: 'POST',
        body: formData
      });

      const result = await response.json();
      
      if (response.ok) {
        return result.data;
      } else {
        throw new Error(result.message);
      }
    } catch (error) {
      console.error('Erreur upload:', error);
      throw error;
    }
  }

  // Upload d'une seule image
  async uploadSingleImage(productId, file, options = {}) {
    const formData = new FormData();
    formData.append('file', file);
    
    if (options.isPrimary !== undefined) {
      formData.append('isPrimary', options.isPrimary);
    }
    
    if (options.altText) {
      formData.append('altText', options.altText);
    }

    try {
      const response = await fetch(`${this.baseUrl}/api/v1/products/${productId}/images/upload-single`, {
        method: 'POST',
        body: formData
      });

      const result = await response.json();
      
      if (response.ok) {
        return result.data;
      } else {
        throw new Error(result.message);
      }
    } catch (error) {
      console.error('Erreur upload:', error);
      throw error;
    }
  }

  // Créer un produit avec images
  async createProductWithImages(productData, files, altTexts = []) {
    const formData = new FormData();
    
    // Ajouter les données du produit
    Object.keys(productData).forEach(key => {
      formData.append(key, productData[key]);
    });
    
    // Ajouter les images
    files.forEach(file => {
      formData.append('files', file);
    });
    
    altTexts.forEach(altText => {
      if (altText && altText.trim()) {
        formData.append('altTexts', altText);
      }
    });

    try {
      const response = await fetch(`${this.baseUrl}/api/v1/products/upload`, {
        method: 'POST',
        body: formData
      });

      const result = await response.json();
      
      if (response.ok) {
        return result.data;
      } else {
        throw new Error(result.message);
      }
    } catch (error) {
      console.error('Erreur création produit:', error);
      throw error;
    }
  }
}

// Utilisation
const uploader = new ImageUploader();

// Exemple d'utilisation
document.getElementById('uploadForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const files = Array.from(document.getElementById('images').files);
  const altTexts = Array.from(document.querySelectorAll('.alt-text')).map(input => input.value);
  
  try {
    const result = await uploader.uploadImages(1, files, altTexts);
    console.log('Images uploadées:', result);
  } catch (error) {
    console.error('Erreur:', error);
  }
});
```

## 📝 **Configuration Backend**

### **Limites de fichiers (application.yml)**
```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
      file-size-threshold: 2KB
```

### **Stockage des images**
- **Dossier** : `uploads/images/`
- **URLs publiques** : `http://localhost:8080/images/{filename}`
- **Noms de fichiers** : UUID générés automatiquement

## ⚠️ **Règles et Limitations**

1. **Types de fichiers** : Images uniquement (JPEG, PNG, GIF, WebP, etc.)
2. **Taille maximale** : 10MB par fichier
3. **Requête maximale** : 50MB total
4. **Validation** : Vérification du type MIME
5. **Sécurité** : Noms de fichiers sécurisés (UUID)

## 🔧 **Gestion des Erreurs**

```javascript
// Exemple de gestion d'erreurs
try {
  const result = await uploader.uploadImages(productId, files);
  // Succès
} catch (error) {
  if (error.message.includes('File too large')) {
    alert('Fichier trop volumineux (max 10MB)');
  } else if (error.message.includes('Invalid file type')) {
    alert('Type de fichier non supporté');
  } else {
    alert('Erreur lors de l\'upload: ' + error.message);
  }
}
```

## 🚀 **Test de l'API**

Un fichier de test HTML est disponible : `test-upload.html`

1. Ouvrez le fichier dans un navigateur
2. Sélectionnez des images
3. Testez les différents endpoints d'upload
4. Vérifiez les résultats dans la console

## 📁 **Structure des Fichiers Uploadés**

```
uploads/
└── images/
    ├── 550e8400-e29b-41d4-a716-446655440000.jpg
    ├── 6ba7b810-9dad-11d1-80b4-00c04fd430c8.png
    └── 6ba7b811-9dad-11d1-80b4-00c04fd430c8.gif
```

Votre frontend peut maintenant uploader des images directement vers le backend au lieu d'utiliser des URLs externes ! 🎉
