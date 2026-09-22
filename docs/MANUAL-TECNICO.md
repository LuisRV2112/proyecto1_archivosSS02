# Manual Técnico — Mini ERP

Sistema de gestión comercial para una empresa de muebles de hogar y oficina:
productos, proveedores, compras, inventario, clientes y ventas, con control de
acceso por roles.

---

## 1. Arquitectura

Dos aplicaciones independientes que se comunican por HTTP/JSON. El frontend
**nunca** accede a la base de datos directamente; solo consume la API REST del
backend.

```
┌──────────────┐    HTTP/JSON     ┌──────────────────┐    JDBC    ┌─────────────┐
│  Angular 21  │ ───────────────► │  Spring Boot 4   │ ─────────► │ PostgreSQL  │
│  (frontend)  │ ◄─────────────── │  Java 21         │ ◄───────── │      18     │
│  :4200       │    API REST      │  (backend) :8080 │            │    :5432    │
└──────────────┘                  └──────────────────┘            └─────────────┘
```

Ver `docs/diagramas/despliegue.drawio` para el diagrama de despliegue completo.

### Backend — capas y paquetes

```
com.minierp.backend
├── config/         SecurityConfig (seguridad, CORS)
├── seguridad/       JWT: filtro, generacion/validacion de tokens, UserDetailsService
├── modelo/          Entidades JPA (una por tabla, mas las vistas como @Immutable)
├── repositorio/     Interfaces Spring Data (consultas derivadas y @Query)
├── dto/             Records de entrada y salida de la API
├── servicio/        Logica de negocio y transacciones (@Transactional)
├── controlador/     Endpoints REST (@RestController)
└── excepcion/       Excepciones propias y el manejador global (@RestControllerAdvice)
```

Regla de capas estricta: `controlador → servicio → repositorio → modelo`. Un
controlador nunca llama a un repositorio directamente; toda la lógica de
negocio vive en el servicio.

### Frontend — estructura

```
frontend/src/app
├── core/            Modelos, servicio de autenticacion, interceptor JWT, guards
├── layout/          Shell con menu lateral filtrado por rol
├── auth/login/      Pantalla de login
├── paginas/inicio/  Landing tras el login (segura para cualquier rol)
└── <modulo>/        Un folder por modulo (ventas, compras, inventario,
                      productos, clientes, proveedores, categorias, reportes,
                      bitacora, usuarios), cada uno con su *.service.ts
                      (llamadas HTTP) y su componente standalone (lista + form)
```

Componentes standalone (sin `NgModule`), señales (`signal`/`computed`) para el
estado local, `ReactiveFormsModule` para los formularios con validación.

---

## 2. Modelo de datos

El esquema completo está en `migraciones-iniciales/V1__esquema.sql` (aplicado
por Flyway). Diagrama entidad-relación en notación de Chen:
`docs/diagramas/er.drawio`.

### Tablas principales

| Tabla | Propósito |
|---|---|
| `rol`, `usuario` | Roles fijos (ADMINISTRACION, COMPRAS, INVENTARIO, VENTAS) y credenciales |
| `categoria`, `producto` | Catálogo. `producto` **no** tiene columna de existencia |
| `proveedor`, `producto_proveedor` | Proveedores y qué productos surte cada uno (M:N con costo de referencia) |
| `compra`, `detalle_compra` | Compras y sus líneas |
| `lote` | Una capa de inventario por cada línea de compra — la base del método PEPS |
| `cliente` | Clientes; NIT único excepto `'CF'` (consumidor final) |
| `venta`, `detalle_venta` | Ventas y sus líneas |
| `detalle_venta_lote` | De qué lote(s) salió cada línea de venta (trazabilidad PEPS) |
| `movimiento_inventario` | Kardex: cada entrada y salida con su origen |
| `bitacora` | Registro de acciones de los usuarios |

### Vistas

- `v_existencia`: existencia de cada producto, calculada sumando
  `lote.cantidad_disponible` (nunca almacenada).
- `v_alerta_stock`: productos con existencia ≤ su stock mínimo.

---

## 3. Decisiones de diseño

### 3.1 Método de inventario: PEPS

El carné del estudiante determina el método (par → PEPS, primeras entradas
primeras salidas). Implementado en `VentaServicio.registrarLinea`:

1. Se buscan los lotes del producto con `cantidad_disponible > 0`, ordenados
   por `fecha_ingreso` y luego `id` ascendente — el más antiguo primero.
2. Se consume del primer lote; si no alcanza, se pasa al siguiente, hasta
   cubrir la cantidad pedida.
3. Por cada lote tocado se registra una fila en `detalle_venta_lote`
   (trazabilidad) y un `movimiento_inventario` tipo `SALIDA`.

El código está comentado extensamente paso a paso en
`backend/src/main/java/com/minierp/backend/servicio/VentaServicio.java`.

### 3.2 Bloqueo pesimista al vender

