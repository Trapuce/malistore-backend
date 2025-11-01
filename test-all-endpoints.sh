#!/bin/bash

# Script de test automatique de tous les endpoints de MaliStore Backend
# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
ADMIN_TOKEN=""
USER_TOKEN=""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}🧪 Test des Endpoints MaliStore Backend${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Fonction pour afficher le résultat
test_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${RED}❌ ÉCHEC${NC}"
    fi
}

# Fonction pour faire une pause
pause() {
    sleep 0.5
}

echo -e "${YELLOW}📋 1. TESTS D'AUTHENTIFICATION${NC}\n"

# 1.1 Connexion Admin
echo -n "1.1 Connexion Admin (admin@malistore.com)... "
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@malistore.com",
    "password": "admin123"
  }')

if echo "$ADMIN_RESPONSE" | grep -q "token"; then
    ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
    echo -e "${GREEN}✅ SUCCÈS${NC} (Token obtenu)"
    echo "   Token: ${ADMIN_TOKEN:0:20}..."
else
    echo -e "${RED}❌ ÉCHEC${NC}"
    echo "   Réponse: $ADMIN_RESPONSE"
fi
pause

# 1.2 Inscription d'un nouvel utilisateur
echo -n "1.2 Inscription nouvel utilisateur... "
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Test1234!",
    "firstName": "Test",
    "lastName": "User"
  }')

if echo "$REGISTER_RESPONSE" | grep -q "token\|email"; then
    echo -e "${GREEN}✅ SUCCÈS${NC}"
else
    echo -e "${YELLOW}⚠️  PEUT-ÊTRE DÉJÀ EXISTANT${NC}"
fi
pause

# 1.3 Connexion utilisateur normal
echo -n "1.3 Connexion utilisateur (testuser@example.com)... "
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Test1234!"
  }')

if echo "$USER_RESPONSE" | grep -q "token"; then
    USER_TOKEN=$(echo "$USER_RESPONSE" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
    echo -e "${GREEN}✅ SUCCÈS${NC} (Token obtenu)"
    echo "   Token: ${USER_TOKEN:0:20}..."
else
    echo -e "${RED}❌ ÉCHEC${NC}"
    echo "   Réponse: $USER_RESPONSE"
fi
pause

echo -e "\n${YELLOW}📚 2. TESTS DES CATÉGORIES${NC}\n"

# 2.1 Lister toutes les catégories (Public)
echo -n "2.1 GET /api/categories (Public)... "
CATEGORIES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/categories")
if echo "$CATEGORIES_RESPONSE" | grep -q "id\|name"; then
    CATEGORY_COUNT=$(echo "$CATEGORIES_RESPONSE" | grep -o '"id"' | wc -l | tr -d ' ')
    echo -e "${GREEN}✅ SUCCÈS${NC} ($CATEGORY_COUNT catégories trouvées)"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi
pause

# 2.2 Créer une nouvelle catégorie (Admin)
echo -n "2.2 POST /api/admin/categories (Admin)... "
CREATE_CATEGORY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/categories" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Test Category",
    "description": "Catégorie de test créée automatiquement",
    "imageUrl": "https://via.placeholder.com/400",
    "active": true,
    "sortOrder": 100
  }')

if echo "$CREATE_CATEGORY_RESPONSE" | grep -q "id"; then
    NEW_CATEGORY_ID=$(echo "$CREATE_CATEGORY_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo -e "${GREEN}✅ SUCCÈS${NC} (ID: $NEW_CATEGORY_ID)"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
    echo "   Réponse: $CREATE_CATEGORY_RESPONSE"
fi
pause

# 2.3 Modifier la catégorie
if [ ! -z "$NEW_CATEGORY_ID" ]; then
    echo -n "2.3 PUT /api/admin/categories/$NEW_CATEGORY_ID (Admin)... "
    UPDATE_CATEGORY_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/admin/categories/$NEW_CATEGORY_ID" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{
        "name": "Test Category - Modifiée",
        "description": "Description modifiée",
        "active": true
      }')
    
    if echo "$UPDATE_CATEGORY_RESPONSE" | grep -q "id\|Modifiée"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${RED}❌ ÉCHEC${NC}"
    fi
    pause
