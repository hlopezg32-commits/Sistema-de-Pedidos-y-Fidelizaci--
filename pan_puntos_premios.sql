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

INSERT INTO clientes (identificador, nombre, telefono, correo, puntos, estado, fecha_registro) VALUES
('12345678-9', 'Juan Carlos Perez', '55551234', 'juan.perez@correo.com', 1316, 'ACTIVO', '2026-08-15 10:00:00'),
('87654321-0', 'Maria Fernanda Gomez', '55552345', 'maria.gomez@correo.com', 240, 'ACTIVO', '2026-08-20 11:30:00'),
('2548123450101', 'Carlos Eduardo Lopez', '55553456', 'carlos.lopez@correo.com', 85, 'ACTIVO', '2026-09-01 09:15:00'),
('45678912-3', 'Ana Lucia Morales', '55554567', 'ana.morales@correo.com', 52, 'ACTIVO', '2026-09-10 14:20:00'),
('1892345670101', 'Roberto Alejandro Soto', '55555678', 'roberto.soto@correo.com', 38, 'ACTIVO', '2026-09-22 16:45:00'),
('98765432-1', 'Sofia Isabel Martinez', '55556789', 'sofia.martinez@correo.com', 18, 'ACTIVO', '2026-10-02 12:10:00'),
('3012456780101', 'Diego Fernando Castillo', '55557890', 'diego.castillo@correo.com', 12, 'ACTIVO', '2026-10-12 18:30:00'),
('54321678-K', 'Jimena Alejandra Valdez', '55558901', 'jimena.valdez@correo.com', 0, 'ACTIVO', '2026-10-25 15:00:00'),
('2145678900101', 'Cesar Augusto Ramirez', '55559012', 'cesar.ramirez@correo.com', 320, 'ACTIVO', '2026-10-28 17:15:00'),
('CF', 'Consumidor Final', '00000000', 'cf@subnotway.com', 0, 'INACTIVO', '2026-11-01 08:00:00');

INSERT INTO productos (codigo, nombre, categoria, precio, existencia, estado) VALUES
('SAN001', 'Sandwich de ensalada de atun', 'SANDWICH', 75.00, 50, 'ACTIVO'),
('SAN002', 'Cheese party', 'SANDWICH', 80.00, 40, 'ACTIVO'),
('SAN003', 'Pork Jhon', 'SANDWICH', 95.00, 2, 'ACTIVO'),
('SAN004', 'Sandwich de chompipe', 'SANDWICH', 75.00, 35, 'ACTIVO'),
('SAN005', 'Pollo al granjero', 'SANDWICH', 55.00, 45, 'ACTIVO'),
('SAN006', 'Ensalada panizada', 'SANDWICH', 65.00, 30, 'ACTIVO'),
('BEB001', 'Monster', 'BEBIDA', 20.00, 60, 'ACTIVO'),
('BEB002', 'Monster blanca', 'BEBIDA', 20.00, 45, 'ACTIVO'),
('BEB003', 'Monster rosa', 'BEBIDA', 20.00, 0, 'ACTIVO'),
('BEB004', 'Monster tropical', 'BEBIDA', 20.00, 35, 'ACTIVO'),
('BEB005', 'Monster verde', 'BEBIDA', 20.00, 40, 'ACTIVO'),
('BEB006', 'Tampico', 'BEBIDA', 10.00, 50, 'ACTIVO'),
('BEB007', 'Agua pura', 'BEBIDA', 8.00, 80, 'ACTIVO'),
('BEB008', 'Pepsi', 'BEBIDA', 12.00, 75, 'ACTIVO'),
('BEB009', 'Cocacola', 'BEBIDA', 12.00, 90, 'ACTIVO'),
('BEB010', 'Horchata', 'BEBIDA', 15.00, 25, 'ACTIVO'),
('ACO001', 'Doritos', 'ACOMPANAMIENTO', 8.00, 70, 'ACTIVO'),
('ACO002', 'Cheetos', 'ACOMPANAMIENTO', 8.00, 55, 'ACTIVO'),
('ACO003', 'Cheetos flaming hot', 'ACOMPANAMIENTO', 8.00, 4, 'ACTIVO'),
('ACO004', 'Lays originales', 'ACOMPANAMIENTO', 8.00, 65, 'ACTIVO'),
('ACO005', 'Lyas crema y especias', 'ACOMPANAMIENTO', 8.00, 40, 'ACTIVO'),
('ACO006', 'Lays de pollo campero', 'ACOMPANAMIENTO', 8.00, 35, 'ACTIVO'),
('ACO007', 'Doritos verdes', 'ACOMPANAMIENTO', 8.00, 45, 'ACTIVO'),
('ACO008', 'Doritos incognita', 'ACOMPANAMIENTO', 8.00, 3, 'ACTIVO'),
('ACO009', 'Tortrix barbacoa', 'ACOMPANAMIENTO', 5.00, 80, 'ACTIVO'),
('ACO010', 'Tortrix cobanero', 'ACOMPANAMIENTO', 5.00, 60, 'ACTIVO'),
('ACO011', 'Tortrix de salsa criolla', 'ACOMPANAMIENTO', 5.00, 50, 'ACTIVO'),
('ACO012', 'Galleta de queso con frambuesa', 'ACOMPANAMIENTO', 12.00, 5, 'ACTIVO'),
('ACO013', 'Coockie chocolate explosion', 'ACOMPANAMIENTO', 12.00, 40, 'ACTIVO');

