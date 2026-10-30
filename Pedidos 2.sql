USE pan_puntos_premios;

ALTER TABLE pedido_detalles
ADD COLUMN extras JSON NULL;