DROP DATABASE IF EXISTS pan_puntos_premios;
CREATE DATABASE pan_puntos_premios;
USE pan_puntos_premios;

CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    identificador VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(100),
    puntos INT NOT NULL DEFAULT 0,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (puntos >= 0)
);

CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    categoria ENUM('SANDWICH', 'BEBIDA', 'ACOMPANAMIENTO', 'COMBO') NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    existencia INT NOT NULL DEFAULT 0,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CHECK (precio >= 0),
    CHECK (existencia >= 0)
);

CREATE TABLE combos (
    id_combo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

INSERT INTO combos (nombre, descripcion, precio) VALUES
('Combo 1', '3 panes, 3 acompañamientos, 3 bebidas', 0.00),
('Combo 2', '10 panes, 10 acompañamientos, 10 bebidas', 0.00),
('Combo 3', '5 panes, 5 acompañamientos, 5 bebidas', 0.00),
('Combo 4', '2 panes, 2 acompañamientos, 2 bebidas', 0.00),
('Combo 5', '1 pan, 1 bebida, 1 acompañamiento', 0.00);

CREATE TABLE menus (
    id_menu INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CHECK (precio >= 0)
);

CREATE TABLE menu_componentes (
    id_menu INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id_menu, id_producto),
    CONSTRAINT fk_componente_menu FOREIGN KEY (id_menu) REFERENCES menus(id_menu),
    CONSTRAINT fk_componente_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CHECK (cantidad > 0)
);

CREATE TABLE pedidos (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('PENDIENTE', 'PAGADO', 'ENTREGADO', 'ANULADO') NOT NULL DEFAULT 'PENDIENTE',
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CHECK (total >= 0)
);

CREATE TABLE pedido_detalles (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NULL,
    id_menu INT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CONSTRAINT fk_detalle_menu FOREIGN KEY (id_menu) REFERENCES menus(id_menu),
    CHECK (cantidad > 0),
    CHECK (precio_unitario >= 0),
    CHECK ((id_producto IS NOT NULL AND id_menu IS NULL) OR (id_producto IS NULL AND id_menu IS NOT NULL))
);

CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    metodo ENUM('EFECTIVO', 'TARJETA') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    cambio DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado ENUM('APROBADO', 'RECHAZADO') NOT NULL,
    referencia VARCHAR(100),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
    CHECK (monto >= 0),
    CHECK (cambio >= 0)
);

CREATE TABLE movimientos_inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    tipo ENUM('ENTRADA', 'VENTA', 'CANJE') NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255),
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CHECK (cantidad > 0)
);

CREATE TABLE movimientos_puntos (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_pedido INT NULL,
    tipo ENUM('ACUMULACION', 'CANJE') NOT NULL,
    puntos INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimiento_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT fk_movimiento_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
    CHECK (puntos > 0)
);


CREATE TABLE recompensas (
    id_recompensa INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    puntos_requeridos INT NOT NULL,
    id_producto INT NULL,
    id_menu INT NULL,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_recompensa_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CONSTRAINT fk_recompensa_menu FOREIGN KEY (id_menu) REFERENCES menus(id_menu),
    CHECK (puntos_requeridos > 0)
);

INSERT INTO recompensas (nombre, descripcion, puntos_requeridos, estado) VALUES
('Combo 1', 'Canje por 3 panes, 3 acompañamientos y 3 bebidas', 4000, 'ACTIVO'),
('Combo 2', 'Canje por 10 panes, 10 acompañamientos y 10 bebidas', 6500, 'ACTIVO'),
('Combo 3', 'Canje por 5 panes, 5 acompañamientos y 5 bebidas', 4500, 'ACTIVO'),
('Combo 4', 'Canje por 2 panes, 2 acompañamientos y 2 bebidas', 3500, 'ACTIVO'),
('Combo 5', 'Canje por 1 pan, 1 bebida y 1 acompañamiento', 2500, 'ACTIVO'),
('Bebida Extra', 'Canje por 1 bebida del catálogo', 1500, 'ACTIVO'),
('Acompañamiento Extra', 'Canje por 1 acompañamiento del catálogo', 1500, 'ACTIVO');

CREATE TABLE canjes (
    id_canje INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_recompensa INT NOT NULL,
    puntos_utilizados INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_canje_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT fk_canje_recompensa FOREIGN KEY (id_recompensa) REFERENCES recompensas(id_recompensa),
    CHECK (puntos_utilizados > 0)
);
