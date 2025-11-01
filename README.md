# 🛍️ MaliStore Backend

API Backend REST pour une plateforme e-commerce complète construite avec Spring Boot 3, PostgreSQL et Docker.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table des Matières

- [Fonctionnalités](#-fonctionnalités)
- [Technologies](#-technologies)
- [Démarrage Rapide](#-démarrage-rapide)
- [Déploiement](#-déploiement)
- [API Documentation](#-api-documentation)
- [Architecture](#-architecture)
- [Sécurité](#-sécurité)

## ✨ Fonctionnalités

### Authentification & Autorisation
- ✅ Inscription et connexion avec JWT
- ✅ Refresh tokens
- ✅ Réinitialisation de mot de passe par email
- ✅ Rôles utilisateurs (USER, ADMIN)

### Gestion des Produits
- ✅ CRUD complet des produits
- ✅ Gestion des catégories
- ✅ Upload d'images multiples
- ✅ Recherche et filtrage
- ✅ Gestion du stock avec alertes

### Panier & Commandes
- ✅ Gestion du panier utilisateur
- ✅ Création et suivi des commandes
- ✅ Historique des commandes
- ✅ Gestion des adresses de livraison

### Paiements
- ✅ Intégration Stripe
- ✅ Webhooks Stripe
- ✅ Suivi des paiements

### Administration
- ✅ Dashboard admin
- ✅ Gestion des commandes
- ✅ Gestion des produits et catégories
- ✅ Gestion du stock

## 🛠️ Technologies

**Backend:**
- Java 17
- Spring Boot 3.5.7
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL 15
- Flyway (migrations)
- MapStruct (mapping)
- Lombok

**Paiements:**
- Stripe API

**Documentation:**
- OpenAPI 3 / Swagger UI

**Déploiement:**
- Docker & Docker Compose
- Traefik (reverse proxy)

## 🚀 Démarrage Rapide

### Prérequis

- Java 17+
- Docker & Docker Compose
- Maven 3.9+

### 1. Cloner le projet

```bash
git clone https://github.com/Trapuce/malistore-backend.git
cd malistore-backend
```

### 2. Configuration

Créer le fichier `.env` :

```bash
cp env.example .env
```

Éditer `.env` et configurer au minimum :

```env
POSTGRES_PASSWORD=your_secure_password
JWT_SECRET=$(openssl rand -base64 64)
```

### 3. Lancement avec Docker

```bash
# Démarrer PostgreSQL et l'application
docker-compose up -d

# Voir les logs
docker-compose logs -f backend
```

### 4. Lancement en développement (local)

```bash
# Démarrer PostgreSQL
bash scripts/start-postgresql.sh

# Lancer l'application
./mvnw spring-boot:run
```

L'API sera accessible sur : `http://localhost:8080`

### 5. Accéder à la documentation

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** : http://localhost:8080/v3/api-docs

## 📦 Déploiement sur VPS

### Déploiement Automatique

```bash
# Configurer votre .env
cp env.example .env
nano .env

# Déployer sur le VPS
./deploy.sh root@votre-vps-ip
```

### Déploiement Manuel

Consultez le guide détaillé : [DEPLOIEMENT_VPS.md](DEPLOIEMENT_VPS.md)

**Quick Start** : [QUICK_START.md](QUICK_START.md)

## 📚 API Documentation

### Endpoints Publics

#### Authentification
```bash
POST /api/auth/register  # Créer un compte
POST /api/auth/login     # Se connecter
POST /api/auth/refresh   # Rafraîchir le token
```

#### Produits & Catégories
```bash
GET /api/categories              # Liste des catégories
GET /api/products                # Liste des produits
GET /api/products/{id}           # Détails d'un produit
GET /api/products?categoryId=1   # Produits par catégorie
```

### Endpoints Authentifiés

#### Panier
```bash
GET    /api/cart              # Voir mon panier
POST   /api/cart/items        # Ajouter au panier
PUT    /api/cart/items/{id}   # Modifier quantité
DELETE /api/cart/items/{id}   # Retirer du panier
DELETE /api/cart              # Vider le panier
```

#### Commandes
```bash
GET  /api/orders      # Mes commandes
POST /api/orders      # Créer une commande
GET  /api/orders/{id} # Détails d'une commande
```

#### Adresses
```bash
GET    /api/shipping-addresses     # Mes adresses
POST   /api/shipping-addresses     # Ajouter une adresse
PUT    /api/shipping-addresses/{id}
DELETE /api/shipping-addresses/{id}
```

#### Paiements
```bash
POST /api/payments/create-session  # Créer session Stripe
```

### Endpoints Admin

```bash
# Produits
POST   /api/admin/products
PUT    /api/admin/products/{id}
DELETE /api/admin/products/{id}
POST   /api/admin/products/{id}/images

# Catégories
POST   /api/admin/categories
PUT    /api/admin/categories/{id}
DELETE /api/admin/categories/{id}

# Commandes
GET /api/admin/orders
PUT /api/admin/orders/{id}/status
```

### Exemples d'utilisation

**Inscription**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Connexion**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Ajouter au panier** (avec token)
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

## 🏗️ Architecture

```
malistore-backend/
├── src/
│   ├── main/
│   │   ├── java/com/malistore_backend/
│   │   │   ├── config/           # Configuration (Security, OpenAPI, etc.)
│   │   │   ├── data/
│   │   │   │   ├── entity/       # Entités JPA
│   │   │   │   └── repository/   # Repositories Spring Data
│   │   │   ├── security/         # JWT, Filters
│   │   │   ├── service/          # Logique métier
│   │   │   └── web/
│   │   │       ├── api/          # Contrôleurs REST
│   │   │       ├── dto/          # Data Transfer Objects
│   │   │       ├── exception/    # Gestion des erreurs
│   │   │       └── mappers/      # MapStruct mappers
│   │   └── resources/
│   │       ├── application.yml           # Config développement
│   │       ├── application-prod.yml      # Config production
│   │       └── db/migration/             # Scripts Flyway
│   └── test/
├── Dockerfile                    # Image Docker
├── docker-compose.yml           # Orchestration
├── deploy.sh                    # Script de déploiement
└── README.md
```

## 🔐 Sécurité

### JWT Authentication
- Les tokens JWT ont une durée de vie de 24h
- Les refresh tokens sont valides 7 jours
- Tous les endpoints protégés nécessitent un token valide

### Mots de passe
- Hashage avec BCrypt
- Validation de force minimale
- Réinitialisation sécurisée par email

### CORS
- Configuré pour accepter les requêtes du frontend
- Headers personnalisables

### Variables d'environnement
- ⚠️ Ne jamais commiter le fichier `.env`
- ⚠️ Utiliser des secrets forts en production
- ⚠️ Générer un nouveau `JWT_SECRET` unique

## 📊 Base de Données

### Structure
- **users** : Utilisateurs
- **categories** : Catégories de produits
- **products** : Produits
- **product_images** : Images de produits
- **cart_items** : Articles du panier
- **shipping_addresses** : Adresses de livraison
- **orders** : Commandes
- **order_items** : Articles des commandes
- **payments** : Paiements Stripe
- **password_reset_tokens** : Tokens de reset

### Migrations
Les migrations Flyway sont automatiquement exécutées au démarrage :
- `V1__Create_base_tables.sql` : Structure de base
- `V2__Insert_default_data.sql` : Données initiales
- `V3__Add_missing_columns.sql` : Colonnes additionnelles

## 🧪 Tests

### Tests automatisés

```bash
# Lancer le script de test de tous les endpoints
bash test-all-endpoints.sh
```

### Tests manuels

Consultez [TESTS_REUSSIS.md](TESTS_REUSSIS.md) pour les résultats des tests.

## 📈 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Métriques
- Endpoint Prometheus : `/actuator/metrics`

## 🤝 Contribution

Les contributions sont les bienvenues !

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add some AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 Données par Défaut

### Compte Admin
- **Email** : `admin@malistore.com`
- **Mot de passe** : `admin123`
- **Rôle** : ADMIN

### Catégories (5)
1. Électronique et Gadgets
2. Vêtements et Mode
3. Maison et Jardin
4. Sports et Loisirs
5. Livres et Médias

### Produits (5)
Un produit exemple par catégorie avec images.

## 📄 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👨‍💻 Auteur

**Daouda Traoré**

- GitHub: [@Trapuce](https://github.com/Trapuce)
- Email: contact@trapuce.tech

## 🙏 Remerciements

- Spring Boot Team
- PostgreSQL Community
- Stripe API

---

**🌐 URLs de Production**

- **API Backend** : https://backend-storemali.trapuce.tech
- **Swagger UI** : https://backend-storemali.trapuce.tech/swagger-ui.html
- **Frontend** : https://storemali.trapuce.tech

---

**Créé avec ❤️ pour MaliStore**

