USE pan_puntos_premios;

SHOW TABLES;

SELECT * FROM clientes;

SELECT * FROM productos;

SELECT * FROM menus;

SELECT
    m.nombre AS menu,
    p.nombre AS producto,
    mc.cantidad,
    mc.cantidad_extra
FROM menu_componentes mc
INNER JOIN menus m
    ON mc.id_menu = m.id_menu
INNER JOIN productos p
    ON mc.id_producto = p.id_producto;

SELECT
    pe.id_pedido,
    c.nombre AS cliente,
    pe.fecha,
    pe.estado,
    pe.total
FROM pedidos pe
INNER JOIN clientes c
    ON pe.id_cliente = c.id_cliente;

SELECT
    pd.id_detalle,
    pd.id_pedido,
    pd.id_producto,
    pd.id_menu,
    pd.cantidad,
    pd.precio_unitario,
    pd.extras
FROM pedido_detalles pd;

SELECT * FROM pagos;

SELECT
    id_producto,
    nombre,
    existencia,
    precio,
    estado
FROM productos;

SELECT
    mi.id_movimiento,
    p.nombre AS producto,
    mi.tipo,
    mi.cantidad,
    mi.fecha,
    mi.observacion
FROM movimientos_inventario mi
INNER JOIN productos p
    ON mi.id_producto = p.id_producto;

SELECT
    mp.id_movimiento,
    c.nombre AS cliente,
    mp.tipo,
    mp.puntos,
    mp.fecha
FROM movimientos_puntos mp
INNER JOIN clientes c
    ON mp.id_cliente = c.id_cliente;

SELECT * FROM recompensas;

SELECT
    ca.id_canje,
    c.nombre AS cliente,
    r.nombre AS recompensa,
    ca.puntos_utilizados,
    ca.fecha
FROM canjes ca
INNER JOIN clientes c
    ON ca.id_cliente = c.id_cliente
INNER JOIN recompensas r
    ON ca.id_recompensa = r.id_recompensa;

SELECT
    pe.id_pedido,
    pd.id_detalle,
    p.nombre AS producto_extra,
    pde.cantidad,
    pde.precio_unitario
FROM pedido_detalle_extras pde
INNER JOIN pedido_detalles pd
    ON pde.id_detalle = pd.id_detalle
INNER JOIN pedidos pe
    ON pd.id_pedido = pe.id_pedido
INNER JOIN productos p
    ON pde.id_producto = p.id_producto;

SELECT
    COUNT(*) AS total_productos,
    SUM(existencia) AS unidades_disponibles
FROM productos;

SELECT
    estado,
    COUNT(*) AS cantidad_pedidos
FROM pedidos
GROUP BY estado;

SELECT
    nombre,
    puntos,
    estado
FROM clientes
ORDER BY puntos DESC;