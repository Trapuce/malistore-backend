# 🚀 Guide de Déploiement - MaliStore Backend sur VPS

## 📋 Prérequis

### Sur votre VPS
- ✅ Docker et Docker Compose installés
- ✅ Traefik déjà configuré et en cours d'exécution
- ✅ Réseau Docker `traefik-network` existant
- ✅ Domaine `backend-storemali.trapuce.tech` pointant vers votre VPS

### Sur votre machine locale
- ✅ Git installé
- ✅ Accès SSH à votre VPS

## 🔧 Étape 1 : Préparation des fichiers

### 1.1 Créer le fichier .env
Sur votre machine locale, dans le dossier du projet :

```bash
cd /Users/daoudatraore/Downloads/malistore-backend
cp .env.example .env
```

### 1.2 Éditer le fichier .env

Ouvrez `.env` et modifiez les valeurs suivantes :

```bash
# Database Configuration
POSTGRES_DB=malistore_db
POSTGRES_USER=malistore_user
POSTGRES_PASSWORD=VotrMotDePasseSecuriseIci123!

# JWT Configuration (TRÈS IMPORTANT - Générer une clé unique et sécurisée)
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Application URLs
APP_BASE_URL=https://backend-storemali.trapuce.tech
APP_FRONTEND_URL=https://storemali.trapuce.tech

# Stripe (si vous utilisez les paiements)
STRIPE_PUBLIC_KEY=pk_live_votre_cle_publique
STRIPE_SECRET_KEY=sk_live_votre_cle_secrete
STRIPE_WEBHOOK_SECRET=whsec_votre_webhook_secret

# Email (si vous utilisez l'envoi d'emails)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=votre-mot-de-passe-application
```

**⚠️ IMPORTANT** : Pour générer un JWT_SECRET sécurisé :
```bash
openssl rand -base64 64
```

## 🌐 Étape 2 : Configuration Traefik

### 2.1 Vérifier que le réseau Traefik existe

Sur votre VPS :
```bash
docker network ls | grep traefik
```

Si le réseau n'existe pas, créez-le :
```bash
docker network create traefik-network
```

### 2.2 Vérifier votre configuration Traefik

Assurez-vous que votre Traefik est configuré avec :
- Let's Encrypt pour les certificats SSL
- Entrypoint `websecure` sur le port 443

Exemple de configuration Traefik (traefik.yml) :
```yaml
entryPoints:
  web:
    address: ":80"
  websecure:
    address: ":443"

certificatesResolvers:
  letsencrypt:
    acme:
      email: votre-email@example.com
      storage: /letsencrypt/acme.json
      httpChallenge:
        entryPoint: web
```

## 📦 Étape 3 : Déploiement sur le VPS

### 3.1 Créer le dossier du projet sur le VPS

```bash
ssh votre-user@votre-vps-ip

# Sur le VPS
mkdir -p ~/malistore-backend
cd ~/malistore-backend
```

### 3.2 Transférer les fichiers depuis votre machine locale

**Option A : Via SCP**
```bash
# Sur votre machine locale
cd /Users/daoudatraore/Downloads/malistore-backend

# Transférer tous les fichiers
scp -r * votre-user@votre-vps-ip:~/malistore-backend/
```

**Option B : Via Git (Recommandé)**
```bash
# Sur votre machine locale - créer un repo git si pas déjà fait
cd /Users/daoudatraore/Downloads/malistore-backend
git init
git add .
git commit -m "Initial commit"

# Pousser vers votre repo (GitHub, GitLab, etc.)
git remote add origin votre-repo-url
git push -u origin main

# Sur le VPS - cloner le repo
cd ~/malistore-backend
git clone votre-repo-url .
```

### 3.3 Configurer les variables d'environnement sur le VPS

```bash
# Sur le VPS
cd ~/malistore-backend

# Créer le fichier .env (ne PAS commit ce fichier sur Git)
nano .env
```

Copiez le contenu de votre .env local et sauvegardez (Ctrl+X, Y, Enter).

### 3.4 Construire et démarrer les conteneurs

```bash
# Sur le VPS
cd ~/malistore-backend

# Construire l'image Docker
docker-compose build

# Démarrer les services
docker-compose up -d

# Vérifier les logs
docker-compose logs -f backend
```

## 📊 Étape 4 : Vérification du déploiement

### 4.1 Vérifier les conteneurs

```bash
docker-compose ps
```

Vous devriez voir :
- `malistore-backend` (running)
- `malistore-postgres` (running)

### 4.2 Vérifier les logs

```bash
# Logs de l'application
docker-compose logs -f backend

# Logs de PostgreSQL
docker-compose logs -f postgres
```

Attendez de voir :
```
Started MalistoreBackendApplication in X.XXX seconds
```

### 4.3 Tester l'API

```bash
# Test de santé
curl https://backend-storemali.trapuce.tech/actuator/health

# Test des catégories
curl https://backend-storemali.trapuce.tech/api/categories

# Test de l'inscription
curl -X POST https://backend-storemali.trapuce.tech/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Test1234!"
  }'
```

### 4.4 Accéder à Swagger UI

Ouvrez votre navigateur : `https://backend-storemali.trapuce.tech/swagger-ui.html`