`LoteRepositorio.buscarConExistenciaParaActualizar` usa
`@Lock(LockModeType.PESSIMISTIC_WRITE)`, que en PostgreSQL se traduce a
`SELECT ... FOR UPDATE`. Es la solución al problema clásico de acceso
concurrente: si dos vendedores intentan vender el último producto disponible
al mismo tiempo, el segundo debe esperar a que la transacción del primero
termine (commit o rollback) antes de poder leer el saldo real. Sin este
bloqueo, ambos podrían leer "hay 1 disponible" y venderlo los dos, dejando la
existencia en negativo.

### 3.3 Existencia calculada, no almacenada

`producto` no tiene columna `existencia`. Se calcula siempre sumando
`lote.cantidad_disponible` (vista `v_existencia`). Con una sola fuente de
verdad es imposible que un contador y los lotes queden desincronizados.

### 3.4 Borrado lógico

`producto`, `proveedor`, `cliente`, `categoria` y `usuario` tienen una columna
`activo`. Nunca se ejecuta `DELETE` sobre ellos — desactivar preserva el
historial de compras, ventas y kardex que los referencian. Los servicios
correspondientes exponen `activar()`/`desactivar()`, nunca un método de borrado
físico.

### 3.5 Precio histórico

`detalle_venta.precio_unitario` se congela al momento de la venta (se copia
desde `producto.precio_venta`, no se referencia). Si el precio del producto
cambia después, las facturas ya emitidas no cambian con él.

### 3.6 Número de factura

Lo asigna la secuencia `seq_numero_factura` como `DEFAULT` de la columna
(empieza en 1001). En JPA, `Venta.numeroFactura` es `insertable = false`: el
backend nunca lo envía en el `INSERT`; se lee después con
`entityManager.refresh(venta)` una vez que Postgres ya lo asignó.

### 3.7 Transacciones

Registrar una compra o una venta es **una sola transacción**
(`@Transactional`). Si cualquier línea falla (producto no surtido por el
proveedor, existencia insuficiente, producto o cliente inactivo), no queda
nada escrito: ni la cabecera, ni los detalles, ni los lotes, ni los
movimientos de inventario.

### 3.8 Restricciones en la base como última línea de defensa

Los `CHECK` de PostgreSQL (lote no negativo, cantidades y montos positivos,
`venta.total = subtotal + iva`) garantizan la integridad aunque el backend
fallara. El backend valida primero para dar mensajes claros; la base es el
respaldo final.

---

## 4. Seguridad

- JWT firmado con HMAC (`io.jsonwebtoken`), clave y expiración en
  `application.properties` (nunca fijas en el código).
- `JwtAutenticacionFiltro` recarga el usuario desde la base en cada petición
  (no confía solo en el contenido del token), para que una desactivación o un
  cambio de rol tengan efecto inmediato sin esperar a que el token expire.
- Contraseñas con BCrypt (`PasswordEncoder`).
- Reglas de autorización por rol en `SecurityConfig`, alineadas con la tabla
  de permisos de este manual (sección 6).
- CSRF deshabilitado (no hay cookies de sesión que un ataque CSRF pueda
  explotar; la autenticación es 100% por token en el header `Authorization`).

---

## 5. Endpoints de la API

Todos bajo `/api`, requieren `Authorization: Bearer <token>` salvo
`POST /api/auth/login`. Formato de error uniforme:

```json
{ "estado": 400, "error": "Existencia insuficiente", "detalle": "...", "fecha": "..." }
```

### Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Login; devuelve token, usuario, nombre y rol |
| GET | `/api/auth/yo` | Datos del usuario autenticado |

### Catálogos (Categorías, Productos, Proveedores, Clientes)

Mismo patrón para los cuatro (`/api/categorias`, `/api/productos`,
`/api/proveedores`, `/api/clientes`):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/{recurso}?buscar=&activo=` | Listar, con búsqueda y filtro de activos |
| GET | `/{recurso}/{id}` | Obtener por id |
| POST | `/{recurso}` | Crear |
| PUT | `/{recurso}/{id}` | Actualizar |
| PATCH | `/{recurso}/{id}/activar` | Activar |
| PATCH | `/{recurso}/{id}/desactivar` | Desactivar (borrado lógico) |

Adicional en Proveedores (productos que surte, con costo de referencia):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/proveedores/{id}/productos` | Listar productos asociados |
| POST | `/api/proveedores/{id}/productos` | Asociar un producto |
| DELETE | `/api/proveedores/{id}/productos/{productoId}` | Desasociar |

### Compras

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/compras` | Registrar una compra (crea lotes y kardex) |
| GET | `/api/compras?proveedorId=&desde=&hasta=` | Listar con filtros |
| GET | `/api/compras/{id}` | Detalle con sus líneas |

### Inventario

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/inventario/existencias` | Existencia de cada producto (`v_existencia`) |
| GET | `/api/inventario/alertas` | Productos en o bajo su stock mínimo (`v_alerta_stock`) |
| GET | `/api/inventario/productos/{id}/kardex` | Movimientos del producto, orden cronológico |

