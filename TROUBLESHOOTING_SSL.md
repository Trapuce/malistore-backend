# 🔒 Résolution du Problème SSL - MaliStore Backend

## ❌ Erreur Rencontrée

```
curl: (60) SSL certificate problem: self-signed certificate
```

## 🔍 Diagnostic

Cette erreur signifie que Traefik n'a pas encore généré de certificat Let's Encrypt valide. Voici les étapes pour résoudre ce problème.

---

## ✅ Solution Étape par Étape

### Étape 1 : Vérifier que le domaine pointe vers votre VPS

```bash
# Sur votre machine locale
nslookup backend-storemali.trapuce.tech

# Ou
dig backend-storemali.trapuce.tech

# Ou
ping backend-storemali.trapuce.tech
```

**Résultat attendu** : L'IP retournée doit être celle de votre VPS.

**Si ce n'est pas le cas** :
- Allez dans votre gestionnaire DNS (chez votre hébergeur de domaine)
- Créez un enregistrement A :
  - Nom : `backend-storemali`
  - Type : `A`
  - Valeur : `IP_DE_VOTRE_VPS`
  - TTL : `300` (5 minutes)
- Attendez 5-10 minutes pour la propagation DNS

---

### Étape 2 : Vérifier la configuration Traefik

Sur votre VPS :

```bash
# Vérifier que Traefik tourne
docker ps | grep traefik

# Voir les logs Traefik
docker logs traefik | grep -i "certificate\|letsencrypt\|acme"

# Vérifier les logs d'erreurs
docker logs traefik | grep -i error
```

---

### Étape 3 : Vérifier votre configuration Traefik

Assurez-vous que votre Traefik est configuré avec Let's Encrypt. Votre fichier `docker-compose.yml` de Traefik devrait ressembler à ceci :

```yaml
version: '3.8'

services:
  traefik:
    image: traefik:v3.0
    container_name: traefik
    restart: unless-stopped
    command:
      # API et Dashboard
      - "--api.dashboard=true"
      - "--api.insecure=true"
      
      # Providers
      - "--providers.docker=true"
      - "--providers.docker.exposedbydefault=false"
      - "--providers.docker.network=traefik-network"
      
      # Entrypoints
      - "--entrypoints.web.address=:80"
      - "--entrypoints.websecure.address=:443"
      
      # Redirection HTTP vers HTTPS
      - "--entrypoints.web.http.redirections.entrypoint.to=websecure"
      - "--entrypoints.web.http.redirections.entrypoint.scheme=https"
      
      # Let's Encrypt
      - "--certificatesresolvers.letsencrypt.acme.email=votre-email@example.com"
      - "--certificatesresolvers.letsencrypt.acme.storage=/letsencrypt/acme.json"
      - "--certificatesresolvers.letsencrypt.acme.httpchallenge=true"
      - "--certificatesresolvers.letsencrypt.acme.httpchallenge.entrypoint=web"
      
      # Logs
      - "--log.level=INFO"
      - "--accesslog=true"
    
    ports:
      - "80:80"
      - "443:443"
      - "8080:8080"  # Dashboard
    
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
    
    networks:
      - traefik-network

networks:
  traefik-network:
    external: true
```

**Points importants** :
- ✅ `certificatesresolvers.letsencrypt` configuré
- ✅ Email valide pour Let's Encrypt
- ✅ `httpchallenge` activé
- ✅ Volume `./letsencrypt` pour stocker les certificats

---

### Étape 4 : Vérifier les labels du backend

Sur votre VPS, vérifiez les labels du conteneur backend :

```bash
docker inspect malistore-backend | grep -A 20 Labels
```

Les labels importants :
```yaml
- "traefik.enable=true"
- "traefik.http.routers.malistore-backend.rule=Host(`backend-storemali.trapuce.tech`)"
- "traefik.http.routers.malistore-backend.entrypoints=websecure"
- "traefik.http.routers.malistore-backend.tls=true"
- "traefik.http.routers.malistore-backend.tls.certresolver=letsencrypt"
```

