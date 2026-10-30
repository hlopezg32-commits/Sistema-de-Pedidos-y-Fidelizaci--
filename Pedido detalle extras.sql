USE pan_puntos_premios;

CREATE TABLE pedido_detalle_extras (
    id_detalle_extra INT AUTO_INCREMENT PRIMARY KEY,
    id_detalle INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_extra_detalle
        FOREIGN KEY (id_detalle)
        REFERENCES pedido_detalles(id_detalle),

    CONSTRAINT fk_extra_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CONSTRAINT chk_extra_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_extra_precio
        CHECK (precio_unitario >= 0)
);