## 🔐 Étape 5 : Sécurisation

### 5.1 Configurer un pare-feu (UFW)

```bash
# Sur le VPS
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

### 5.2 Sauvegardes automatiques de la base de données

Créer un script de backup :
```bash
# Sur le VPS
nano ~/backup-db.sh
```

Contenu :
```bash
#!/bin/bash
BACKUP_DIR="/home/votre-user/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

docker exec malistore-postgres pg_dump -U malistore_user malistore_db > $BACKUP_DIR/backup_$DATE.sql

# Garder seulement les 7 derniers backups
ls -t $BACKUP_DIR/backup_*.sql | tail -n +8 | xargs rm -f
```

Rendre le script exécutable et ajouter à cron :
```bash
chmod +x ~/backup-db.sh

# Ajouter à crontab (backup quotidien à 2h du matin)
crontab -e
# Ajouter la ligne :
0 2 * * * /home/votre-user/backup-db.sh
```

## 🔄 Étape 6 : Mises à jour

### 6.1 Mise à jour de l'application

```bash
# Sur le VPS
cd ~/malistore-backend

# Si utilisation de Git
git pull origin main

# Reconstruire et redémarrer
docker-compose build
docker-compose up -d

# Vérifier les logs
docker-compose logs -f backend
```

### 6.2 Mise à jour des variables d'environnement

```bash
# Sur le VPS
cd ~/malistore-backend
nano .env

# Après modification
docker-compose up -d
```

## 📈 Étape 7 : Monitoring (Optionnel)

### 7.1 Voir les logs en temps réel

```bash
# Tous les services
docker-compose logs -f

# Seulement le backend
docker-compose logs -f backend

# Seulement PostgreSQL
docker-compose logs -f postgres
```

### 7.2 Statistiques des conteneurs

```bash
docker stats
```

### 7.3 Espace disque

```bash
# Voir l'espace utilisé par Docker
docker system df

# Nettoyer les images inutilisées
docker system prune -a
```

## 🐛 Dépannage

### Problème : L'application ne démarre pas

```bash
# Vérifier les logs
docker-compose logs backend

# Vérifier que PostgreSQL est démarré
docker-compose ps postgres

# Redémarrer les services
docker-compose restart
```

### Problème : Erreur 502 Bad Gateway

```bash
# Vérifier que le backend est accessible
docker exec malistore-backend wget -O- http://localhost:8080/actuator/health

# Vérifier les labels Traefik
docker inspect malistore-backend | grep traefik

# Vérifier les logs Traefik
docker logs traefik
```

### Problème : Base de données inaccessible

```bash
# Se connecter à PostgreSQL
docker exec -it malistore-postgres psql -U malistore_user -d malistore_db

# Vérifier les tables
\dt

# Quitter
\q
```

### Problème : Certificat SSL non généré

```bash
# Vérifier les logs Traefik
docker logs traefik | grep letsencrypt

# Vérifier que le domaine pointe bien vers le VPS
nslookup backend-storemali.trapuce.tech

# Forcer le renouvellement (si nécessaire)
docker-compose down
docker-compose up -d
```

## 📋 Commandes utiles

```bash
# Démarrer les services
docker-compose up -d

# Arrêter les services
docker-compose down

# Redémarrer les services
docker-compose restart

# Voir les logs
docker-compose logs -f

# Reconstruire et redémarrer
docker-compose up -d --build

# Arrêter et supprimer tout (y compris les volumes)
docker-compose down -v

# Voir les processus
docker-compose ps

# Exécuter une commande dans le conteneur
docker exec -it malistore-backend sh

# Voir l'utilisation des ressources
docker stats malistore-backend
```

## 🎯 Checklist de déploiement

- [ ] Docker et Docker Compose installés sur le VPS
- [ ] Traefik configuré et en cours d'exécution
- [ ] Réseau `traefik-network` créé
- [ ] Domaine `backend-storemali.trapuce.tech` pointe vers le VPS
- [ ] Fichier `.env` créé et configuré avec des valeurs sécurisées
- [ ] JWT_SECRET généré de manière sécurisée
- [ ] Fichiers transférés sur le VPS
- [ ] `docker-compose build` exécuté avec succès
- [ ] `docker-compose up -d` exécuté avec succès
- [ ] Conteneurs en cours d'exécution (vérifier avec `docker-compose ps`)
- [ ] Application accessible via HTTPS
- [ ] Certificat SSL généré par Let's Encrypt
- [ ] Swagger UI accessible
- [ ] Tests API réussis
- [ ] Backups configurés

## 🌟 Améliorations futures

1. **CI/CD** : Mettre en place GitHub Actions pour le déploiement automatique
2. **Monitoring** : Ajouter Prometheus + Grafana
3. **Logs centralisés** : Utiliser ELK Stack ou Loki
4. **CDN** : Utiliser un CDN pour les images uploadées
5. **Redis** : Ajouter Redis pour le cache
6. **Rate Limiting** : Implémenter un rate limiter

---

**Votre backend MaliStore est maintenant déployé en production ! 🎉**

Pour toute question ou problème, vérifiez d'abord les logs avec `docker-compose logs -f backend`.