fi

echo -e "\n${YELLOW}📦 3. TESTS DES PRODUITS${NC}\n"

# 3.1 Lister tous les produits (Public)
echo -n "3.1 GET /api/products (Public)... "
PRODUCTS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/products")
if echo "$PRODUCTS_RESPONSE" | grep -q "id\|name"; then
    PRODUCT_COUNT=$(echo "$PRODUCTS_RESPONSE" | grep -o '"id"' | wc -l | tr -d ' ')
    echo -e "${GREEN}✅ SUCCÈS${NC} ($PRODUCT_COUNT produits trouvés)"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi
pause

# 3.2 Obtenir un produit par ID
echo -n "3.2 GET /api/products/1 (Public)... "
PRODUCT_DETAIL=$(curl -s -X GET "$BASE_URL/api/products/1")
if echo "$PRODUCT_DETAIL" | grep -q "id"; then
    echo -e "${GREEN}✅ SUCCÈS${NC}"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi
pause

# 3.3 Créer un nouveau produit (Admin)
echo -n "3.3 POST /api/admin/products (Admin)... "
CREATE_PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Produit de Test",
    "description": "Produit créé automatiquement pour les tests",
    "price": 99.99,
    "stock": 100,
    "categoryId": 1,
    "imageUrl": "https://via.placeholder.com/400",
    "active": true
  }')

if echo "$CREATE_PRODUCT_RESPONSE" | grep -q "id"; then
    NEW_PRODUCT_ID=$(echo "$CREATE_PRODUCT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo -e "${GREEN}✅ SUCCÈS${NC} (ID: $NEW_PRODUCT_ID)"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
    echo "   Réponse: $CREATE_PRODUCT_RESPONSE"
fi
pause

# 3.4 Modifier le produit
if [ ! -z "$NEW_PRODUCT_ID" ]; then
    echo -n "3.4 PUT /api/admin/products/$NEW_PRODUCT_ID (Admin)... "
    UPDATE_PRODUCT_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/admin/products/$NEW_PRODUCT_ID" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{
        "name": "Produit de Test - Modifié",
        "price": 149.99,
        "stock": 150
      }')
    
    if echo "$UPDATE_PRODUCT_RESPONSE" | grep -q "id\|149.99"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${RED}❌ ÉCHEC${NC}"
    fi
    pause
fi

echo -e "\n${YELLOW}🛒 4. TESTS DU PANIER${NC}\n"

# 4.1 Voir le panier (vide au début)
echo -n "4.1 GET /api/cart (User)... "
CART_RESPONSE=$(curl -s -X GET "$BASE_URL/api/cart" \
  -H "Authorization: Bearer $USER_TOKEN")
if echo "$CART_RESPONSE" | grep -q "items\|totalPrice"; then
    echo -e "${GREEN}✅ SUCCÈS${NC}"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi
pause

# 4.2 Ajouter un produit au panier
echo -n "4.2 POST /api/cart/items (User)... "
ADD_TO_CART_RESPONSE=$(curl -s -X POST "$BASE_URL/api/cart/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "productId": 1,
    "quantity": 2
  }')

if echo "$ADD_TO_CART_RESPONSE" | grep -q "id\|quantity"; then
    CART_ITEM_ID=$(echo "$ADD_TO_CART_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo -e "${GREEN}✅ SUCCÈS${NC} (Article ID: $CART_ITEM_ID)"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
    echo "   Réponse: $ADD_TO_CART_RESPONSE"
fi
pause

# 4.3 Modifier la quantité
if [ ! -z "$CART_ITEM_ID" ]; then
    echo -n "4.3 PUT /api/cart/items/$CART_ITEM_ID (User)... "
    UPDATE_CART_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/cart/items/$CART_ITEM_ID" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $USER_TOKEN" \
      -d '{
        "quantity": 3
      }')
    
    if echo "$UPDATE_CART_RESPONSE" | grep -q "id"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${RED}❌ ÉCHEC${NC}"
    fi
    pause
