-- 禾木商城数据库结构
-- MySQL 8.x / 8.4，字符集 utf8mb4
-- 执行顺序：先本文件，再 seed.sql

CREATE DATABASE IF NOT EXISTS mall_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE mall_db;

-- 用户。角色只允许 USER / ADMIN，密码只保存 BCrypt 哈希。
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  email VARCHAR(128) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  nickname VARCHAR(32) NOT NULL,
  phone VARCHAR(20) NULL,
  avatar VARCHAR(255) NULL,
  role VARCHAR(16) NOT NULL DEFAULT 'USER',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  token_version INT NOT NULL DEFAULT 0 COMMENT '退出登录或改密后递增，使旧 JWT 失效',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role_status (role, status),
  CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
  CONSTRAINT chk_users_status CHECK (status IN (0, 1))
) ENGINE=InnoDB COMMENT='用户账户';

-- 商品分类。被商品引用时不允许删除。
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(40) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_categories_name (name),
  KEY idx_categories_status_sort (status, sort_order),
  CONSTRAINT chk_categories_status CHECK (status IN (0, 1))
) ENGINE=InnoDB COMMENT='商品分类';

-- 商品。价格用 DECIMAL，库存和销量用整数。
CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL,
  subtitle VARCHAR(120) NULL,
  description VARCHAR(2000) NULL,
  price DECIMAL(10, 2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  sales INT NOT NULL DEFAULT 0,
  cover_image VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ON_SHELF',
  is_recommend TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_products_category_status (category_id, status),
  KEY idx_products_status_sales (status, sales),
  KEY idx_products_name (name),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT chk_products_price CHECK (price > 0),
  CONSTRAINT chk_products_stock CHECK (stock >= 0),
  CONSTRAINT chk_products_sales CHECK (sales >= 0),
  CONSTRAINT chk_products_status CHECK (status IN ('ON_SHELF', 'OFF_SHELF')),
  CONSTRAINT chk_products_recommend CHECK (is_recommend IN (0, 1))
) ENGINE=InnoDB COMMENT='商品';

-- 购物车。同一用户同一商品只保留一行。
CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_cart_user_product (user_id, product_id),
  KEY idx_cart_user (user_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT chk_cart_quantity CHECK (quantity BETWEEN 1 AND 99)
) ENGINE=InnoDB COMMENT='购物车明细';

-- 收货地址。
CREATE TABLE IF NOT EXISTS addresses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  receiver VARCHAR(32) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  province VARCHAR(32) NOT NULL,
  city VARCHAR(32) NOT NULL,
  district VARCHAR(32) NOT NULL,
  detail VARCHAR(200) NOT NULL,
  is_default TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_addresses_user (user_id, is_default),
  CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT chk_addresses_default CHECK (is_default IN (0, 1))
) ENGINE=InnoDB COMMENT='收货地址';

-- 订单主表。金额由后端按商品现价计算后写入，不信任前端总价。
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL,
  user_id BIGINT NOT NULL,
  address_id BIGINT NULL,
  receiver VARCHAR(32) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  full_address VARCHAR(300) NOT NULL,
  total_amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(24) NOT NULL,
  remark VARCHAR(200) NULL,
  idempotency_key VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_orders_no (order_no),
  UNIQUE KEY uk_orders_user_idem (user_id, idempotency_key),
  KEY idx_orders_user_created (user_id, created_at),
  KEY idx_orders_status_created (status, created_at),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT chk_orders_amount CHECK (total_amount >= 0),
  CONSTRAINT chk_orders_status CHECK (status IN ('PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB COMMENT='订单';

-- 订单商品快照。不建立到 products 的外键，商品删除后历史订单仍可查看。
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NULL,
  product_name VARCHAR(80) NOT NULL,
  product_image VARCHAR(255) NULL,
  unit_price DECIMAL(10, 2) NOT NULL,
  quantity INT NOT NULL,
  subtotal DECIMAL(10, 2) NOT NULL,
  KEY idx_order_items_order (order_id),
  KEY idx_order_items_product (product_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT chk_order_items_qty CHECK (quantity > 0),
  CONSTRAINT chk_order_items_price CHECK (unit_price >= 0 AND subtotal >= 0)
) ENGINE=InnoDB COMMENT='订单商品快照';

-- 库存流水。下单扣减、取消回滚、管理员调整都记在这里。
CREATE TABLE IF NOT EXISTS stock_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  change_qty INT NOT NULL,
  stock_after INT NOT NULL,
  biz_type VARCHAR(32) NOT NULL,
  ref_id BIGINT NULL,
  operator_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_stock_logs_product (product_id, created_at),
  CONSTRAINT fk_stock_logs_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT chk_stock_logs_after CHECK (stock_after >= 0)
) ENGINE=InnoDB COMMENT='库存流水';

-- 操作日志。禁止写入密码、令牌等敏感字段。
CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(32) NULL,
  module VARCHAR(32) NOT NULL,
  action VARCHAR(32) NOT NULL,
  detail VARCHAR(500) NULL,
  ip VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_operation_logs_created (created_at),
  KEY idx_operation_logs_user (user_id)
) ENGINE=InnoDB COMMENT='操作日志';