INSERT INTO menus (codigo, nombre, precio, estado) VALUES
('MEN001', 'Combo 1: 3 Panes, 3 Acompanamientos y 3 Bebidas', 230.00, 'ACTIVO'),
('MEN002', 'Combo 2: 10 Panes, 10 Acompanamientos y 10 Bebidas', 720.00, 'ACTIVO'),
('MEN003', 'Combo 3: 5 Panes, 5 Acompanamientos y 5 Bebidas', 375.00, 'ACTIVO'),
('MEN004', 'Combo 4: 2 Panes, 2 Acompanamientos y 2 Bebidas', 160.00, 'ACTIVO'),
('MEN005', 'Combo 5: 1 Pan, 1 Bebida y 1 Acompanamiento', 85.00, 'ACTIVO');

INSERT INTO menu_componentes (id_menu, id_producto, cantidad) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 5, 1),
(1, 7, 1),
(1, 14, 1),
(1, 15, 1),
(1, 17, 1),
(1, 20, 1),
(1, 29, 1),
(2, 1, 3),
(2, 2, 2),
(2, 4, 2),
(2, 5, 3),
(2, 7, 2),
(2, 12, 2),
(2, 14, 3),
(2, 15, 3),
(2, 17, 3),
(2, 18, 2),
(2, 25, 3),
(2, 26, 2),
(3, 1, 1),
(3, 2, 1),
(3, 4, 1),
(3, 5, 1),
(3, 6, 1),
(3, 7, 1),
(3, 13, 1),
(3, 14, 1),
(3, 15, 1),
(3, 16, 1),
(3, 17, 1),
(3, 20, 1),
(3, 23, 1),
(3, 25, 1),
(3, 29, 1),
(4, 1, 1),
(4, 5, 1),
(4, 14, 1),
(4, 15, 1),
(4, 17, 1),
(4, 20, 1),
(5, 1, 1),
(5, 15, 1),
(5, 17, 1);

INSERT INTO recompensas (nombre, costo_puntos, id_producto, id_menu, estado) VALUES
('Bebida Gratuita Refresco o Agua', 10, 13, NULL, 'ACTIVO'),
('Acompanamiento Gratuito Doritos', 15, 17, NULL, 'ACTIVO'),
('Sandwich Gratuito Atun o Pollo', 30, 1, NULL, 'ACTIVO'),
('Menu Gratuito Combo Individual', 50, NULL, 5, 'ACTIVO');

INSERT INTO canjes (id_cliente, id_recompensa, puntos_utilizados, fecha) VALUES
(1, 4, 50, '2026-09-18 13:40:00'),
(2, 3, 30, '2026-10-05 14:15:00'),
(1, 1, 10, '2026-10-15 11:20:00'),
(3, 2, 15, '2026-10-22 17:05:00'),
(2, 1, 10, '2026-10-29 19:30:00'),
(1, 2, 15, '2026-11-02 12:45:00');

