USE pan_puntos_premios;

CREATE TABLE pedidos (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM(
        'PENDIENTE',
        'PAGADO',
        'ENTREGADO',
        'ANULADO'
    ) NOT NULL DEFAULT 'PENDIENTE',
    total DECIMAL(10,2) NOT NULL DEFAULT 0,

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente),

    CHECK (total >= 0)
);

CREATE TABLE pedido_detalles (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NULL,
    id_menu INT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (id_pedido)
        REFERENCES pedidos(id_pedido),

    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CONSTRAINT fk_detalle_menu
        FOREIGN KEY (id_menu)
        REFERENCES menus(id_menu),

    CHECK (cantidad > 0),
    CHECK (precio_unitario >= 0),

    CHECK (
        (id_producto IS NOT NULL AND id_menu IS NULL)
        OR
        (id_producto IS NULL AND id_menu IS NOT NULL)
    )
);
