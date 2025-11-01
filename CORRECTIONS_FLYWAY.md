# Corrections Flyway et Guide de Test

## ✅ Corrections Effectuées

### 1. Problèmes Flyway Résolus

#### V1__Create_base_tables.sql
**Problème** : Les noms de colonnes dans les migrations SQL ne correspondaient pas aux entités Java.

**Corrections apportées** :

**Table `shipping_addresses`** :
- ❌ `address_line1` → ✅ `street_address`
- ❌ `address_line2` → ✅ `street_address_line2`
- ❌ `state` → ✅ `state_province`
- ❌ `phone` → ✅ `phone_number`
- ➕ Ajout de `address_name` (colonne manquante)
- ❌ Suppression de `first_name`, `last_name`, `company` (non utilisés dans l'entité)

**Table `password_reset_tokens`** :
- ❌ `expires_at` → ✅ `expiry_date`

#### V3__Add_missing_columns.sql
**Problème** : Le fichier V3 essayait d'ajouter des colonnes qui existaient déjà dans V1.

**Corrections apportées** :
- Suppression de toutes les colonnes en doublon
- Conservation uniquement des colonnes réellement manquantes pour la table `payments`:
  - `stripe_payment_intent_id`
  - `webhook_received_at`
  - `failure_reason`
  - `description`

## 🚀 Instructions de Démarrage

### Prérequis
1. **Docker doit être démarré** pour exécuter PostgreSQL
2. Java 17 installé
3. Maven installé (ou utiliser le wrapper `./mvnw`)

### Étapes de Démarrage

#### 1. Démarrer Docker Desktop
Assurez-vous que Docker Desktop est en cours d'exécution sur votre Mac.

#### 2. Démarrer PostgreSQL
```bash
cd /Users/daoudatraore/Downloads/malistore-backend
bash scripts/start-postgresql.sh
```

#### 3. Vérifier que PostgreSQL est en cours d'exécution
```bash
docker ps
```
Vous devriez voir le conteneur `malistore-db` en cours d'exécution.

#### 4. Démarrer l'application Spring Boot
```bash
./mvnw spring-boot:run
```

L'application démarrera sur `http://localhost:8080`

#### 5. Accéder à la documentation Swagger
Ouvrez votre navigateur : `http://localhost:8080/swagger-ui.html`

## 🧪 Tests des Endpoints

### 1. Endpoints d'Authentification (Publics)

#### Créer un compte utilisateur
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

#### Se connecter
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!"
  }'
```

**Important** : Copiez le `token` retourné pour l'utiliser dans les requêtes suivantes.

#### Se connecter en tant qu'Admin (pour les tests)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@malistore.com",
    "password": "admin123"
  }'
```

### 2. Endpoints de Catégories

#### Lister toutes les catégories (Public)
```bash
curl -X GET http://localhost:8080/api/categories
```

#### Créer une nouvelle catégorie (ADMIN uniquement)
```bash
curl -X POST http://localhost:8080/api/admin/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Nouvelle Catégorie",
    "description": "Description de la catégorie",
    "imageUrl": "https://example.com/image.jpg",
    "active": true,
    "sortOrder": 10
  }'
```

#### Modifier une catégorie (ADMIN uniquement)
```bash
curl -X PUT http://localhost:8080/api/admin/categories/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Catégorie Modifiée",
    "description": "Nouvelle description",
    "active": true
  }'
```

#### Supprimer une catégorie (ADMIN uniquement)
```bash
curl -X DELETE http://localhost:8080/api/admin/categories/6 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 3. Endpoints de Produits

#### Lister tous les produits (Public)
```bash
curl -X GET http://localhost:8080/api/products
```

#### Obtenir un produit par ID (Public)
```bash
curl -X GET http://localhost:8080/api/products/1
```

#### Rechercher des produits par catégorie (Public)
```bash
curl -X GET http://localhost:8080/api/products?categoryId=1
```

#### Créer un nouveau produit (ADMIN uniquement)
```bash
curl -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Nouveau Produit",
    "description": "Description du produit",
    "price": 99.99,
    "stock": 50,
    "categoryId": 1,
    "imageUrl": "https://example.com/product.jpg",
    "active": true
  }'
```

#### Modifier un produit (ADMIN uniquement)
```bash
curl -X PUT http://localhost:8080/api/admin/products/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Produit Modifié",
    "price": 149.99,
    "stock": 75
  }'
```

#### Supprimer un produit (ADMIN uniquement)
```bash
curl -X DELETE http://localhost:8080/api/admin/products/6 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

#### Upload d'une image de produit (ADMIN uniquement)
```bash
curl -X POST http://localhost:8080/api/admin/products/1/images \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "altText=Description de l'image" \
  -F "isPrimary=true"
```

