-- Migration V2: Insertion des données par défaut
-- Créée le: 2025-10-28

-- Insertion de l'administrateur par défaut
-- Mot de passe: admin123 (hashé avec BCrypt)
INSERT INTO users (name, email, password, role, status) VALUES
('Admin MaliStore', 'admin@malistore.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ADMIN', 'ACTIVE');

-- Insertion des 5 catégories par défaut
INSERT INTO categories (name, description, image_url, active, sort_order) VALUES
('Électronique et Gadgets', 'Appareils électroniques, gadgets et accessoires tech', 'https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400', true, 1),
('Vêtements et Mode', 'Vêtements pour hommes, femmes et enfants', 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400', true, 2),
('Maison et Jardin', 'Décoration, mobilier et articles pour la maison', 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=400', true, 3),
('Sports et Loisirs', 'Équipements sportifs et articles de loisirs', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400', true, 4),
('Livres et Médias', 'Livres, films, musique et jeux', 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400', true, 5);

-- Insertion d'un produit par catégorie
INSERT INTO products (name, description, price, image_url, stock, active, category_id) VALUES
-- Produit Électronique
('iPhone 15 Pro Max', 'Le dernier smartphone Apple avec écran Super Retina XDR de 6,7 pouces, puce A17 Pro et système de caméra Pro avancé.', 1299.99, 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=400', 25, true, 1),

-- Produit Vêtements
('T-shirt Premium Cotton', 'T-shirt en coton bio de haute qualité, confortable et durable. Disponible en plusieurs couleurs.', 29.99, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400', 50, true, 2),

-- Produit Maison
('Lampadaire Design Moderne', 'Lampadaire élégant avec abat-jour en tissu et structure en métal. Parfait pour éclairer votre salon.', 89.99, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400', 15, true, 3),

-- Produit Sports
('Raquette de Tennis Professionnelle', 'Raquette de tennis haute performance avec cadre en graphite et cordage pré-tendu. Idéale pour les joueurs expérimentés.', 199.99, 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400', 30, true, 4),

-- Produit Livres
('Livre "Le Guide du Développeur"', 'Guide complet pour apprendre le développement web moderne avec les dernières technologies et bonnes pratiques.', 39.99, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400', 100, true, 5);

-- Insertion des images pour chaque produit
INSERT INTO product_images (product_id, filename, image_url, alt_text, is_primary, sort_order) VALUES
-- Images pour iPhone 15 Pro Max
(1, 'iphone-15-pro-max-front.jpg', 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=400', 'iPhone 15 Pro Max - Vue de face', true, 0),
(1, 'iphone-15-pro-max-back.jpg', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400', 'iPhone 15 Pro Max - Vue arrière', false, 1),

-- Images pour T-shirt Premium
(2, 'tshirt-premium-front.jpg', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400', 'T-shirt Premium - Vue de face', true, 0),
(2, 'tshirt-premium-back.jpg', 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=400', 'T-shirt Premium - Vue arrière', false, 1),

-- Images pour Lampadaire
(3, 'lampadaire-design-1.jpg', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400', 'Lampadaire Design - Vue d ensemble', true, 0),
(3, 'lampadaire-design-2.jpg', 'https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=400', 'Lampadaire Design - Détail', false, 1),

-- Images pour Raquette de Tennis
(4, 'raquette-tennis-1.jpg', 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400', 'Raquette de Tennis - Vue principale', true, 0),
(4, 'raquette-tennis-2.jpg', 'https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=400', 'Raquette de Tennis - Détail du cordage', false, 1),

-- Images pour Livre
(5, 'livre-developpeur-1.jpg', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400', 'Livre Guide du Développeur - Couverture', true, 0),
(5, 'livre-developpeur-2.jpg', 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400', 'Livre Guide du Développeur - Pages intérieures', false, 1);
