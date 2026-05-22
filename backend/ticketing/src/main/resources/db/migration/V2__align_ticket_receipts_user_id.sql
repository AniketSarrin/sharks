-- Align receipts with JWT subject (UUID). Existing bigint user IDs cannot be mapped to real users.
TRUNCATE TABLE ticket_receipts RESTART IDENTITY;

ALTER TABLE ticket_receipts DROP COLUMN user_id;
ALTER TABLE ticket_receipts ADD COLUMN user_id UUID NOT NULL;

ALTER TABLE ticket_receipts ADD COLUMN purchased_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE ticket_receipts ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1;
ALTER TABLE ticket_receipts ADD CONSTRAINT chk_ticket_receipts_quantity_positive CHECK (quantity >= 1);
ALTER TABLE ticket_receipts ADD COLUMN ticket_code VARCHAR(64) NOT NULL;

CREATE UNIQUE INDEX uq_ticket_receipts_ticket_code ON ticket_receipts (ticket_code);
