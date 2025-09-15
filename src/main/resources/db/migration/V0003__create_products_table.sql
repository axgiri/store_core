-- Products table
CREATE TABLE IF NOT EXISTS products (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price NUMERIC(19,2) NOT NULL,
  category VARCHAR(100) NOT NULL,
  shop_id BIGINT NOT NULL
);

ALTER TABLE products
  ADD CONSTRAINT fk_products_shop
  FOREIGN KEY (shop_id) REFERENCES shops(id)
  ON DELETE CASCADE;

-- Element collections for products
CREATE TABLE IF NOT EXISTS product_tags (
  product_id BIGINT NOT NULL,
  tag VARCHAR(255) NOT NULL,
  PRIMARY KEY (product_id, tag),
  CONSTRAINT fk_product_tags_product
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_hidden_labels (
  product_id BIGINT NOT NULL,
  label VARCHAR(255) NOT NULL,
  PRIMARY KEY (product_id, label),
  CONSTRAINT fk_product_hidden_labels_product
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_attributes (
  product_id BIGINT NOT NULL,
  attr_key VARCHAR(255) NOT NULL,
  attr_value VARCHAR(1000),
  PRIMARY KEY (product_id, attr_key),
  CONSTRAINT fk_product_attributes_product
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE CASCADE
);

