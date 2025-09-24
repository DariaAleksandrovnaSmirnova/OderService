CREATE TABLE items (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    name varchar(128) NOT NULL,
    price decimal(10,2) NOT NULL
);

CREATE TABLE orders (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    user_id bigint NOT NULL,
    status varchar(64) NOT NULL,
    creation_date timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE order_items (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    order_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity integer NOT NULL,

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE RESTRICT
);

CREATE INDEX idx_items_name ON items(name);

INSERT INTO items (name, price) VALUES
('Ноутбук Lenovo IdeaPad', 899.99),
('Смартфон Samsung Galaxy S23', 799.99),
('Наушники Sony WH-1000XM4', 349.99),
('Планшет Apple iPad Air', 749.99),
('Мышь Logitech MX Master 3', 99.99);