### 4. Endpoints du Panier

#### Voir mon panier (Authentifié)
```bash
curl -X GET http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Ajouter un produit au panier (Authentifié)
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

#### Modifier la quantité d'un article (Authentifié)
```bash
curl -X PUT http://localhost:8080/api/cart/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "quantity": 3
  }'
```

#### Supprimer un article du panier (Authentifié)
```bash
curl -X DELETE http://localhost:8080/api/cart/items/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Vider le panier (Authentifié)
```bash
curl -X DELETE http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. Endpoints des Adresses de Livraison

#### Créer une adresse de livraison (Authentifié)
```bash
curl -X POST http://localhost:8080/api/shipping-addresses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "addressName": "Maison",
    "streetAddress": "123 Rue de la Paix",
    "streetAddressLine2": "Appartement 4B",
    "city": "Paris",
    "stateProvince": "Île-de-France",
    "postalCode": "75001",
    "country": "France",
    "phoneNumber": "+33123456789",
    "isDefault": true
  }'
```

#### Lister mes adresses (Authentifié)
```bash
curl -X GET http://localhost:8080/api/shipping-addresses \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6. Endpoints des Commandes

#### Créer une commande (Authentifié)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "shippingAddressId": 1
  }'
```

#### Lister mes commandes (Authentifié)
```bash
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Obtenir les détails d'une commande (Authentifié)
```bash
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Lister toutes les commandes (ADMIN uniquement)
```bash
curl -X GET http://localhost:8080/api/admin/orders \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

#### Modifier le statut d'une commande (ADMIN uniquement)
```bash
curl -X PUT http://localhost:8080/api/admin/orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "status": "SHIPPED"
  }'
```

### 7. Endpoints de Paiement (Stripe)

#### Créer une session de paiement Stripe (Authentifié)
```bash
curl -X POST http://localhost:8080/api/payments/create-session \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "orderId": 1,
    "successUrl": "http://localhost:3000/payment/success",
    "cancelUrl": "http://localhost:3000/payment/cancel"
  }'
```

## 📋 Vérifications Post-Migration

### Vérifier que les migrations Flyway ont réussi
```bash
# Se connecter à PostgreSQL
docker exec -it malistore-db psql -U malistore_user -d malistore_db

# Dans le shell PostgreSQL
\dt  -- Lister toutes les tables
\d shipping_addresses  -- Vérifier la structure de la table
\d password_reset_tokens
\d payments
\q  -- Quitter
```

### Vérifier les données par défaut
```bash
# Dans le shell PostgreSQL
SELECT * FROM categories;
SELECT * FROM products;
SELECT * FROM users;
```

## 🔍 Endpoints à vérifier dans Swagger UI

Une fois l'application démarrée, ouvrez `http://localhost:8080/swagger-ui.html` et vérifiez que tous ces contrôleurs sont présents :

1. **authentification-api-controller** : `/api/auth/*`
2. **category-controller** : `/api/categories/*`
3. **product-controller** : `/api/products/*`
4. **cart-controller** : `/api/cart/*`
5. **order-controller** : `/api/orders/*`
6. **payment-controller** : `/api/payments/*`
7. **shipping-address-controller** : `/api/shipping-addresses/*`
8. **admin-product-controller** : `/api/admin/products/*`
9. **admin-order-controller** : `/api/admin/orders/*`
10. **user-api-controller** : `/api/users/*`

## ⚠️ Notes Importantes

1. **Tokens JWT** : Les tokens expirent après 24 heures par défaut. Si vos requêtes retournent 401, reconnectez-vous.

2. **Roles** : 
   - USER : Peut accéder aux endpoints de base (produits, panier, commandes)
   - ADMIN : Peut accéder à tous les endpoints + gestion des produits et commandes

3. **Base de données** : Les données par défaut incluent :
   - 1 compte admin : `admin@malistore.com` / `admin123`
   - 5 catégories
   - 5 produits (un par catégorie)

4. **Upload d'images** : Les images sont stockées dans le dossier `uploads/images/`

## 🐛 En cas de problème

### L'application ne démarre pas
1. Vérifier que Docker est démarré
2. Vérifier que PostgreSQL est en cours d'exécution : `docker ps`
3. Vérifier les logs : `tail -f /Users/daoudatraore/Downloads/malistore-backend/app.log`

### Erreur de connexion à la base de données
```bash
# Redémarrer PostgreSQL
docker stop malistore-db
docker rm malistore-db
bash scripts/start-postgresql.sh
```

### Erreur Flyway
```bash
# Nettoyer complètement la base de données et recommencer
docker stop malistore-db
docker rm malistore-db
docker volume rm malistore_data
bash scripts/start-postgresql.sh
./mvnw spring-boot:run
```