---

### Étape 5 : Vérifier que les ports sont ouverts

```bash
# Sur votre VPS
sudo ufw status

# Les ports suivants doivent être ouverts :
# 22/tcp   - SSH
# 80/tcp   - HTTP (pour le challenge Let's Encrypt)
# 443/tcp  - HTTPS
```

Si les ports ne sont pas ouverts :
```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload
```

---

### Étape 6 : Redémarrer Traefik et le backend

```bash
# Sur votre VPS

# Arrêter tout
docker-compose -f /path/to/traefik/docker-compose.yml down
cd ~/malistore-backend
docker-compose down

# Redémarrer Traefik d'abord
docker-compose -f /path/to/traefik/docker-compose.yml up -d

# Attendre 10 secondes
sleep 10

# Redémarrer le backend
cd ~/malistore-backend
docker-compose up -d

# Voir les logs
docker-compose logs -f backend
```

---

### Étape 7 : Forcer la génération du certificat

Si après 2-3 minutes le certificat n'est toujours pas généré :

```bash
# Sur votre VPS

# 1. Supprimer le fichier acme.json
sudo rm /path/to/traefik/letsencrypt/acme.json

# 2. Recréer le fichier avec les bonnes permissions
sudo touch /path/to/traefik/letsencrypt/acme.json
sudo chmod 600 /path/to/traefik/letsencrypt/acme.json

# 3. Redémarrer Traefik
docker restart traefik

# 4. Vérifier les logs
docker logs -f traefik
```

Vous devriez voir dans les logs :
```
time="..." level=info msg="Trying to challenge from ..." providerName=letsencrypt.acme
time="..." level=info msg="The key type is ..." providerName=letsencrypt.acme
time="..." level=info msg="legolog: ..." providerName=letsencrypt.acme
```

---

### Étape 8 : Tester avec curl en ignorant le certificat (temporaire)

En attendant que le certificat soit généré, vous pouvez tester avec :

```bash
# Option 1 : Ignorer l'erreur SSL (UNIQUEMENT POUR TESTER)
curl -k https://backend-storemali.trapuce.tech/api/categories

# Option 2 : Utiliser HTTP temporairement (si redirection pas encore active)
curl http://backend-storemali.trapuce.tech/api/categories
```

---

## 🔧 Configuration Traefik Recommandée pour MaliStore

Créez un fichier `docker-compose.traefik.yml` sur votre VPS :

```yaml
version: '3.8'

services:
  traefik:
    image: traefik:v3.0
    container_name: traefik
    restart: unless-stopped
    security_opt:
      - no-new-privileges:true
    
    command:
      # API
      - "--api.dashboard=true"
      
      # Providers
      - "--providers.docker=true"
      - "--providers.docker.exposedbydefault=false"
      - "--providers.docker.network=traefik-network"
      
      # Entrypoints
      - "--entrypoints.web.address=:80"
      - "--entrypoints.websecure.address=:443"
      
      # Redirection HTTP -> HTTPS
      - "--entrypoints.web.http.redirections.entrypoint.to=websecure"
      - "--entrypoints.web.http.redirections.entrypoint.scheme=https"
      - "--entrypoints.web.http.redirections.entrypoint.permanent=true"
      
      # Let's Encrypt
      - "--certificatesresolvers.letsencrypt.acme.email=votre-email@trapuce.tech"
      - "--certificatesresolvers.letsencrypt.acme.storage=/letsencrypt/acme.json"
      - "--certificatesresolvers.letsencrypt.acme.httpchallenge=true"
      - "--certificatesresolvers.letsencrypt.acme.httpchallenge.entrypoint=web"
      
      # Logs
      - "--log.level=INFO"
      - "--accesslog=true"
    
    ports:
      - "80:80"
      - "443:443"
    
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
    
    networks:
      - traefik-network
    
    labels:
      # Dashboard
      - "traefik.enable=true"
      - "traefik.http.routers.dashboard.rule=Host(`traefik.trapuce.tech`)"
      - "traefik.http.routers.dashboard.entrypoints=websecure"
      - "traefik.http.routers.dashboard.tls=true"
      - "traefik.http.routers.dashboard.tls.certresolver=letsencrypt"
      - "traefik.http.routers.dashboard.service=api@internal"
      - "traefik.http.routers.dashboard.middlewares=auth"
      
      # Authentification (changer le mot de passe)
      # Générer avec : echo $(htpasswd -nb admin votre_mot_de_passe) | sed -e s/\\$/\\$\\$/g
      - "traefik.http.middlewares.auth.basicauth.users=admin:$$apr1$$xxxxx"

networks:
  traefik-network:
    external: true

volumes:
  letsencrypt:
```

