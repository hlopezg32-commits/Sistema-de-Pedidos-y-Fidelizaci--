DROP DATABASE IF EXISTS pan_puntos_premios;
CREATE DATABASE pan_puntos_premios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
) ENGINE=InnoDB;

CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    categoria ENUM('SANDWICH', 'BEBIDA', 'ACOMPANAMIENTO') NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    existencia INT NOT NULL DEFAULT 0,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CHECK (precio >= 0),
    CHECK (existencia >= 0)
) ENGINE=InnoDB;

CREATE TABLE menus (
    id_menu INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE menu_componentes (
    id_menu INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id_menu, id_producto),
    CONSTRAINT fk_componente_menu FOREIGN KEY (id_menu) REFERENCES menus(id_menu) ON DELETE CASCADE,
    CONSTRAINT fk_componente_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE TABLE pedidos (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('PENDIENTE', 'PAGADO', 'ENTREGADO', 'ANULADO') NOT NULL DEFAULT 'PENDIENTE',
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CHECK (total >= 0)
) ENGINE=InnoDB;

CREATE TABLE pedido_detalles (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NULL,
    id_menu INT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CONSTRAINT fk_detalle_menu FOREIGN KEY (id_menu) REFERENCES menus(id_menu),
    CHECK (cantidad > 0),
    CHECK (precio_unitario >= 0),
    CHECK ((id_producto IS NOT NULL AND id_menu IS NULL) OR (id_producto IS NULL AND id_menu IS NOT NULL))
) ENGINE=InnoDB;

CREATE TABLE pedido_detalle_extras (
    id_detalle_extra INT AUTO_INCREMENT PRIMARY KEY,
    id_detalle INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_extra_detalle FOREIGN KEY (id_detalle) REFERENCES pedido_detalles(id_detalle) ON DELETE CASCADE,
    CONSTRAINT fk_extra_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CHECK (cantidad > 0),
    CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

CREATE TABLE movimientos_inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    tipo ENUM('ENTRADA', 'VENTA', 'CANJE') NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255),
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CHECK (cantidad > 0)
) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

CREATE TABLE recompensas (
    id_recompensa INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    costo_puntos INT NOT NULL,
    id_producto INT NULL,
    id_menu INT NULL,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_recompensa_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    CONSTRAINT fk_recompensa_menu FOREIGN KEY (id_menu) REFERENCES menus(id_menu),
    CHECK (costo_puntos > 0)
) ENGINE=InnoDB;

CREATE TABLE canjes (
    id_canje INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_recompensa INT NOT NULL,
    puntos_utilizados INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_canje_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT fk_canje_recompensa FOREIGN KEY (id_recompensa) REFERENCES recompensas(id_recompensa),
    CHECK (puntos_utilizados > 0)
) ENGINE=InnoDB;

INSERT INTO clientes (identificador, nombre, telefono, correo, puntos, estado) VALUES
('CL-001', 'Juan Carlos Perez', '55551234', 'juan.perez@correo.com', 1316, 'ACTIVO'),
('CL-002', 'Maria Fernanda Gomez', '55552345', 'maria.gomez@correo.com', 45, 'ACTIVO'),
('CL-003', 'Carlos Eduardo Lopez', '55553456', 'carlos.lopez@correo.com', 20, 'ACTIVO'),
('CL-004', 'Ana Lucia Morales', '55554567', 'ana.morales@correo.com', 0, 'ACTIVO');

INSERT INTO productos (codigo, nombre, categoria, precio, existencia, estado) VALUES
('SAN001', 'Sub de Jamon 15cm', 'SANDWICH', 28.00, 45, 'ACTIVO'),
('SAN002', 'Sub de Pavo y Jamon 15cm', 'SANDWICH', 34.00, 30, 'ACTIVO'),
('SAN003', 'Sub de Pollo Teriyaki 15cm', 'SANDWICH', 38.00, 25, 'ACTIVO'),
('BEB001', 'Gaseosa 500ml', 'BEBIDA', 10.00, 60, 'ACTIVO'),
('BEB002', 'Te Frio de Limon 500ml', 'BEBIDA', 12.00, 50, 'ACTIVO'),
('BEB003', 'Agua Pura 600ml', 'BEBIDA', 8.00, 55, 'ACTIVO'),
('ACO001', 'Papas Fritas Medianas', 'ACOMPANAMIENTO', 14.00, 40, 'ACTIVO'),
('ACO002', 'Galleta con Chispas', 'ACOMPANAMIENTO', 8.00, 60, 'ACTIVO');

INSERT INTO menus (codigo, nombre, precio, estado) VALUES
('MEN001', 'Menu Clasico Jamon', 45.00, 'ACTIVO'),
('MEN002', 'Menu Pollo Teriyaki', 55.00, 'ACTIVO');

INSERT INTO menu_componentes (id_menu, id_producto, cantidad) VALUES
(1, 1, 1),
(1, 4, 1),
(1, 7, 1),
(2, 3, 1),
(2, 5, 1),
(2, 8, 1);

INSERT INTO recompensas (nombre, costo_puntos, id_producto, id_menu, estado) VALUES
('Bebida Gratuita Refresco 500ml', 10, 4, NULL, 'ACTIVO'),
('Acompanamiento Gratuito Papas Fritas', 15, 7, NULL, 'ACTIVO'),
('Sandwich Gratuito Sub Jamon', 30, 1, NULL, 'ACTIVO'),
('Menu Gratuito Sub Jamon Completo', 50, NULL, 1, 'ACTIVO');