INSERT INTO pedidos (id_cliente, fecha, estado, total) VALUES
(1, '2026-09-15 12:30:00', 'ENTREGADO', 170.00),
(2, '2026-09-22 14:10:00', 'ENTREGADO', 160.00),
(3, '2026-09-28 19:45:00', 'ENTREGADO', 85.00),
(1, '2026-10-05 13:15:00', 'ENTREGADO', 230.00),
(4, '2026-10-10 18:20:00', 'ENTREGADO', 75.00),
(5, '2026-10-15 12:50:00', 'ENTREGADO', 80.00),
(2, '2026-10-19 20:00:00', 'ENTREGADO', 375.00),
(6, '2026-10-24 13:40:00', 'ENTREGADO', 55.00),
(7, '2026-10-28 19:10:00', 'ENTREGADO', 720.00),
(9, '2026-11-01 12:20:00', 'ENTREGADO', 160.00),
(1, '2026-11-02 14:05:00', 'ENTREGADO', 160.00),
(2, '2026-11-03 11:30:00', 'ENTREGADO', 230.00),
(3, '2026-11-03 13:45:00', 'ENTREGADO', 85.00),
(1, '2026-11-03 17:15:00', 'ENTREGADO', 107.00);

INSERT INTO pedido_detalles (id_pedido, id_producto, id_menu, cantidad, precio_unitario) VALUES
(1, NULL, 5, 2, 85.00),
(2, NULL, 4, 1, 160.00),
(3, NULL, 5, 1, 85.00),
(4, NULL, 1, 1, 230.00),
(5, 1, NULL, 1, 75.00),
(6, 2, NULL, 1, 80.00),
(7, NULL, 3, 1, 375.00),
(8, 5, NULL, 1, 55.00),
(9, NULL, 2, 1, 720.00),
(10, NULL, 4, 1, 160.00),
(11, 1, NULL, 1, 75.00),
(11, NULL, 5, 1, 85.00),
(12, NULL, 1, 1, 230.00),
(13, NULL, 5, 1, 85.00),
(14, 1, NULL, 1, 75.00),
(14, 7, NULL, 1, 20.00);

INSERT INTO pedido_detalle_extras (id_detalle, id_producto, cantidad, precio_unitario) VALUES
(15, 28, 1, 12.00);

INSERT INTO pagos (id_pedido, metodo, monto, cambio, estado, referencia, fecha) VALUES
(1, 'EFECTIVO', 200.00, 30.00, 'APROBADO', 'REC-0915-01', '2026-09-15 12:32:00'),
(2, 'TARJETA', 160.00, 0.00, 'APROBADO', 'VISA-847291', '2026-09-22 14:12:00'),
(3, 'EFECTIVO', 100.00, 15.00, 'APROBADO', 'REC-0928-02', '2026-09-28 19:47:00'),
(4, 'EFECTIVO', 250.00, 20.00, 'APROBADO', 'REC-1005-01', '2026-10-05 13:18:00'),
(5, 'TARJETA', 75.00, 0.00, 'APROBADO', 'MC-559102', '2026-10-10 18:22:00'),
(6, 'EFECTIVO', 100.00, 20.00, 'APROBADO', 'REC-1015-03', '2026-10-15 12:53:00'),
(7, 'TARJETA', 375.00, 0.00, 'APROBADO', 'VISA-992144', '2026-10-19 20:02:00'),
(8, 'EFECTIVO', 60.00, 5.00, 'APROBADO', 'REC-1024-01', '2026-10-24 13:42:00'),
(9, 'TARJETA', 720.00, 0.00, 'APROBADO', 'VISA-112358', '2026-10-28 19:12:00'),
(10, 'EFECTIVO', 200.00, 40.00, 'APROBADO', 'REC-1101-01', '2026-11-01 12:23:00'),
(11, 'TARJETA', 160.00, 0.00, 'APROBADO', 'VISA-334455', '2026-11-02 14:08:00'),
(12, 'EFECTIVO', 250.00, 20.00, 'APROBADO', 'REC-1103-01', '2026-11-03 11:33:00'),
(13, 'TARJETA', 85.00, 0.00, 'APROBADO', 'VISA-778899', '2026-11-03 13:48:00'),
(14, 'EFECTIVO', 110.00, 3.00, 'APROBADO', 'REC-1103-02', '2026-11-03 17:18:00');