fi

echo -e "\n${YELLOW}📍 5. TESTS DES ADRESSES DE LIVRAISON${NC}\n"

# 5.1 Créer une adresse de livraison
echo -n "5.1 POST /api/shipping-addresses (User)... "
CREATE_ADDRESS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/shipping-addresses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "addressName": "Maison",
    "streetAddress": "123 Rue de Test",
    "streetAddressLine2": "Appartement 1",
    "city": "Paris",
    "stateProvince": "Île-de-France",
    "postalCode": "75001",
    "country": "France",
    "phoneNumber": "+33123456789",
    "isDefault": true
  }')

if echo "$CREATE_ADDRESS_RESPONSE" | grep -q "id"; then
    ADDRESS_ID=$(echo "$CREATE_ADDRESS_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo -e "${GREEN}✅ SUCCÈS${NC} (ID: $ADDRESS_ID)"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
    echo "   Réponse: $CREATE_ADDRESS_RESPONSE"
fi
pause

# 5.2 Lister les adresses
echo -n "5.2 GET /api/shipping-addresses (User)... "
ADDRESSES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/shipping-addresses" \
  -H "Authorization: Bearer $USER_TOKEN")
if echo "$ADDRESSES_RESPONSE" | grep -q "id"; then
    echo -e "${GREEN}✅ SUCCÈS${NC}"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi
pause

echo -e "\n${YELLOW}📦 6. TESTS DES COMMANDES${NC}\n"

# 6.1 Créer une commande
if [ ! -z "$ADDRESS_ID" ]; then
    echo -n "6.1 POST /api/orders (User)... "
    CREATE_ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $USER_TOKEN" \
      -d "{
        \"shippingAddressId\": $ADDRESS_ID
      }")
    
    if echo "$CREATE_ORDER_RESPONSE" | grep -q "id\|orderNumber"; then
        ORDER_ID=$(echo "$CREATE_ORDER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
        echo -e "${GREEN}✅ SUCCÈS${NC} (Commande ID: $ORDER_ID)"
    else
        echo -e "${RED}❌ ÉCHEC${NC}"
        echo "   Réponse: $CREATE_ORDER_RESPONSE"
    fi
    pause
else
    echo "6.1 POST /api/orders (User)... ${RED}❌ SAUTÉ (Pas d'adresse)${NC}"
fi

# 6.2 Lister les commandes de l'utilisateur
echo -n "6.2 GET /api/orders (User)... "
USER_ORDERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/orders" \
  -H "Authorization: Bearer $USER_TOKEN")
if echo "$USER_ORDERS_RESPONSE" | grep -q "id\|orderNumber\|\[\]"; then
    echo -e "${GREEN}✅ SUCCÈS${NC}"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi
pause

# 6.3 Obtenir les détails d'une commande
if [ ! -z "$ORDER_ID" ]; then
    echo -n "6.3 GET /api/orders/$ORDER_ID (User)... "
    ORDER_DETAIL=$(curl -s -X GET "$BASE_URL/api/orders/$ORDER_ID" \
      -H "Authorization: Bearer $USER_TOKEN")
    if echo "$ORDER_DETAIL" | grep -q "id"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${RED}❌ ÉCHEC${NC}"
    fi
    pause
fi

# 6.4 Lister toutes les commandes (Admin)
echo -n "6.4 GET /api/admin/orders (Admin)... "
ADMIN_ORDERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/orders" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
if echo "$ADMIN_ORDERS_RESPONSE" | grep -q "id\|orderNumber\|\[\]"; then
    echo -e "${GREEN}✅ SUCCÈS${NC}"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi
pause

# 6.5 Modifier le statut d'une commande (Admin)
if [ ! -z "$ORDER_ID" ]; then
    echo -n "6.5 PUT /api/admin/orders/$ORDER_ID/status (Admin)... "
    UPDATE_ORDER_STATUS=$(curl -s -X PUT "$BASE_URL/api/admin/orders/$ORDER_ID/status" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{
        "status": "SHIPPED"
      }')
    
    if echo "$UPDATE_ORDER_STATUS" | grep -q "id\|SHIPPED"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${RED}❌ ÉCHEC${NC}"
    fi
    pause
fi

echo -e "\n${YELLOW}💳 7. TESTS DES PAIEMENTS${NC}\n"

# 7.1 Créer une session de paiement Stripe
if [ ! -z "$ORDER_ID" ]; then
    echo -n "7.1 POST /api/payments/create-session (User)... "
    CREATE_PAYMENT_SESSION=$(curl -s -X POST "$BASE_URL/api/payments/create-session" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $USER_TOKEN" \
      -d "{
        \"orderId\": $ORDER_ID,
        \"successUrl\": \"http://localhost:3000/payment/success\",
        \"cancelUrl\": \"http://localhost:3000/payment/cancel\"
      }")
    
    if echo "$CREATE_PAYMENT_SESSION" | grep -q "sessionId\|url"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${YELLOW}⚠️  PEUT NÉCESSITER STRIPE CONFIGURÉ${NC}"
        echo "   Réponse: $CREATE_PAYMENT_SESSION"
    fi
    pause
fi

echo -e "\n${YELLOW}🧹 8. TESTS DE NETTOYAGE (SUPPRESSION)${NC}\n"

# 8.1 Supprimer l'article du panier
if [ ! -z "$CART_ITEM_ID" ]; then
    echo -n "8.1 DELETE /api/cart/items/$CART_ITEM_ID (User)... "
    DELETE_CART_ITEM=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/api/cart/items/$CART_ITEM_ID" \
      -H "Authorization: Bearer $USER_TOKEN")
    
    if echo "$DELETE_CART_ITEM" | grep -q "200\|204"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${YELLOW}⚠️  Statut: $DELETE_CART_ITEM${NC}"
    fi
    pause
fi

# 8.2 Supprimer le produit de test
if [ ! -z "$NEW_PRODUCT_ID" ]; then
    echo -n "8.2 DELETE /api/admin/products/$NEW_PRODUCT_ID (Admin)... "
    DELETE_PRODUCT=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/api/admin/products/$NEW_PRODUCT_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if echo "$DELETE_PRODUCT" | grep -q "200\|204"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${YELLOW}⚠️  Statut: $DELETE_PRODUCT${NC}"
    fi
    pause
fi

# 8.3 Supprimer la catégorie de test
if [ ! -z "$NEW_CATEGORY_ID" ]; then
    echo -n "8.3 DELETE /api/admin/categories/$NEW_CATEGORY_ID (Admin)... "
    DELETE_CATEGORY=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/api/admin/categories/$NEW_CATEGORY_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if echo "$DELETE_CATEGORY" | grep -q "200\|204"; then
        echo -e "${GREEN}✅ SUCCÈS${NC}"
    else
        echo -e "${YELLOW}⚠️  Statut: $DELETE_CATEGORY${NC}"
    fi
    pause
fi

echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}✅ Tests terminés !${NC}"
echo -e "${BLUE}========================================${NC}\n"

echo -e "${GREEN}Résumé des tests :${NC}"
echo "- Authentification : Admin et User"
echo "- Catégories : CRUD complet"
echo "- Produits : CRUD complet"
echo "- Panier : Ajout, modification, suppression"
echo "- Adresses : Création et listage"
echo "- Commandes : Création, listage, modification de statut"
echo "- Paiements : Création de session Stripe"
echo ""
echo -e "${YELLOW}Note :${NC} Vérifiez les résultats ci-dessus pour voir les détails."
echo -e "${YELLOW}Tokens sauvegardés :${NC}"
echo "  ADMIN_TOKEN: ${ADMIN_TOKEN:0:30}..."
echo "  USER_TOKEN: ${USER_TOKEN:0:30}..."

