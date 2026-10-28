USE pan_puntos_premios;

CREATE TABLE movimientos_inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    tipo ENUM('ENTRADA', 'VENTA', 'CANJE') NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255),

    CONSTRAINT fk_movimiento_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CHECK (cantidad > 0)
);

CREATE TABLE movimientos_puntos (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_pedido INT NULL,
    tipo ENUM('ACUMULACION', 'CANJE') NOT NULL,
    puntos INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movimiento_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente),

    CONSTRAINT fk_movimiento_pedido
        FOREIGN KEY (id_pedido)
        REFERENCES pedidos(id_pedido),

    CHECK (puntos > 0)
);

CREATE TABLE recompensas (
    id_recompensa INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    costo_puntos INT NOT NULL,
    id_producto INT NULL,
    id_menu INT NULL,
    estado ENUM('ACTIVA', 'INACTIVA') NOT NULL DEFAULT 'ACTIVA',

    CONSTRAINT fk_recompensa_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CONSTRAINT fk_recompensa_menu
        FOREIGN KEY (id_menu)
        REFERENCES menus(id_menu),

    CHECK (costo_puntos > 0),

    CHECK (
        (id_producto IS NOT NULL AND id_menu IS NULL)
        OR
        (id_producto IS NULL AND id_menu IS NOT NULL)
    )
);

CREATE TABLE canjes (
    id_canje INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_recompensa INT NOT NULL,
    puntos_utilizados INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_canje_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente),

    CONSTRAINT fk_canje_recompensa
        FOREIGN KEY (id_recompensa)
        REFERENCES recompensas(id_recompensa),

    CHECK (puntos_utilizados > 0)
);
