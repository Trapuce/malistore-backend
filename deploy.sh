#!/bin/bash

# Script de déploiement rapide pour MaliStore Backend
# Usage: ./deploy.sh [VPS_USER@VPS_IP]

set -e

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
VPS_TARGET="${1:-root@votre-vps-ip}"
PROJECT_NAME="malistore-backend"
VPS_DIR="~/$PROJECT_NAME"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}🚀 Déploiement MaliStore Backend${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Vérification du fichier .env
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ Erreur: Fichier .env manquant${NC}"
    echo -e "${YELLOW}Créez le fichier .env à partir de .env.example${NC}"
    exit 1
fi

echo -e "${YELLOW}📋 Configuration:${NC}"
echo -e "  VPS: $VPS_TARGET"
echo -e "  Projet: $PROJECT_NAME"
echo -e "  Dossier VPS: $VPS_DIR\n"

# Confirmation
read -p "Continuer avec le déploiement? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${RED}Déploiement annulé.${NC}"
    exit 1
fi

# Étape 1: Créer le dossier sur le VPS
echo -e "\n${YELLOW}📁 Étape 1: Création du dossier sur le VPS...${NC}"
ssh $VPS_TARGET "mkdir -p $VPS_DIR"
echo -e "${GREEN}✅ Dossier créé${NC}"

# Étape 2: Transférer les fichiers
echo -e "\n${YELLOW}📤 Étape 2: Transfert des fichiers...${NC}"
rsync -avz --exclude 'target' \
           --exclude '.git' \
           --exclude 'node_modules' \
           --exclude 'uploads' \
           --exclude '*.log' \
           --exclude '.idea' \
           --exclude '.vscode' \
           ./ $VPS_TARGET:$VPS_DIR/

echo -e "${GREEN}✅ Fichiers transférés${NC}"

# Étape 3: Construire et démarrer les conteneurs
echo -e "\n${YELLOW}🐳 Étape 3: Construction des conteneurs Docker...${NC}"
ssh $VPS_TARGET "cd $VPS_DIR && docker-compose build"
echo -e "${GREEN}✅ Images Docker construites${NC}"

# Étape 4: Démarrer les services
echo -e "\n${YELLOW}🚀 Étape 4: Démarrage des services...${NC}"
ssh $VPS_TARGET "cd $VPS_DIR && docker-compose up -d"
echo -e "${GREEN}✅ Services démarrés${NC}"

# Étape 5: Attendre le démarrage complet
echo -e "\n${YELLOW}⏳ Étape 5: Attente du démarrage complet (30 secondes)...${NC}"
sleep 30

# Étape 6: Vérifier les services
echo -e "\n${YELLOW}🔍 Étape 6: Vérification des services...${NC}"
ssh $VPS_TARGET "cd $VPS_DIR && docker-compose ps"

# Étape 7: Afficher les logs
echo -e "\n${YELLOW}📋 Étape 7: Logs récents...${NC}"
ssh $VPS_TARGET "cd $VPS_DIR && docker-compose logs --tail=50 backend"

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✅ Déploiement terminé !${NC}"
echo -e "${BLUE}========================================${NC}\n"

echo -e "${GREEN}URLs de l'application :${NC}"
echo -e "  API: ${BLUE}https://backend-storemali.trapuce.tech${NC}"
echo -e "  Swagger: ${BLUE}https://backend-storemali.trapuce.tech/swagger-ui.html${NC}"
echo -e "  Health: ${BLUE}https://backend-storemali.trapuce.tech/actuator/health${NC}"

echo -e "\n${YELLOW}Commandes utiles :${NC}"
echo -e "  Voir les logs: ${BLUE}ssh $VPS_TARGET 'cd $VPS_DIR && docker-compose logs -f backend'${NC}"
echo -e "  Redémarrer: ${BLUE}ssh $VPS_TARGET 'cd $VPS_DIR && docker-compose restart'${NC}"
echo -e "  Arrêter: ${BLUE}ssh $VPS_TARGET 'cd $VPS_DIR && docker-compose down'${NC}"

echo -e "\n${GREEN}🎉 Déploiement réussi !${NC}\n"