---

## 🎯 Checklist de Vérification SSL

- [ ] Le domaine `backend-storemali.trapuce.tech` pointe vers l'IP du VPS
- [ ] Traefik est en cours d'exécution
- [ ] Le réseau `traefik-network` existe
- [ ] Les ports 80 et 443 sont ouverts dans le firewall
- [ ] Traefik est configuré avec Let's Encrypt
- [ ] Le fichier `acme.json` existe avec les bonnes permissions (600)
- [ ] Le backend a les bons labels Traefik
- [ ] Le backend est dans le réseau `traefik-network`
- [ ] Attendre 2-5 minutes pour la génération du certificat

---

## 🔍 Commandes de Diagnostic

### Vérifier le certificat généré

```bash
# Voir le contenu d'acme.json
sudo cat /path/to/traefik/letsencrypt/acme.json | jq

# Vérifier le certificat avec OpenSSL
openssl s_client -connect backend-storemali.trapuce.tech:443 -servername backend-storemali.trapuce.tech
```

### Vérifier les logs Traefik en temps réel

```bash
docker logs -f traefik
```

### Tester le challenge HTTP de Let's Encrypt

```bash
# Le endpoint suivant doit être accessible
curl http://backend-storemali.trapuce.tech/.well-known/acme-challenge/test
```

---

## ⚡ Solution Rapide

Si vous voulez générer le certificat rapidement :

```bash
# 1. Vérifier DNS
nslookup backend-storemali.trapuce.tech

# 2. Arrêter tout
cd ~/malistore-backend
docker-compose down
docker stop traefik

# 3. Supprimer acme.json
sudo rm /path/to/traefik/letsencrypt/acme.json
sudo touch /path/to/traefik/letsencrypt/acme.json
sudo chmod 600 /path/to/traefik/letsencrypt/acme.json

# 4. Redémarrer Traefik
docker start traefik

# 5. Attendre 30 secondes
sleep 30

# 6. Redémarrer le backend
cd ~/malistore-backend
docker-compose up -d

# 7. Attendre 2 minutes et tester
sleep 120
curl https://backend-storemali.trapuce.tech/api/categories
```

---

## 📞 Si le Problème Persiste

### Vérifier que Traefik voit votre service

```bash
# API Traefik (si dashboard activé sur port 8080)
curl http://votre-vps-ip:8080/api/http/routers | jq

# Ou directement
docker exec traefik wget -O- http://localhost:8080/api/http/routers
```

### Activer les logs de debug

Modifiez votre configuration Traefik :
```yaml
- "--log.level=DEBUG"
```

Puis redémarrez et vérifiez les logs.

---

## ✅ Résultat Attendu

Une fois le certificat généré, vous devriez voir :

```bash
curl https://backend-storemali.trapuce.tech/api/categories
```

**Réponse** :
```json
{
  "status": "success",
  "message": "Operation successful",
  "data": [...]
}
```

Sans erreur SSL ! 🎉

---

**Date** : 1er novembre 2025  
**Auteur** : Assistant IA  
**Problème** : SSL certificate problem - self-signed certificate  
**Solution** : Configuration Traefik + Let's Encrypt

