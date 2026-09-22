-- =============================================================================
-- V3__vistas.sql
-- Mini ERP — Vistas de consulta
-- =============================================================================


-- -----------------------------------------------------------------------------
-- Existencia actual de cada producto.
--
-- Se CALCULA sumando lo disponible en sus lotes; no se guarda en un contador
-- aparte. Con una sola fuente de verdad es imposible que la existencia y los
-- lotes digan cosas distintas.
--
-- El LEFT JOIN es necesario para que aparezcan tambien los productos que nunca
-- se han comprado (existencia 0), que son justo los que mas le interesan al
-- reporte de productos con menor existencia.
-- -----------------------------------------------------------------------------
CREATE VIEW v_existencia AS
SELECT
    p.id                                     AS producto_id,
    p.codigo,
    p.nombre,
    c.nombre                                 AS categoria,
    p.precio_venta,
    p.stock_minimo,
    p.activo,
    COALESCE(SUM(l.cantidad_disponible), 0)  AS existencia,
    COALESCE(SUM(l.cantidad_disponible * l.costo_unitario), 0)
                                             AS valor_inventario
FROM producto p
JOIN categoria c ON c.id = p.categoria_id
LEFT JOIN lote l ON l.producto_id = p.id
GROUP BY p.id, p.codigo, p.nombre, c.nombre, p.precio_venta, p.stock_minimo, p.activo;


-- -----------------------------------------------------------------------------
-- Productos que requieren atencion: existencia igual o menor a su minimo.
--
-- Responde al requisito del enunciado: "situaciones en las que las
-- existencias de determinados productos alcancen niveles que requieran
-- atencion por parte de los responsables".
-- -----------------------------------------------------------------------------
CREATE VIEW v_alerta_stock AS
SELECT
    producto_id,
    codigo,
    nombre,
    categoria,
    existencia,
    stock_minimo,
    stock_minimo - existencia AS faltante,
    CASE
        WHEN existencia = 0 THEN 'AGOTADO'
        ELSE 'BAJO_MINIMO'
    END                       AS nivel
FROM v_existencia
WHERE activo
  AND existencia <= stock_minimo;
