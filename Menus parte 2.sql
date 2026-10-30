USE pan_puntos_premios;

ALTER TABLE menus
ADD COLUMN tipo ENUM('SANDWICH', 'COMBO')
NOT NULL DEFAULT 'SANDWICH';

ALTER TABLE menus
ADD COLUMN id_producto INT NULL;

ALTER TABLE menus
ADD UNIQUE KEY uq_menus_producto (id_producto);

ALTER TABLE menus
ADD CONSTRAINT fk_menus_producto
FOREIGN KEY (id_producto)
REFERENCES productos(id_producto);

ALTER TABLE menu_componentes
ADD COLUMN cantidad_extra INT NOT NULL DEFAULT 0;

ALTER TABLE menu_componentes
ADD CONSTRAINT chk_cantidad_extra
CHECK (cantidad_extra >= 0);
