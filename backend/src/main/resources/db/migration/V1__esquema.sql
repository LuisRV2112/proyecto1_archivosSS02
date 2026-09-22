-- =============================================================================
-- V1__esquema.sql
-- Mini ERP — Esquema de la base de datos
--
-- Metodo de valuacion de inventario: PEPS (primeras entradas, primeras salidas)
--
-- Principio de diseno: toda regla de negocio que pueda expresarse como
-- restriccion se declara AQUI, en la base de datos, y no solo en el backend.
-- Asi, ni un error de programacion ni un acceso directo por SQL pueden dejar
-- datos inconsistentes (por ejemplo, existencias negativas).
-- =============================================================================


-- -----------------------------------------------------------------------------
-- SEGURIDAD: roles y usuarios
-- -----------------------------------------------------------------------------

CREATE TABLE rol (
    id          SMALLSERIAL  PRIMARY KEY,
    nombre      VARCHAR(30)  NOT NULL UNIQUE,
    descripcion VARCHAR(150)
);

CREATE TABLE usuario (
    id              SERIAL       PRIMARY KEY,
    username        VARCHAR(40)  NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,          -- BCrypt, nunca texto plano
    nombre_completo VARCHAR(100) NOT NULL,
    rol_id          SMALLINT     NOT NULL REFERENCES rol(id),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP    NOT NULL DEFAULT now()
);


-- -----------------------------------------------------------------------------
-- CATALOGO: categorias y productos
-- -----------------------------------------------------------------------------

