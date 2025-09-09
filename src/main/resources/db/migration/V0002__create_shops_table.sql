-- Shops table
CREATE TABLE IF NOT EXISTS shops (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  name VARCHAR(255) NOT NULL,
  address VARCHAR(255),
  phone_number VARCHAR(255) NOT NULL,
  photo_header VARCHAR(255),
  description TEXT,
  -- category is currently modeled as a simple column by JPA (List without @ElementCollection)
  -- to match runtime expectations, we add a single column; values can be stored as string (e.g., enum name)
  category VARCHAR(100),
  -- category is Enum<List> in entity; using separate table for categories
  owner_id BIGINT NOT NULL
);

ALTER TABLE shops
  ADD CONSTRAINT fk_shops_owner
  FOREIGN KEY (owner_id) REFERENCES persons(id)
  ON DELETE CASCADE;

-- categories for shops (because entity uses List<CategoryEnum>)
CREATE TABLE IF NOT EXISTS shop_categories (
  shop_id BIGINT NOT NULL,
  category VARCHAR(100) NOT NULL,
  PRIMARY KEY (shop_id, category),
  CONSTRAINT fk_shop_categories_shop
    FOREIGN KEY (shop_id) REFERENCES shops(id)
    ON DELETE CASCADE
);
