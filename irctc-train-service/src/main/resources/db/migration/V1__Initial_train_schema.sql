-- Train Service Initial Schema Migration

-- Trains table
CREATE TABLE IF NOT EXISTS trains (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  train_number VARCHAR(20) NOT NULL UNIQUE,
  train_name VARCHAR(255) NOT NULL,
  source_station VARCHAR(100) NOT NULL,
  destination_station VARCHAR(100) NOT NULL,
  departure_time TIMESTAMP NOT NULL,
  arrival_time TIMESTAMP NOT NULL,
  train_type VARCHAR(50) NOT NULL,
  train_class VARCHAR(50) NOT NULL,
  base_fare DOUBLE PRECISION NOT NULL,
  total_seats INT NOT NULL,
  available_seats INT NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  route_description TEXT,
  distance INT NOT NULL,
  duration INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_train_number ON trains(train_number);
CREATE INDEX IF NOT EXISTS idx_train_route ON trains(source_station, destination_station);
CREATE INDEX IF NOT EXISTS idx_train_status ON trains(status);

-- Train amenities table
CREATE TABLE IF NOT EXISTS train_amenities (
  train_id BIGINT NOT NULL,
  amenity VARCHAR(100) NOT NULL,
  PRIMARY KEY (train_id, amenity),
  FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE
);

