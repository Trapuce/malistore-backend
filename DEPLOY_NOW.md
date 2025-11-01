# 🚀 Déploiement Immédiat - MaliStore Backend

## ⚡ Configuration Adaptée à Votre Traefik

Votre configuration Traefik utilise :
- ✅ Réseau : `web`
- ✅ Cert Resolver : `myresolver`
- ✅ Email : `trapucework33@gmail.com`

Le `docker-compose.yml` a été **automatiquement adapté** ! 🎉

---

## 📋 Instructions de Déploiement

### Étape 1 : Vérifier le DNS

```bash
# Sur votre machine locale
nslookup backend-storemali.trapuce.tech
```

**Résultat attendu** : L'IP doit être celle de votre VPS.

Si ce n'est pas le cas, ajoutez un enregistrement A dans votre DNS :
- Nom : `backend-storemali`
- Type : `A`
- Valeur : `IP_DE_VOTRE_VPS`

---

### Étape 2 : Préparer le fichier .env

**Sur votre VPS** :

```bash
ssh root@votre-vps-ip
cd ~/malistore-backend
nano .env
```

**Contenu du fichier .env** (copiez-collez et modifiez les valeurs) :

```bash
# Database Configuration
POSTGRES_DB=malistore_db
POSTGRES_USER=malistore_user
POSTGRES_PASSWORD=VotrMotDePasseSecurise123!

# JWT Configuration (GÉNÉRER UNE CLÉ UNIQUE)
JWT_SECRET=VotreCleSecreteTresLongueEtAleatoire123456789012345678901234567890
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Application URLs
APP_BASE_URL=https://backend-storemali.trapuce.tech
APP_FRONTEND_URL=https://storemali.trapuce.tech

# Stripe (optionnel)
STRIPE_PUBLIC_KEY=
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=
STRIPE_SUCCESS_URL=https://storemali.trapuce.tech/payment/success
STRIPE_CANCEL_URL=https://storemali.trapuce.tech/payment/cancel

# Email (optionnel)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# Stock Alert
STOCK_ALERT_THRESHOLD=5
STOCK_ALERT_EMAIL=admin@malistore.com

# Java Options
JAVA_OPTS=-Xms256m -Xmx512m
```

**Sauvegardez** : `Ctrl+X`, puis `Y`, puis `Enter`

---

### Étape 3 : Générer des secrets sécurisés

```bash
# Sur votre VPS - Générer JWT_SECRET
openssl rand -base64 64

# Copiez le résultat et mettez-le dans JWT_SECRET dans le fichier .env
```

```bash
# Générer POSTGRES_PASSWORD
openssl rand -base64 32

# Copiez le résultat et mettez-le dans POSTGRES_PASSWORD dans le fichier .env
```

---

### Étape 4 : Démarrer l'application

```bash
# Sur votre VPS
cd ~/malistore-backend

# Construire l'image
docker-compose build

# Démarrer les services
docker-compose up -d

# Voir les logs en temps réel
docker-compose logs -f backend
```

**Attendez de voir** :
```
Started MalistoreBackendApplication in X.XXX seconds
```

---

### Étape 5 : Vérifier le déploiement

```bash
# Vérifier que les conteneurs tournent
docker-compose ps

# Devrait afficher :
# malistore-backend    running
# malistore-postgres   running
```

---

### Étape 6 : Attendre la génération du certificat SSL

Let's Encrypt va générer automatiquement un certificat. **Attendez 2-3 minutes**.

```bash
# Voir les logs Traefik
docker logs traefik | grep -i "backend-storemali"

# Vous devriez voir :
# "Trying to challenge from backend-storemali.trapuce.tech"
# "The certificate for domain backend-storemali.trapuce.tech has been generated"
```

---

### Étape 7 : Tester l'API

```bash
# Test de santé
curl https://backend-storemali.trapuce.tech/actuator/health

# Test des catégories
curl https://backend-storemali.trapuce.tech/api/categories

# Si vous obtenez encore une erreur SSL, attendez 1-2 minutes de plus
```

---

## 🎉 Résultat Attendu

**Succès !** Vous devriez voir :

```json
{
  "status": "success",
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "name": "Électronique et Gadgets",
      ...
    }
  ]
}
```

---

## 🌐 URLs de Votre API

- **API** : https://backend-storemali.trapuce.tech
- **Swagger UI** : https://backend-storemali.trapuce.tech/swagger-ui.html
- **Health Check** : https://backend-storemali.trapuce.tech/actuator/health
- **Catégories** : https://backend-storemali.trapuce.tech/api/categories

---

## 🧪 Tests Rapides

### Test 1 : Inscription
```bash
curl -X POST https://backend-storemali.trapuce.tech/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Test1234!"
  }'
```

### Test 2 : Connexion
```bash
curl -X POST https://backend-storemali.trapuce.tech/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!"
  }'
```

---

## 🐛 Si le certificat SSL n'est pas généré

### Vérifier les logs Traefik

```bash
docker logs traefik | tail -100
```

### Vérifier que le port 80 est accessible

```bash
# Sur votre VPS
sudo ufw status

# Le port 80 DOIT être ouvert (pour le challenge Let's Encrypt)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload
```

### Forcer la regénération du certificat

```bash
# Arrêter le backend
cd ~/malistore-backend
docker-compose down

# Redémarrer Traefik
docker restart traefik

# Attendre 10 secondes
sleep 10

# Redémarrer le backend
docker-compose up -d

# Attendre 2 minutes et vérifier
sleep 120
docker logs traefik | grep backend-storemali
```

---

## ✅ Checklist Finale

- [ ] DNS configuré (backend-storemali.trapuce.tech → IP VPS)
- [ ] Fichier `.env` créé avec secrets sécurisés
- [ ] JWT_SECRET généré et unique
- [ ] POSTGRES_PASSWORD fort
- [ ] Ports 80 et 443 ouverts
- [ ] `docker-compose up -d` exécuté
- [ ] Certificat SSL généré (attendre 2-3 min)
- [ ] API accessible via HTTPS
- [ ] Tests d'inscription/connexion fonctionnels

---

## 📞 Support

Si vous rencontrez un problème :

1. **Vérifiez les logs** : `docker-compose logs -f backend`
2. **Vérifiez Traefik** : `docker logs traefik | tail -50`
3. **Consultez** : [TROUBLESHOOTING_SSL.md](TROUBLESHOOTING_SSL.md)

---

## 🎯 Commandes Utiles

```bash
# Voir les logs
docker-compose logs -f backend

# Redémarrer
docker-compose restart

# Arrêter
docker-compose down

# Mettre à jour
git pull origin main
docker-compose up -d --build

# Backup DB
docker exec malistore-postgres pg_dump -U malistore_user malistore_db > backup.sql
```

---

**Votre backend est maintenant déployé en production ! 🚀**

**Configuration adaptée à votre Traefik existant avec réseau `web` et resolver `myresolver`.**

