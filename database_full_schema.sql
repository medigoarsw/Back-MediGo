-- ╔════════════════════════════════════════════════════════════════════════════╗
-- ║                    MediGo DATABASE - Full Schema                           ║
-- ║                         Supabase PostgreSQL                                ║
-- ╚════════════════════════════════════════════════════════════════════════════╝

-- ═══════════════════════════════════════════════════════════════════════════════
-- 1. USUARIOS (Administradores, Afiliados, Repartidores)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    username        VARCHAR(100),                          -- Para compatibilidad
    role            VARCHAR(20)  NOT NULL DEFAULT 'AFFILIATE' CHECK (role IN ('ADMIN','AFFILIATE','DELIVERY')),
    active          BOOLEAN DEFAULT TRUE,
    phone           VARCHAR(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 2. SUCURSALES / LOCALES
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE branches (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    address         VARCHAR(500),
    city            VARCHAR(100),
    latitude        DECIMAL(10,7),
    longitude       DECIMAL(10,7),
    phone           VARCHAR(20),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 3. MEDICAMENTOS / CATÁLOGO
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE medications (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    unit            VARCHAR(50),                           -- "Caja x30", "Blíster x10", etc.
    active          BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 4. INVENTARIO POR SUCURSAL (Tabla crítica para concurrencia)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE branch_stock (
    id              BIGSERIAL PRIMARY KEY,
    branch_id       BIGINT NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    medication_id   BIGINT NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    quantity        INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    last_updated    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(branch_id, medication_id)
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 5. DIRECCIONES DE USUARIOS (Para órdenes y entregas)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label           VARCHAR(100),                          -- "Casa", "Trabajo", "Otro"
    street          VARCHAR(255) NOT NULL,
    city            VARCHAR(100),
    neighborhood    VARCHAR(100),
    postal_code     VARCHAR(20),
    latitude        DECIMAL(10,7),
    longitude       DECIMAL(10,7),
    is_default      BOOLEAN DEFAULT FALSE,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 6. ÓRDENES / PEDIDOS
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    affiliate_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    branch_id       BIGINT NOT NULL REFERENCES branches(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
                    CHECK (status IN ('PENDING','CONFIRMED','ASSIGNED','IN_ROUTE','DELIVERED','CANCELLED')),
    delivery_address_id BIGINT REFERENCES addresses(id),  -- Dirección de entrega
    address_lat     DECIMAL(10,7),                        -- Backup si no usa address_id
    address_lng     DECIMAL(10,7),
    total_amount    DECIMAL(12,2) DEFAULT 0,
    delivery_fee    DECIMAL(12,2) DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_at     TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 7. ITEMS DE ORDEN (Medicamentos en cada orden)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    medication_id   BIGINT NOT NULL REFERENCES medications(id),
    quantity        INT NOT NULL CHECK (quantity > 0),
    unit_price      DECIMAL(12,2) NOT NULL,
    subtotal        DECIMAL(12,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 8. ENTREGAS / LOGÍSTICA
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE deliveries (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    delivery_person_id BIGINT REFERENCES users(id),       -- FK a delivery person
    status          VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' 
                    CHECK (status IN ('ASSIGNED','IN_ROUTE','DELIVERED','FAILED')),
    estimated_time  TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    assigned_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at      TIMESTAMP,
    notes           TEXT,
    rating          DECIMAL(2,1) CHECK (rating >= 1 AND rating <= 5),
    review          TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 9. ACTUALIZACIONES DE UBICACIÓN (Para rastreo en tiempo real)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE location_updates (
    id              BIGSERIAL PRIMARY KEY,
    delivery_id     BIGINT NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    latitude        DECIMAL(10,7) NOT NULL,
    longitude       DECIMAL(10,7) NOT NULL,
    accuracy        DECIMAL(10,2),                         -- Precisión en metros
    timestamp       BIGINT NOT NULL,                       -- Unix timestamp
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 10. SUBASTAS
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE auctions (
    id              BIGSERIAL PRIMARY KEY,
    medication_id   BIGINT NOT NULL REFERENCES medications(id),
    branch_id       BIGINT NOT NULL REFERENCES branches(id),
    base_price      DECIMAL(12,2) NOT NULL,
    max_price       DECIMAL(12,2),                         -- Opcional: cierre por monto máximo
    inactivity_minutes INTEGER,                             -- Opcional: cierre por inactividad
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    last_bid_at     TIMESTAMP,                             -- Para calcular inactividad
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                    CHECK (status IN ('SCHEDULED','ACTIVE','CLOSED','CANCELLED')),
    closure_type    VARCHAR(20) DEFAULT 'FIXED_TIME'
                    CHECK (closure_type IN ('FIXED_TIME','INACTIVITY','MAX_PRICE')),
    winner_id       BIGINT REFERENCES users(id),
    final_price     DECIMAL(12,2),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 11. PUJAS / BIDS (Para subastas)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE bids (
    id              BIGSERIAL PRIMARY KEY,
    auction_id      BIGINT NOT NULL REFERENCES auctions(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    user_name       VARCHAR(255),                          -- Desnormalizado para rapidez
    amount          DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    placed_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 12. NOTIFICACIONES
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(50),                           -- 'ORDER_CONFIRMED', 'DELIVERY_STARTED', etc.
    title           VARCHAR(255) NOT NULL,
    message         TEXT,
    related_id      BIGINT,                                -- ID de orden, entrega, etc.
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 13. AUDIT LOG (Para rastrear cambios importantes)
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    action          VARCHAR(100),                          -- 'CREATE_ORDER', 'UPDATE_STATUS', etc.
    entity_type     VARCHAR(50),                           -- 'ORDER', 'DELIVERY', 'AUCTION'
    entity_id       BIGINT,
    old_value       JSONB,
    new_value       JSONB,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- ÍNDICES PARA PERFORMANCE
-- ═══════════════════════════════════════════════════════════════════════════════

-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- Branches
CREATE INDEX idx_branches_active ON branches(is_active);
CREATE INDEX idx_branches_city ON branches(city);

-- Medications
CREATE INDEX idx_medications_name ON medications(name);
CREATE INDEX idx_medications_active ON medications(active);

-- Branch Stock
CREATE INDEX idx_branch_stock_branch ON branch_stock(branch_id);
CREATE INDEX idx_branch_stock_medication ON branch_stock(medication_id);

-- Addresses
CREATE INDEX idx_addresses_user ON addresses(user_id);
CREATE INDEX idx_addresses_default ON addresses(user_id, is_default);

-- Orders
CREATE INDEX idx_orders_affiliate ON orders(affiliate_id);
CREATE INDEX idx_orders_branch ON orders(branch_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_affiliate_status ON orders(affiliate_id, status);

-- Order Items
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_medication ON order_items(medication_id);

-- Deliveries
CREATE INDEX idx_deliveries_order ON deliveries(order_id);
CREATE INDEX idx_deliveries_person ON deliveries(delivery_person_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at DESC);

-- Location Updates
CREATE INDEX idx_location_updates_delivery ON location_updates(delivery_id);
CREATE INDEX idx_location_updates_timestamp ON location_updates(timestamp DESC);

-- Auctions
CREATE INDEX idx_auctions_status ON auctions(status);
CREATE INDEX idx_auctions_branch ON auctions(branch_id);
CREATE INDEX idx_auctions_medication ON auctions(medication_id);
CREATE INDEX idx_auctions_start_time ON auctions(start_time DESC);
CREATE INDEX idx_auctions_end_time ON auctions(end_time);

-- Bids
CREATE INDEX idx_bids_auction ON bids(auction_id);
CREATE INDEX idx_bids_user ON bids(user_id);
CREATE INDEX idx_bids_auction_amount ON bids(auction_id, amount DESC);
CREATE INDEX idx_bids_placed_at ON bids(placed_at DESC);

-- Notifications
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Audit Logs
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - USUARIOS
-- ═══════════════════════════════════════════════════════════════════════════════
-- Password para todos: "password123" (hasheado con BCrypt strength 12)
-- Hash: $2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y

INSERT INTO users (email, password_hash, name, username, role, active, phone) VALUES
-- Administradores
('admin@medigo.com',        '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Admin MediGo',           'admin_medigo',      'ADMIN',     TRUE, '+57-1-2345678'),
('manager@medigo.com',      '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Manager Sistema',        'manager_sistema',   'ADMIN',     TRUE, '+57-1-9876543'),

-- Afiliados (Farmacias/Tiendas)
('farmacia.norte@medigo.com', '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Farmacia Norte',         'farmacia_norte',    'AFFILIATE', TRUE, '+57-300-1234567'),
('farmacia.sur@medigo.com',   '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Farmacia Sur',           'farmacia_sur',      'AFFILIATE', TRUE, '+57-301-2345678'),
('farmacia.centro@medigo.com','$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Farmacia Centro',        'farmacia_centro',   'AFFILIATE', TRUE, '+57-302-3456789'),
('cliente.juan@medigo.com',   '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Juan Pérez',             'juan_perez',        'AFFILIATE', TRUE, '+57-310-1111111'),
('cliente.maria@medigo.com',  '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'María García',           'maria_garcia',      'AFFILIATE', TRUE, '+57-311-2222222'),

-- Repartidores (Delivery)
('delivery.carlos@medigo.com',  '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Carlos López Repartidor', 'carlos_delivery',   'DELIVERY',  TRUE, '+57-320-3333333'),
('delivery.diego@medigo.com',   '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Diego Martínez',         'diego_delivery',    'DELIVERY',  TRUE, '+57-321-4444444'),
('delivery.ana@medigo.com',     '$2a$12$9aCCg8p4F.PV/OhC/yLMVu5X2JMrLEGTVWKxqT5T1eVzbSKzfKJ/y', 'Ana Rodríguez',          'ana_delivery',      'DELIVERY',  TRUE, '+57-322-5555555');

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - SUCURSALES
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO branches (name, address, city, latitude, longitude, phone, is_active) VALUES
('Sucursal Centro Bogotá',  'Carrera 7 #32-00, Centro',        'Bogotá',   4.6351, -74.0703, '+57-1-5000000', TRUE),
('Sucursal Norte Bogotá',   'Calle 100 #15-20, Usaquén',       'Bogotá',   4.6868, -74.0560, '+57-1-5000001', TRUE),
('Sucursal Sur Bogotá',     'Calle 45 #22-10, Puente Aranda',  'Bogotá',   4.6097, -74.0817, '+57-1-5000002', TRUE),
('Sucursal Occidente Bogotá','Calle 80 #12-15, Suba',          'Bogotá',   4.6888, -74.1177, '+57-1-5000003', TRUE),
('Sucursal Soacha',         'Carrera 9 #10-15',                'Soacha',   4.5803, -74.1673, '+57-1-8000000', TRUE);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - MEDICAMENTOS
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO medications (name, description, unit, active) VALUES
-- Analgésicos y Antiinflamatorios
('Ibuprofeno 400mg',         'Antiinflamatorio y analgésico para dolores moderados',                'Caja x30', TRUE),
('Paracetamol 500mg',        'Analgésico y antipirético para fiebre y dolor',                      'Caja x30', TRUE),
('Acetilsalicílico 500mg',   'Analgésico, antiinflamatorio y anticoagulante',                      'Caja x30', TRUE),
('Diclofenaco 50mg',         'Antiinflamatorio no esteroideo potente',                             'Caja x30', TRUE),

-- Antihipertensivos
('Losartán 50mg',            'Antagonista de angiotensina II para hipertensión',                   'Caja x30', TRUE),
('Enalapril 10mg',           'Inhibidor ACE para control de presión arterial',                     'Caja x30', TRUE),
('Amlodipino 5mg',           'Bloqueador de canales de calcio para hipertensión',                  'Caja x30', TRUE),

-- Antidiabéticos
('Metformina 850mg',         'Biguanida para diabetes tipo 2',                                     'Caja x30', TRUE),
('Glibenclamida 5mg',        'Sulfonilurea para control de glucosa',                               'Caja x30', TRUE),

-- Antibióticos
('Amoxicilina 500mg',        'Antibiótico beta-lactámico de amplio espectro',                      'Caja x20', TRUE),
('Azitromicina 500mg',       'Antibiótico macrólido para infecciones respiratorias',               'Caja x10', TRUE),
('Ciprofloxacino 500mg',     'Fluoroquinolona para infecciones del tracto urinario',               'Caja x10', TRUE),

-- Medicamentos para el corazón
('Atorvastatina 20mg',       'Estatina para reducción de colesterol',                              'Caja x30', TRUE),
('Propranolol 40mg',         'Beta-bloqueador para arritmias e hipertensión',                      'Caja x30', TRUE),

-- Otros
('Omeprazol 20mg',           'Inhibidor de bomba de protones para gastritis',                      'Caja x30', TRUE),
('Loratadina 10mg',          'Antihistamínico para alergias',                                      'Caja x30', TRUE),
('Vitamina C 500mg',         'Suplemento vitamínico para deficiencias',                            'Caja x30', TRUE);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - INVENTARIO (Branch Stock)
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO branch_stock (branch_id, medication_id, quantity) VALUES
-- Sucursal Centro
(1, 1, 120), (1, 2, 100), (1, 3, 80),  (1, 4, 60),  (1, 5, 150),
(1, 6, 100), (1, 7, 90),  (1, 8, 140), (1, 9, 75),  (1, 10, 200),
(1, 11, 85), (1, 12, 110), (1, 13, 95), (1, 14, 130), (1, 15, 200),

-- Sucursal Norte
(2, 1, 100), (2, 2, 90),  (2, 3, 70),  (2, 4, 50),  (2, 5, 130),
(2, 6, 80),  (2, 7, 75),  (2, 8, 120), (2, 9, 65),  (2, 10, 180),
(2, 11, 70), (2, 12, 95),  (2, 13, 85), (2, 14, 110), (2, 15, 180),

-- Sucursal Sur
(3, 1, 110), (3, 2, 95),  (3, 3, 75),  (3, 4, 55),  (3, 5, 140),
(3, 6, 90),  (3, 7, 85),  (3, 8, 130), (3, 9, 70),  (3, 10, 190),
(3, 11, 80), (3, 12, 100), (3, 13, 90), (3, 14, 120), (3, 15, 190),

-- Sucursal Occidente
(4, 1, 105), (4, 2, 92),  (4, 3, 72),  (4, 4, 52),  (4, 5, 135),
(4, 6, 85),  (4, 7, 80),  (4, 8, 125), (4, 9, 67),  (4, 10, 185),
(4, 11, 75), (4, 12, 97),  (4, 13, 87), (4, 14, 115), (4, 15, 185),

-- Sucursal Soacha
(5, 1, 90),  (5, 2, 80),  (5, 3, 65),  (5, 4, 45),  (5, 5, 120),
(5, 6, 70),  (5, 7, 65),  (5, 8, 110), (5, 9, 55),  (5, 10, 160),
(5, 11, 60), (5, 12, 85),  (5, 13, 75), (5, 14, 100), (5, 15, 160);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - DIRECCIONES DE USUARIOS
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO addresses (user_id, label, street, city, neighborhood, postal_code, latitude, longitude, is_default, is_active) VALUES
-- Direcciones de Juan Pérez (cliente)
(6, 'Casa', 'Calle 50 #12-34', 'Bogotá', 'Chapinero', '110111', 4.6452, -74.0505, TRUE, TRUE),
(6, 'Trabajo', 'Carrera 9 #72-50', 'Bogotá', 'Suba', '110111', 4.6888, -74.1177, FALSE, TRUE),

-- Direcciones de María García (cliente)
(7, 'Casa', 'Calle 120 #8-15', 'Bogotá', 'Usaquén', '110111', 4.7000, -74.0550, TRUE, TRUE),
(7, 'Casa de padres', 'Carrera 5 #45-20', 'Bogotá', 'La Candelaria', '110111', 4.6290, -74.0756, FALSE, TRUE);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - ÓRDENES
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO orders (affiliate_id, branch_id, status, delivery_address_id, address_lat, address_lng, total_amount, delivery_fee, notes, created_at, delivery_at) VALUES
-- Órdenes entregadas
(6, 1, 'DELIVERED', 1, 4.6452, -74.0505, 125.50, 5.00, 'Ordenado para dolor de cabeza', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days 23 hours'),
(7, 2, 'DELIVERED', 3, 4.7000, -74.0550, 89.75, 5.00, 'Medicinas para gripa', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 23 hours'),

-- Órdenes en tránsito
(6, 1, 'IN_ROUTE', 1, 4.6452, -74.0505, 156.25, 5.00, 'Urgente', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours'),
(7, 2, 'IN_ROUTE', 3, 4.7000, -74.0550, 203.00, 5.00, '', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '4 hours'),

-- Órdenes confirmadas
(6, 3, 'CONFIRMED', 1, 4.6452, -74.0505, 95.00, 5.00, 'Para la presión', NOW() - INTERVAL '2 hours', NULL),
(7, 1, 'CONFIRMED', 3, 4.7000, -74.0550, 150.50, 5.00, '', NOW() - INTERVAL '1 hour', NULL);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - ITEMS DE ORDEN
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO order_items (order_id, medication_id, quantity, unit_price) VALUES
-- Orden 1
(1, 1, 2, 15.50),   -- 2x Ibuprofeno
(1, 2, 1, 12.75),   -- 1x Paracetamol

-- Orden 2
(2, 10, 1, 45.00),  -- 1x Amoxicilina
(2, 15, 1, 44.75),  -- 1x Vitamina C

-- Orden 3
(3, 5, 1, 78.50),   -- 1x Losartán
(3, 13, 2, 39.87),  -- 2x Loratadina

-- Orden 4
(4, 8, 1, 98.00),   -- 1x Metformina
(4, 14, 1, 105.00), -- 1x Atorvastatina

-- Orden 5
(5, 5, 1, 47.50),   -- 1x Losartán
(5, 6, 1, 47.50),   -- 1x Enalapril

-- Orden 6
(6, 3, 2, 25.00),   -- 2x Acetilsalicílico
(6, 12, 1, 100.50); -- 1x Omeprazol

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - ENTREGAS
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO deliveries (order_id, delivery_person_id, status, estimated_time, actual_delivery_time, assigned_at, started_at, notes, rating, review) VALUES
-- Entregas completadas
(1, 8, 'DELIVERED', NOW() - INTERVAL '4 days 22 hours', NOW() - INTERVAL '4 days 23 hours', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days 23 hours 30 minutes', 'Entregado sin problemas', 5.0, 'Excelente servicio, puntual'),
(2, 9, 'DELIVERED', NOW() - INTERVAL '2 days 22 hours', NOW() - INTERVAL '2 days 23 hours', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 23 hours 30 minutes', 'Puerta principal', 4.5, 'Fue rápido, muy bien'),

-- Entregas en ruta
(3, 8, 'IN_ROUTE', NOW() + INTERVAL '1 hour', NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day 8 hours', 'Salir inmediatamente', NULL, NULL),
(4, 9, 'IN_ROUTE', NOW() + INTERVAL '2 hours', NULL, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours 15 minutes', '', NULL, NULL),

-- Entregas asignadas
(5, 7, 'ASSIGNED', NOW() + INTERVAL '3 hours', NULL, NOW() - INTERVAL '2 hours', NULL, 'Entrega matutina preferible', NULL, NULL),
(6, 8, 'ASSIGNED', NOW() + INTERVAL '2 hours 30 minutes', NULL, NOW() - INTERVAL '1 hour', NULL, '', NULL, NULL);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - ACTUALIZACIONES DE UBICACIÓN
-- ═══════════════════════════════════════════════════════════════════════════════
-- Nota: Los timestamps son en milisegundos desde epoch
INSERT INTO location_updates (delivery_id, latitude, longitude, accuracy, timestamp) VALUES
-- Entregas completadas (histórico)
(1, 4.6351, -74.0703, 15.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '4 days 23 hours 30 minutes' - INTERVAL '10 minutes'))::BIGINT * 1000),
(1, 4.6380, -74.0680, 12.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '4 days 23 hours 30 minutes' - INTERVAL '5 minutes'))::BIGINT * 1000),
(1, 4.6452, -74.0505, 8.0,  EXTRACT(EPOCH FROM (NOW() - INTERVAL '4 days 23 hours 30 minutes'))::BIGINT * 1000),

(2, 4.6868, -74.0560, 15.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '2 days 23 hours 30 minutes' - INTERVAL '10 minutes'))::BIGINT * 1000),
(2, 4.7000, -74.0550, 10.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '2 days 23 hours 30 minutes'))::BIGINT * 1000),

-- Entregas en ruta (actualizaciones recientes)
(3, 4.6351, -74.0703, 12.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '8 hours'))::BIGINT * 1000),
(3, 4.6400, -74.0680, 10.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '6 hours'))::BIGINT * 1000),
(3, 4.6452, -74.0550, 8.0,  EXTRACT(EPOCH FROM (NOW() - INTERVAL '2 hours'))::BIGINT * 1000),

(4, 4.6868, -74.0560, 15.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '4 hours'))::BIGINT * 1000),
(4, 4.6900, -74.0580, 12.0, EXTRACT(EPOCH FROM (NOW() - INTERVAL '2 hours'))::BIGINT * 1000);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - SUBASTAS
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO auctions (medication_id, branch_id, base_price, max_price, inactivity_minutes, start_time, end_time, last_bid_at, status, closure_type, winner_id, final_price) VALUES
-- Subastas cerradas (completadas)
(1, 1, 10.00, 50.00, 15, NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days 22 hours', NOW() - INTERVAL '9 days 22 hours 30 minutes', 'CLOSED', 'MAX_PRICE', 6, 49.50),
(8, 2, 85.00, 100.00, NULL, NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days 20 hours', NOW() - INTERVAL '6 days 20 hours 45 minutes', 'CLOSED', 'FIXED_TIME', 7, 96.25),

-- Subastas activas
(5, 1, 40.00, NULL, 20, NOW() - INTERVAL '2 days', NOW() + INTERVAL '5 days', NOW() - INTERVAL '30 minutes', 'ACTIVE', 'FIXED_TIME', NULL, NULL),
(10, 3, 35.00, 80.00, 15, NOW() - INTERVAL '1 day', NOW() + INTERVAL '2 days', NOW() - INTERVAL '1 hour', 'ACTIVE', 'MAX_PRICE', NULL, NULL),

-- Subastas programadas (futuras)
(2, 1, 12.00, NULL, 25, NOW() + INTERVAL '2 days', NOW() + INTERVAL '4 days', NULL, 'SCHEDULED', 'FIXED_TIME', NULL, NULL),
(14, 2, 95.00, 120.00, 10, NOW() + INTERVAL '5 days', NOW() + INTERVAL '7 days', NULL, 'SCHEDULED', 'INACTIVITY', NULL, NULL);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - PUJAS (BIDS)
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO bids (auction_id, user_id, user_name, amount, placed_at) VALUES
-- Pujas para subasta 1 (cerrada)
(1, 6, 'Juan Pérez', 15.00, NOW() - INTERVAL '10 days 1 hour'),
(1, 7, 'María García', 25.00, NOW() - INTERVAL '10 days 45 minutes'),
(1, 3, 'Farmacia Norte', 35.00, NOW() - INTERVAL '10 days 30 minutes'),
(1, 6, 'Juan Pérez', 45.00, NOW() - INTERVAL '10 days 15 minutes'),
(1, 3, 'Farmacia Norte', 49.50, NOW() - INTERVAL '9 days 23 hours'),

-- Pujas para subasta 2 (cerrada)
(2, 7, 'María García', 92.00, NOW() - INTERVAL '7 days 1 hour'),
(2, 4, 'Farmacia Sur', 94.00, NOW() - INTERVAL '6 days 22 hours'),
(2, 7, 'María García', 96.25, NOW() - INTERVAL '6 days 20 hours 45 minutes'),

-- Pujas para subasta 3 (activa)
(3, 6, 'Juan Pérez', 45.00, NOW() - INTERVAL '2 days'),
(3, 4, 'Farmacia Sur', 55.00, NOW() - INTERVAL '1 day 12 hours'),
(3, 3, 'Farmacia Norte', 65.00, NOW() - INTERVAL '18 hours'),

-- Pujas para subasta 4 (activa)
(4, 7, 'María García', 40.00, NOW() - INTERVAL '1 day'),
(4, 5, 'Farmacia Centro', 50.00, NOW() - INTERVAL '12 hours'),
(4, 6, 'Juan Pérez', 65.00, NOW() - INTERVAL '1 hour');

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA - NOTIFICACIONES
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO notifications (user_id, type, title, message, related_id, is_read) VALUES
-- Notificaciones para Juan Pérez
(6, 'ORDER_CONFIRMED', 'Orden confirmada', 'Tu orden #1 ha sido confirmada', 5, TRUE),
(6, 'DELIVERY_STARTED', 'Entrega iniciada', 'Tu entrega #3 está en camino', 3, FALSE),
(6, 'ORDER_DELIVERED', 'Orden entregada', 'Tu orden #1 ha sido entregada', 1, TRUE),

-- Notificaciones para María García
(7, 'ORDER_CONFIRMED', 'Orden confirmada', 'Tu orden #6 ha sido confirmada', 6, TRUE),
(7, 'DELIVERY_STARTED', 'Entrega iniciada', 'Tu entrega #4 está en camino', 4, FALSE),
(7, 'AUCTION_WINNER', '¡Ganaste la subasta!', 'Felicidades, ganaste la subasta #1 con $49.50', 1, TRUE),

-- Notificaciones para afiliados
(3, 'NEW_BID', 'Nueva puja', 'Una nueva puja fue realizada en tu subasta', 3, TRUE),
(4, 'AUCTION_CLOSING', 'Subasta por cerrar', 'Tu subasta cerrará en 1 hora', 4, FALSE);

-- ═════════════════════════════════════════════════════════════════════════════════
-- VERIFICACIÓN FINAL
-- ═════════════════════════════════════════════════════════════════════════════════
-- Ejecuta estas consultas para verificar que todo se creó correctamente:

SELECT 'Users created' AS check_point, COUNT(*) as total FROM users;
SELECT 'Branches created', COUNT(*) FROM branches;
SELECT 'Medications created', COUNT(*) FROM medications;
SELECT 'Branch stock created', COUNT(*) FROM branch_stock;
SELECT 'Addresses created', COUNT(*) FROM addresses;
SELECT 'Orders created', COUNT(*) FROM orders;
SELECT 'Order items created', COUNT(*) FROM order_items;
SELECT 'Deliveries created', COUNT(*) FROM deliveries;
SELECT 'Location updates created', COUNT(*) FROM location_updates;
SELECT 'Auctions created', COUNT(*) FROM auctions;
SELECT 'Bids created', COUNT(*) FROM bids;
SELECT 'Notifications created', COUNT(*) FROM notifications;