INSERT INTO movimientos_puntos (id_cliente, id_pedido, tipo, puntos, fecha) VALUES
(1, 1, 'ACUMULACION', 16, '2026-09-15 12:32:00'),
(1, NULL, 'CANJE', 50, '2026-09-18 13:40:00'),
(2, 2, 'ACUMULACION', 8, '2026-09-22 14:12:00'),
(3, 3, 'ACUMULACION', 8, '2026-09-28 19:47:00'),
(1, 4, 'ACUMULACION', 8, '2026-10-05 13:18:00'),
(2, NULL, 'CANJE', 30, '2026-10-05 14:15:00'),
(4, 5, 'ACUMULACION', 2, '2026-10-10 18:22:00'),
(5, 6, 'ACUMULACION', 2, '2026-10-15 12:53:00'),
(1, NULL, 'CANJE', 10, '2026-10-15 11:20:00'),
(2, 7, 'ACUMULACION', 8, '2026-10-19 20:02:00'),
(3, NULL, 'CANJE', 15, '2026-10-22 17:05:00'),
(6, 8, 'ACUMULACION', 2, '2026-10-24 13:42:00'),
(7, 9, 'ACUMULACION', 8, '2026-10-28 19:12:00'),
(2, NULL, 'CANJE', 10, '2026-10-29 19:30:00'),
(9, 10, 'ACUMULACION', 8, '2026-11-01 12:23:00'),
(1, 11, 'ACUMULACION', 10, '2026-11-02 14:08:00'),
(1, NULL, 'CANJE', 15, '2026-11-02 12:45:00'),
(2, 12, 'ACUMULACION', 8, '2026-11-03 11:33:00'),
(3, 13, 'ACUMULACION', 8, '2026-11-03 13:48:00'),
(1, 14, 'ACUMULACION', 2, '2026-11-03 17:18:00');

INSERT INTO movimientos_inventario (id_producto, tipo, cantidad, fecha, observacion) VALUES
(1, 'ENTRADA', 100, '2026-09-01 08:00:00', 'Lote inicial de panes y proteinas'),
(2, 'ENTRADA', 80, '2026-09-01 08:00:00', 'Lote inicial de panes y quesos'),
(3, 'ENTRADA', 10, '2026-09-01 08:00:00', 'Lote especial cerdo ahumado'),
(4, 'ENTRADA', 50, '2026-09-01 08:00:00', 'Lote inicial pavo y chompipe'),
(5, 'ENTRADA', 80, '2026-09-01 08:00:00', 'Lote inicial pechuga de pollo'),
(6, 'ENTRADA', 50, '2026-09-01 08:00:00', 'Lote inicial ensaladas preparadas'),
(7, 'ENTRADA', 100, '2026-09-01 08:00:00', 'Lote inicial Monster negra'),
(9, 'ENTRADA', 15, '2026-09-01 08:00:00', 'Lote promocional Monster rosa'),
(13, 'ENTRADA', 150, '2026-09-01 08:00:00', 'Lote inicial agua embotellada'),
(15, 'ENTRADA', 150, '2026-09-01 08:00:00', 'Lote inicial Cocacola'),
(17, 'ENTRADA', 150, '2026-09-01 08:00:00', 'Lote inicial Doritos'),
(19, 'ENTRADA', 20, '2026-09-01 08:00:00', 'Lote Cheetos flaming hot'),
(24, 'ENTRADA', 20, '2026-09-01 08:00:00', 'Lote Doritos incognita'),
(28, 'ENTRADA', 20, '2026-09-01 08:00:00', 'Lote Galletas gourmet frambuesa'),
(1, 'VENTA', 5, '2026-09-15 12:30:00', 'Despacho por pedidos'),
(15, 'VENTA', 5, '2026-09-15 12:30:00', 'Despacho por pedidos'),
(17, 'VENTA', 5, '2026-09-15 12:30:00', 'Despacho por pedidos'),
(13, 'CANJE', 1, '2026-10-15 11:20:00', 'Canje de bebida gratuita'),
(17, 'CANJE', 1, '2026-10-22 17:05:00', 'Canje de acompanamiento gratuito'),
(1, 'CANJE', 1, '2026-10-05 14:15:00', 'Canje de sandwich gratuito');