CREATE TABLE categoria (
    id          SERIAL       PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- La EXISTENCIA no se guarda en esta tabla: se calcula sumando lo disponible
-- en los lotes (ver vista v_existencia en V3). Tener una sola fuente de verdad
-- evita que un contador y los lotes queden desincronizados.
CREATE TABLE producto (
    id           SERIAL        PRIMARY KEY,
    codigo       VARCHAR(20)   NOT NULL UNIQUE,     -- SKU visible para el negocio
    nombre       VARCHAR(100)  NOT NULL,
    descripcion  VARCHAR(300),
    categoria_id INTEGER       NOT NULL REFERENCES categoria(id),
    precio_venta NUMERIC(12,2) NOT NULL CHECK (precio_venta > 0),
    stock_minimo INTEGER       NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    activo       BOOLEAN       NOT NULL DEFAULT TRUE,  -- borrado logico
    creado_en    TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_producto_categoria ON producto(categoria_id);


-- -----------------------------------------------------------------------------
-- PROVEEDORES
-- -----------------------------------------------------------------------------

CREATE TABLE proveedor (
    id        SERIAL       PRIMARY KEY,
    nit       VARCHAR(15)  NOT NULL UNIQUE,
    nombre    VARCHAR(100) NOT NULL,
    telefono  VARCHAR(20),
    correo    VARCHAR(100),
    direccion VARCHAR(200),
    activo    BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP    NOT NULL DEFAULT now()
);

-- Relacion muchos a muchos: un proveedor surte varios productos y un producto
-- puede comprarse a varios proveedores (requisito explicito del enunciado).
CREATE TABLE producto_proveedor (
    producto_id      INTEGER       NOT NULL REFERENCES producto(id),
    proveedor_id     INTEGER       NOT NULL REFERENCES proveedor(id),
    costo_referencia NUMERIC(12,2) CHECK (costo_referencia > 0),
    PRIMARY KEY (producto_id, proveedor_id)
);


-- -----------------------------------------------------------------------------
-- COMPRAS
-- -----------------------------------------------------------------------------

CREATE TABLE compra (
    id               SERIAL        PRIMARY KEY,
    proveedor_id     INTEGER       NOT NULL REFERENCES proveedor(id),
    usuario_id       INTEGER       NOT NULL REFERENCES usuario(id),
    fecha            TIMESTAMP     NOT NULL DEFAULT now(),
    numero_documento VARCHAR(30),                   -- factura del proveedor
    total            NUMERIC(14,2) NOT NULL CHECK (total >= 0),
    observaciones    VARCHAR(300)
);

CREATE INDEX idx_compra_fecha     ON compra(fecha);
CREATE INDEX idx_compra_proveedor ON compra(proveedor_id);

CREATE TABLE detalle_compra (
    id             SERIAL        PRIMARY KEY,
    compra_id      INTEGER       NOT NULL REFERENCES compra(id),
    producto_id    INTEGER       NOT NULL REFERENCES producto(id),
    cantidad       INTEGER       NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(12,2) NOT NULL CHECK (costo_unitario > 0),
    subtotal       NUMERIC(14,2) GENERATED ALWAYS AS (cantidad * costo_unitario) STORED
);

CREATE INDEX idx_detalle_compra_producto ON detalle_compra(producto_id);


-- -----------------------------------------------------------------------------
-- INVENTARIO: lotes (la base del metodo PEPS)
-- -----------------------------------------------------------------------------

-- Cada linea de compra genera un LOTE: una capa de inventario con su propio
-- costo y su fecha de ingreso. Al vender, PEPS consume los lotes del mas
-- antiguo al mas reciente.
--
-- Las dos restricciones CHECK son la garantia final contra existencias
-- negativas: aunque el backend fallara, la base de datos rechazaria la
-- operacion.
CREATE TABLE lote (
    id                  SERIAL        PRIMARY KEY,
    producto_id         INTEGER       NOT NULL REFERENCES producto(id),
    detalle_compra_id   INTEGER       NOT NULL UNIQUE REFERENCES detalle_compra(id),
    fecha_ingreso       TIMESTAMP     NOT NULL,
    cantidad_inicial    INTEGER       NOT NULL CHECK (cantidad_inicial > 0),
    cantidad_disponible INTEGER       NOT NULL,
    costo_unitario      NUMERIC(12,2) NOT NULL CHECK (costo_unitario > 0),

    CONSTRAINT chk_lote_disponible_no_negativo
        CHECK (cantidad_disponible >= 0),
    CONSTRAINT chk_lote_disponible_no_excede
        CHECK (cantidad_disponible <= cantidad_inicial)
);

-- Indice clave para PEPS: busca los lotes con existencia de un producto,
-- ordenados por fecha de ingreso, sin recorrer la tabla completa.
CREATE INDEX idx_lote_peps
    ON lote(producto_id, fecha_ingreso, id)
    WHERE cantidad_disponible > 0;


-- -----------------------------------------------------------------------------
-- CLIENTES Y VENTAS
-- -----------------------------------------------------------------------------

CREATE TABLE cliente (
    id        SERIAL       PRIMARY KEY,
    nit       VARCHAR(15)  NOT NULL,                -- 'CF' = consumidor final
    nombre    VARCHAR(100) NOT NULL,
    telefono  VARCHAR(20),
    correo    VARCHAR(100),
    direccion VARCHAR(200),
    activo    BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP    NOT NULL DEFAULT now()
);

-- El NIT debe ser unico, EXCEPTO 'CF': en Guatemala muchos clientes compran
-- como consumidor final y todos comparten ese valor. Un indice unico parcial
-- expresa exactamente esa regla.
CREATE UNIQUE INDEX uq_cliente_nit ON cliente(nit) WHERE nit <> 'CF';

CREATE SEQUENCE seq_numero_factura START 1001;

CREATE TABLE venta (
    id             SERIAL        PRIMARY KEY,
    numero_factura INTEGER       NOT NULL UNIQUE DEFAULT nextval('seq_numero_factura'),
    cliente_id     INTEGER       NOT NULL REFERENCES cliente(id),
    usuario_id     INTEGER       NOT NULL REFERENCES usuario(id),
    fecha          TIMESTAMP     NOT NULL DEFAULT now(),
    subtotal       NUMERIC(14,2) NOT NULL CHECK (subtotal >= 0),
    iva            NUMERIC(14,2) NOT NULL CHECK (iva >= 0),
    total          NUMERIC(14,2) NOT NULL CHECK (total >= 0),

    CONSTRAINT chk_venta_total CHECK (total = subtotal + iva)
);

CREATE INDEX idx_venta_fecha   ON venta(fecha);
CREATE INDEX idx_venta_cliente ON venta(cliente_id);

-- Se guarda el precio AL MOMENTO de la venta. Si el precio del producto cambia
-- despues, la factura historica no debe cambiar con el.
CREATE TABLE detalle_venta (
    id              SERIAL        PRIMARY KEY,
    venta_id        INTEGER       NOT NULL REFERENCES venta(id),
    producto_id     INTEGER       NOT NULL REFERENCES producto(id),
    cantidad        INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario > 0),
    subtotal        NUMERIC(14,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED
);

CREATE INDEX idx_detalle_venta_producto ON detalle_venta(producto_id);

-- Trazabilidad PEPS: de que lotes salio cada linea de venta. Una linea puede
-- consumir varios lotes si el mas antiguo no alcanza. Permite calcular el
-- costo de lo vendido y demostrar que se respeto el orden PEPS.
CREATE TABLE detalle_venta_lote (
    id               SERIAL        PRIMARY KEY,
    detalle_venta_id INTEGER       NOT NULL REFERENCES detalle_venta(id),
    lote_id          INTEGER       NOT NULL REFERENCES lote(id),
    cantidad         INTEGER       NOT NULL CHECK (cantidad > 0),
    costo_unitario   NUMERIC(12,2) NOT NULL CHECK (costo_unitario > 0)
);


-- -----------------------------------------------------------------------------
-- MOVIMIENTOS DE INVENTARIO (kardex)
-- -----------------------------------------------------------------------------

-- Cada entrada o salida queda registrada con su ORIGEN, como pide el
-- enunciado: permite saber como y por que cambio la existencia de un producto.
CREATE TABLE movimiento_inventario (
    id                    SERIAL        PRIMARY KEY,
    producto_id           INTEGER       NOT NULL REFERENCES producto(id),
    lote_id               INTEGER       REFERENCES lote(id),
    tipo                  VARCHAR(10)   NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA')),
    origen                VARCHAR(10)   NOT NULL CHECK (origen IN ('COMPRA', 'VENTA', 'AJUSTE')),
    referencia_id         INTEGER,                  -- id de la compra o de la venta
    cantidad              INTEGER       NOT NULL CHECK (cantidad > 0),
    costo_unitario        NUMERIC(12,2) NOT NULL,
    existencia_resultante INTEGER       NOT NULL CHECK (existencia_resultante >= 0),
    usuario_id            INTEGER       NOT NULL REFERENCES usuario(id),
    fecha                 TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimiento_producto ON movimiento_inventario(producto_id, fecha);


-- -----------------------------------------------------------------------------
-- BITACORA
-- -----------------------------------------------------------------------------

CREATE TABLE bitacora (
    id         BIGSERIAL    PRIMARY KEY,
    usuario_id INTEGER      REFERENCES usuario(id),   -- NULL en intentos de login fallidos
    modulo     VARCHAR(30)  NOT NULL,
    accion     VARCHAR(30)  NOT NULL,
    entidad    VARCHAR(40),
    entidad_id INTEGER,
    detalle    VARCHAR(500),
    fecha      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_bitacora_fecha ON bitacora(fecha);
