USE pan_puntos_premios;

-- Clientes de prueba
INSERT INTO clientes
    (identificador, nombre, telefono, correo)
VALUES
    ('CLI001', 'Carlos Lopez', '55550001', 'carlos@example.com'),
    ('CLI002', 'Maria Perez', '55550002', 'maria@example.com');

-- Productos de prueba
INSERT INTO productos
    (codigo, nombre, categoria, precio, existencia)
VALUES
    ('SAN001', 'Sandwich de jamon', 'SANDWICH', 25.00, 30),
    ('BEB001', 'Gaseosa', 'BEBIDA', 8.00, 40),
    ('ACO001', 'Papas fritas', 'ACOMPANAMIENTO', 12.00, 25);

-- Menu de prueba
INSERT INTO menus
    (codigo, nombre, precio)
VALUES
    ('MEN001', 'Menu clasico', 40.00);

-- Componentes del menu
INSERT INTO menu_componentes
    (id_menu, id_producto, cantidad)
VALUES
    (1, 1, 1),
    (1, 2, 1),
    (1, 3, 1);

-- Recompensas del proyecto
INSERT INTO recompensas
    (nombre, costo_puntos, id_producto)
VALUES
    ('Bebida gratuita', 10, 2),
    ('Acompanamiento gratuito', 15, 3),
    ('Sandwich gratuito', 30, 1);

INSERT INTO recompensas
    (nombre, costo_puntos, id_menu)
VALUES
    ('Menu gratuito', 50, 1);
