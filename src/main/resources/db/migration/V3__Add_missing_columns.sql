-- Migration V3: Ajout de colonnes manquantes pour les paiements
-- Note: Toutes les autres colonnes sont déjà définies dans V1

-- Add columns to payments table (seulement les colonnes qui n'existent pas dans V1)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS stripe_payment_intent_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS webhook_received_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(1000);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS description VARCHAR(500);



