USE pan_puntos_premios;

CREATE TABLE menus (
    id_menu INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    CHECK (precio >= 0)
);

CREATE TABLE menu_componentes (
    id_menu INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,

    PRIMARY KEY (id_menu, id_producto),

    CONSTRAINT fk_componente_menu
        FOREIGN KEY (id_menu)
        REFERENCES menus(id_menu),

    CONSTRAINT fk_componente_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CHECK (cantidad > 0)
);
