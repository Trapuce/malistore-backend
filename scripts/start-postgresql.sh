#!/bin/bash

# Script pour lancer PostgreSQL avec Docker
CONTAINER_NAME="malistore-db"
POSTGRES_USER="malistore_user"
POSTGRES_PASSWORD="malistore_password"
POSTGRES_DB="malistore_db"
PORT=5432

echo "🚀 Démarrage de PostgreSQL avec Docker..."

# Vérifier si le conteneur existe déjà
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "🔁 Redémarrage du conteneur existant..."
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME
fi

# Lancer le conteneur PostgreSQL
docker run -d \
  --name $CONTAINER_NAME \
  -e POSTGRES_USER=$POSTGRES_USER \
  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
  -e POSTGRES_DB=$POSTGRES_DB \
  -p $PORT:5432 \
  -v malistore_data:/var/lib/postgresql/data \
  postgres:15-alpine

echo "✅ PostgreSQL démarré avec succès!"
echo "📊 Informations de connexion:"
echo "   - Host: localhost"
echo "   - Port: $PORT"
echo "   - User: $POSTGRES_USER"
echo "   - Password: $POSTGRES_PASSWORD"
echo "   - Database: $POSTGRES_DB"
echo ""
echo "🔗 URL de connexion: jdbc:postgresql://localhost:$PORT/$POSTGRES_DB"
echo ""
echo "📋 Commandes utiles:"
echo "   - Voir les logs: docker logs $CONTAINER_NAME"
echo "   - Arrêter: docker stop $CONTAINER_NAME"
echo "   - Supprimer: docker rm $CONTAINER_NAME"