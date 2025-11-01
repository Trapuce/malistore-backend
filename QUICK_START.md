# 🚀 Quick Start - Déploiement Rapide

## 📋 Prérequis VPS
- Docker et Docker Compose installés
- Traefik en cours d'exécution
- Réseau Docker `traefik-network` créé
- Domaine `backend-storemali.trapuce.tech` pointant vers le VPS

## ⚡ Déploiement en 5 Minutes

### 1. Créer le fichier .env
```bash
cd /Users/daoudatraore/Downloads/malistore-backend
cp env.example .env
nano .env
```

Modifiez au minimum :
```bash
POSTGRES_PASSWORD=VotreMotDePasseSecurise123!
JWT_SECRET=$(openssl rand -base64 64)
```

### 2. Déployer avec le script automatique
```bash
./deploy.sh root@votre-vps-ip
```

OU déploiement manuel :

### 2. Transférer sur le VPS
```bash
# Depuis votre machine locale
scp -r * root@votre-vps-ip:~/malistore-backend/
```

### 3. Sur le VPS
```bash
ssh root@votre-vps-ip

cd ~/malistore-backend

# Construire et démarrer
docker-compose build
docker-compose up -d

# Voir les logs
docker-compose logs -f backend
```

### 4. Tester
```bash
curl https://backend-storemali.trapuce.tech/actuator/health
curl https://backend-storemali.trapuce.tech/api/categories
```

## ✅ Vérifications

```bash
# Sur le VPS
docker-compose ps          # Vérifier que tout tourne
docker-compose logs -f     # Voir les logs en temps réel
docker stats               # Utilisation des ressources
```

## 🔧 Commandes Utiles

```bash
# Redémarrer
docker-compose restart

# Arrêter
docker-compose down

# Voir les logs
docker-compose logs -f backend

# Mettre à jour
git pull origin main
docker-compose up -d --build
```

## 📍 URLs

- **API** : https://backend-storemali.trapuce.tech
- **Swagger** : https://backend-storemali.trapuce.tech/swagger-ui.html
- **Health** : https://backend-storemali.trapuce.tech/actuator/health

## 🆘 Problèmes Courants

### Le conteneur redémarre en boucle
```bash
docker-compose logs backend
# Vérifier les variables d'environnement dans .env
```

### 502 Bad Gateway
```bash
# Vérifier que Traefik voit le service
docker logs traefik | grep malistore

# Vérifier le réseau
docker network inspect traefik-network
```

### Base de données inaccessible
```bash
docker exec -it malistore-postgres psql -U malistore_user -d malistore_db
```

---

Pour plus de détails, consultez **DEPLOIEMENT_VPS.md**

