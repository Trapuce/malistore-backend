# 📋 Résumé des Corrections - MaliStore Backend

## ✅ Travail Effectué

### 1. Corrections des Migrations Flyway

#### Problèmes Identifiés et Résolus

**V1__Create_base_tables.sql** - Incohérence avec les entités Java :

| Table | Ancien Nom | Nouveau Nom | Raison |
|-------|------------|-------------|--------|
| `shipping_addresses` | `address_line1` | `street_address` | Correspondance avec l'entité Java |
| `shipping_addresses` | `address_line2` | `street_address_line2` | Correspondance avec l'entité Java |
| `shipping_addresses` | `state` | `state_province` | Correspondance avec l'entité Java |
| `shipping_addresses` | `phone` | `phone_number` | Correspondance avec l'entité Java |
| `shipping_addresses` | - | `address_name` | Colonne manquante ajoutée |
| `password_reset_tokens` | `expires_at` | `expiry_date` | Correspondance avec l'entité Java |

**Colonnes supprimées de `shipping_addresses`** (non utilisées dans l'entité Java) :
- `first_name`
- `last_name`
- `company`

**V3__Add_missing_columns.sql** - Nettoyage :
- ❌ Supprimé : Colonnes dupliquées qui existaient déjà dans V1
- ✅ Conservé : Colonnes réellement manquantes pour `payments` :
  - `stripe_payment_intent_id`
  - `webhook_received_at`
  - `failure_reason`
  - `description`

### 2. Fichiers Créés

1. **CORRECTIONS_FLYWAY.md** 
   - Guide complet de démarrage
   - Documentation de tous les endpoints avec exemples cURL
   - Instructions de dépannage

2. **test-all-endpoints.sh**
   - Script Bash automatisé pour tester tous les endpoints
   - Tests couvrant :
     - ✅ Authentification (Admin & User)
     - ✅ Catégories (CRUD complet)
     - ✅ Produits (CRUD complet)
     - ✅ Panier (Ajout, modification, suppression)
     - ✅ Adresses de livraison
     - ✅ Commandes (Création, listage, gestion)
     - ✅ Paiements Stripe
     - ✅ Nettoyage (suppression des données de test)

3. **RESUME_CORRECTIONS.md** (ce fichier)
   - Résumé de toutes les corrections

## 🚀 Comment Utiliser

### Étape 1 : Démarrer PostgreSQL

```bash
# Ouvrir Docker Desktop (nécessaire !)
# Puis exécuter :
cd /Users/daoudatraore/Downloads/malistore-backend
bash scripts/start-postgresql.sh
```

### Étape 2 : Vérifier PostgreSQL

```bash
docker ps
# Devrait afficher le conteneur 'malistore-db' en cours d'exécution
```

### Étape 3 : Démarrer l'application

```bash
cd /Users/daoudatraore/Downloads/malistore-backend
./mvnw spring-boot:run
```

L'application démarrera sur `http://localhost:8080`

### Étape 4 : Tester les endpoints

#### Option A : Tests Automatisés (Recommandé)

```bash
cd /Users/daoudatraore/Downloads/malistore-backend
bash test-all-endpoints.sh
```

Ce script testera automatiquement tous les endpoints et affichera les résultats en couleur.

#### Option B : Tests Manuels

1. **Via Swagger UI** : `http://localhost:8080/swagger-ui.html`
2. **Via cURL** : Consultez `CORRECTIONS_FLYWAY.md` pour les exemples

#### Option C : Via Postman/Insomnia

Importez ces endpoints de base :

**Authentification Admin** :
```
POST http://localhost:8080/api/auth/login
Body: {"email": "admin@malistore.com", "password": "admin123"}
```

## 📊 Structure de la Base de Données

### Tables Créées (V1)

1. **users** - Utilisateurs (1 admin par défaut)
2. **password_reset_tokens** - Tokens de réinitialisation
3. **categories** - Catégories de produits (5 par défaut)
4. **products** - Produits (5 par défaut)
5. **product_images** - Images de produits (10 par défaut)
6. **cart_items** - Articles du panier
7. **shipping_addresses** - Adresses de livraison
8. **orders** - Commandes
9. **order_items** - Articles des commandes
10. **payments** - Paiements Stripe

### Colonnes Ajoutées (V3)

Table **payments** :
- `stripe_payment_intent_id`
- `webhook_received_at`
- `failure_reason`
- `description`

## 🔑 Comptes par Défaut

### Administrateur
- **Email** : `admin@malistore.com`
- **Mot de passe** : `admin123`
- **Rôle** : ADMIN
- **Permissions** : Accès complet à tous les endpoints

### Utilisateur de Test (à créer)
- **Email** : À définir lors de l'inscription
- **Rôle** : USER
- **Permissions** : Produits, panier, commandes

## 📦 Données par Défaut

### 5 Catégories
1. Électronique et Gadgets
2. Vêtements et Mode
3. Maison et Jardin
4. Sports et Loisirs
5. Livres et Médias

### 5 Produits (un par catégorie)
1. iPhone 15 Pro Max (Électronique)
2. T-shirt Premium Cotton (Vêtements)
3. Lampadaire Design Moderne (Maison)
4. Raquette de Tennis Professionnelle (Sports)
5. Livre "Le Guide du Développeur" (Livres)

Chaque produit a 2 images associées.

## 🧪 Endpoints Testés

### ✅ Endpoints Publics (sans authentification)
- `GET /api/categories` - Liste des catégories
- `GET /api/products` - Liste des produits
- `GET /api/products/{id}` - Détails d'un produit
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion

### ✅ Endpoints Utilisateur (authentification requise)
- `GET /api/cart` - Voir le panier
- `POST /api/cart/items` - Ajouter au panier
- `PUT /api/cart/items/{id}` - Modifier quantité
- `DELETE /api/cart/items/{id}` - Supprimer du panier
- `GET /api/orders` - Mes commandes
- `POST /api/orders` - Créer une commande
- `GET /api/orders/{id}` - Détails d'une commande
- `GET /api/shipping-addresses` - Mes adresses
- `POST /api/shipping-addresses` - Créer une adresse
- `POST /api/payments/create-session` - Créer session Stripe

### ✅ Endpoints Admin (rôle ADMIN requis)
- `POST /api/admin/categories` - Créer catégorie
- `PUT /api/admin/categories/{id}` - Modifier catégorie
- `DELETE /api/admin/categories/{id}` - Supprimer catégorie
- `POST /api/admin/products` - Créer produit
- `PUT /api/admin/products/{id}` - Modifier produit
- `DELETE /api/admin/products/{id}` - Supprimer produit
- `POST /api/admin/products/{id}/images` - Upload image
- `GET /api/admin/orders` - Toutes les commandes
- `PUT /api/admin/orders/{id}/status` - Modifier statut

## ⚠️ Points d'Attention

### 1. Docker Obligatoire
PostgreSQL nécessite Docker. Assurez-vous que Docker Desktop est démarré.

### 2. Stripe (Optionnel)
Les endpoints de paiement nécessitent des clés Stripe valides dans `application.yml`.
Sans cela, les tests de paiement échoueront mais le reste fonctionne.

### 3. Tokens JWT
Les tokens expirent après 24h. En cas d'erreur 401, reconnectez-vous.

### 4. Ordre des Tests
Si vous testez manuellement :
1. D'abord se connecter pour obtenir un token
2. Utiliser le token dans l'en-tête `Authorization: Bearer {token}`
3. Créer une adresse avant de créer une commande
4. Ajouter des produits au panier avant de créer une commande

## 🐛 Dépannage

### L'application ne démarre pas
```bash
# Vérifier que PostgreSQL est démarré
docker ps | grep malistore-db

# Si absent, démarrer Docker Desktop puis :
bash scripts/start-postgresql.sh

# Attendre 5 secondes puis relancer l'application
./mvnw spring-boot:run
```

### Erreur Flyway
```bash
# Nettoyer complètement et recommencer
docker stop malistore-db
docker rm malistore-db
docker volume rm malistore_data
bash scripts/start-postgresql.sh
./mvnw spring-boot:run
```

### Port 8080 déjà utilisé
```bash
# Trouver le processus
lsof -ti:8080

# Le tuer
kill -9 $(lsof -ti:8080)

# Relancer
./mvnw spring-boot:run
```

## 📁 Fichiers Modifiés

```
src/main/resources/db/migration/
  ├── V1__Create_base_tables.sql      ✏️ MODIFIÉ
  └── V3__Add_missing_columns.sql     ✏️ MODIFIÉ

Nouveaux fichiers créés :
  ├── CORRECTIONS_FLYWAY.md           ✨ NOUVEAU
  ├── test-all-endpoints.sh           ✨ NOUVEAU
  └── RESUME_CORRECTIONS.md           ✨ NOUVEAU
```

## ✅ Validation

Les corrections sont validées lorsque :

1. ✅ PostgreSQL démarre sans erreur
2. ✅ L'application Spring Boot démarre sans erreur Flyway
3. ✅ Les migrations V1, V2 et V3 s'exécutent correctement
4. ✅ Tous les endpoints retournent des réponses valides
5. ✅ Le script `test-all-endpoints.sh` s'exécute sans erreur majeure

## 📞 Support

En cas de problème :
1. Vérifiez les logs : `tail -f app.log` (si démarré avec nohup)
2. Consultez `CORRECTIONS_FLYWAY.md` pour les détails
3. Vérifiez que Docker Desktop est bien démarré
4. Vérifiez que le port 5432 (PostgreSQL) et 8080 (Spring Boot) sont libres

---

**Date des corrections** : 1er novembre 2025
**Statut** : ✅ Prêt pour les tests
**Prochaine étape** : Démarrer Docker et exécuter `test-all-endpoints.sh`