### Ventas

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/ventas` | Registrar una venta (PEPS, bloqueo pesimista) |
| GET | `/api/ventas?clienteId=&desde=&hasta=` | Listar con filtros |
| GET | `/api/ventas/{id}` | Detalle, incluyendo de qué lotes salió cada línea |
| GET | `/api/ventas/{id}/factura` | Factura en PDF (`application/pdf`) |

### Reportes

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/reportes/productos/mas-vendidos` | Top 10 por unidades |
| GET | `/api/reportes/productos/menor-existencia` | Ordenado ascendente |
| GET | `/api/reportes/productos/mas-movimientos` | Ordenado descendente |
| GET | `/api/reportes/inventario/productos/{id}/kardex` | Igual que el de Inventario |
| GET | `/api/reportes/compras?desde=&hasta=` | Compras en rango de fechas |
| GET | `/api/reportes/proveedores/mas-compras` | Top 5 por monto acumulado |
| GET | `/api/reportes/compras/productos-frecuentes` | Por número de líneas de compra |
| GET | `/api/reportes/ventas?desde=&hasta=` | Ventas en rango de fechas |
| GET | `/api/reportes/clientes/mas-compras` | Top 10 por monto acumulado |
| GET | `/api/reportes/ventas/productos-mas-ingresos` | Top 10 por ingreso generado |
| GET | `/api/reportes/ventas/resumen-periodo?periodo=` | `dia`, `semana`, `mes` o `anio` (`date_trunc`) |

### Bitácora y Usuarios (solo ADMINISTRACION)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/bitacora?usuarioId=&modulo=&desde=&hasta=` | Consulta con filtros |
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/roles` | Roles disponibles (para el formulario) |
| POST | `/api/usuarios` | Crear usuario |
| PUT | `/api/usuarios/{id}` | Actualizar nombre y rol |
| PATCH | `/api/usuarios/{id}/activar` | Activar |
| PATCH | `/api/usuarios/{id}/desactivar` | Desactivar (ya no puede iniciar sesión) |

---

## 6. Roles y permisos

**ADMINISTRACION puede ver todo.**

| Recurso | ADMIN | COMPRAS | INVENTARIO | VENTAS |
|---|:-:|:-:|:-:|:-:|
| Usuarios | ✅ | | | |
| Categorías (escribir) | ✅ | | ✅ | |
| Productos (escribir) | ✅ | | ✅ | |
| Productos (leer) | ✅ | ✅ | ✅ | ✅ |
| Proveedores | ✅ | ✅ | | |
| Compras | ✅ | ✅ | | |
| Existencias y kardex | ✅ | ✅ | ✅ | ✅ (solo leer) |
| Alertas de stock | ✅ | ✅ | ✅ | |
| Clientes | ✅ | | | ✅ |
| Ventas y facturas | ✅ | | | ✅ |
| Reportes de productos e inventario | ✅ | | ✅ | |
| Reportes de compras y proveedores | ✅ | ✅ | | |
| Reportes de ventas y clientes | ✅ | | | ✅ |
| Bitácora | ✅ | | | |

---

## 7. Instalación y ejecución

### Requisitos

- Fedora Linux (u otra distro con los mismos paquetes)
- JDK 21 (`java-21-openjdk-devel`)
- Node.js ≥ 20.19 y npm
- PostgreSQL 18, con autenticación por contraseña (`scram-sha-256`)

### Base de datos

```bash
sudo -u postgres psql -c "CREATE ROLE erp LOGIN PASSWORD 'erp';"
sudo -u postgres psql -c "CREATE DATABASE mini_erp OWNER erp;"
```

Las migraciones (`backend/src/main/resources/db/migration/V1__esquema.sql`,
`V2__datos_iniciales.sql`, `V3__vistas.sql`) las aplica Flyway automáticamente
al arrancar el backend — nunca se ejecutan a mano.

### Backend

```bash
cd backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw spring-boot:run
```

Arranca en `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install --legacy-peer-deps   # ver nota abajo
npx ng serve
```

Arranca en `http://localhost:4200`.

> **Nota:** `npm install` (sin `--legacy-peer-deps`) puede fallar con
> `TypeError: Cannot read properties of null (reading 'edgesOut')` — es un bug
> conocido de la resolución de peer-dependencies de npm 10.x con las
> dependencias de testing (`vitest`) que trae Angular 21 por defecto.
> `--legacy-peer-deps` lo evita sin afectar el resto de la instalación.

### Usuarios de prueba

Contraseña de todos: **`Erp2026!`**

| Usuario | Rol |
|---|---|
| `admin` | ADMINISTRACION |
| `compras` | COMPRAS |
| `inventario` | INVENTARIO |
| `ventas` | VENTAS |
