-- Wallet System Schema Migration

-- Wallets table
CREATE TABLE IF NOT EXISTS wallets (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR(100) NOT NULL,
  balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  total_top_up DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  total_spent DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  tenant_id VARCHAR(50),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  last_transaction_at TIMESTAMP,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_wallets_user_id UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallets_tenant_id ON wallets(tenant_id);

-- Wallet Transactions table
CREATE TABLE IF NOT EXISTS wallet_transactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  wallet_id BIGINT NOT NULL,
  user_id VARCHAR(100) NOT NULL,
  transaction_type VARCHAR(50) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  balance_before DECIMAL(10, 2) NOT NULL,
  balance_after DECIMAL(10, 2) NOT NULL,
  transaction_id VARCHAR(100) NOT NULL UNIQUE,
  status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',
  description VARCHAR(500),
  reference_id VARCHAR(100),
  reference_type VARCHAR(50),
  tenant_id VARCHAR(50),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wallet_txn_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_user_id ON wallet_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_type ON wallet_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_tenant_id ON wallet_transactions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_created ON wallet_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_transaction_id ON wallet_transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_reference ON wallet_transactions(reference_id, reference_type);

