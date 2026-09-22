# CLAUDE.md — Mini ERP (Proyecto 1, Manejo e Implementación de Archivos)

Este archivo contiene el contexto completo del proyecto. **Léelo entero antes de
cualquier tarea.**

El trabajo está dividido en **9 fases**, cada una en su propio archivo dentro de
`fases/` (ver índice en la sección 10). Ejecuta **solo la fase que se te pida**,
verifica que funcione y **detente** a esperar confirmación antes de pasar a la
siguiente.

**Punto de partida: máquina limpia.** No asumas que nada está instalado ni
configurado. Verifica cada cosa antes de usarla.

---

## 1. Qué es el proyecto

Sistema de gestión comercial (Mini ERP) para una empresa de muebles de hogar y
oficina: productos, proveedores, compras, inventario, clientes y ventas, con
control de acceso por roles.

**Entrega: 22 de septiembre de 2026 a las 16:00**, en un repositorio privado de
GitHub. No se aceptan cambios después de esa hora. El tiempo es muy limitado:
**prioriza que funcione sobre que sea elegante**, y no agregues nada que el
enunciado no pida.

### Arquitectura obligatoria

```
┌──────────────┐    HTTP/JSON     ┌──────────────────┐    JDBC    ┌─────────────┐
│  Angular 21  │ ───────────────► │  Spring Boot     │ ─────────► │ PostgreSQL  │
│  (frontend)  │ ◄─────────────── │  Java 21         │ ◄───────── │             │
│  :4200       │    API REST      │  (backend) :8080 │            │  :5432      │
└──────────────┘                  └──────────────────┘            └─────────────┘
```

- Dos aplicaciones **independientes**: `frontend/` y `backend/`
- El frontend **nunca** accede a la base de datos; solo consume la API REST
- El backend es el **único** que habla con PostgreSQL
- La base se crea y se carga **exclusivamente con migraciones Flyway**

### Evaluación oral

El estudiante será evaluado oralmente: debe poder explicar cualquier parte del
código, justificar decisiones de diseño y hacer modificaciones en vivo. Por eso:

- **Comenta el porqué**, no el qué. `// suma los totales` no sirve;
  `// el precio se toma del detalle y no del producto, porque la factura
  historica no debe cambiar si el precio se actualiza` sí.
- Prefiere código **explícito** sobre magia. **No uses Lombok**: getters,
  setters y constructores deben verse. Usa `record` de Java para los DTO.
- Al terminar cada fase, **explica en pocas líneas** las decisiones de diseño
  que no sean evidentes.

---

## 2. Reglas para ejecutar comandos

### ⚠️ Comandos con `sudo`

**No puedes ingresar la contraseña de `sudo` del estudiante.** Si un paso
necesita permisos de administrador:

1. **No lo ejecutes tú.** Muestra el comando exacto en un bloque de código
2. Explica en una línea qué hace
3. Pide al estudiante que lo ejecute en su terminal y te avise
4. Después **verifica tú** el resultado con un comando que no requiera `sudo`

Agrupa los comandos `sudo` de un mismo paso en un solo bloque, para que el
estudiante los ejecute de una vez.

### ⚠️ Java: puede haber varios JDK

El estudiante puede tener otros JDK instalados (por ejemplo Oracle JDK 25) que
sean el `java` por omisión del sistema. **Siempre** exporta el 21 antes de
cualquier comando Maven, en cada terminal nueva:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # debe decir 21
```

Errores como `invalid target release` o `class file version` significan que se
usó el JDK equivocado.

### Entorno objetivo

| Elemento | Valor |
|---|---|
| Sistema operativo | Fedora Linux 43 |
| Gestor de paquetes | DNF 5 |
| IDE del estudiante | IntelliJ IDEA |
| Java | OpenJDK 21 |
| PostgreSQL | el de los repositorios de Fedora (18) |
| Node.js | el de los repositorios de Fedora |

---

## 3. Estructura del repositorio

```
proyecto1-mini-erp/
├── CLAUDE.md                     ← este archivo (contexto)
├── fases/                        ← una instruccion por fase
├── README.md
├── migraciones-iniciales/        ← los 3 .sql entregados (ver seccion 4)
├── backend/                      ← Spring Boot (Maven)
│   ├── pom.xml
│   ├── mvnw
│   └── src/main/
│       ├── java/com/minierp/backend/
│       └── resources/
│           ├── application.properties
│           └── db/migration/     ← aqui se copian los .sql
├── frontend/                     ← Angular 21
└── docs/
    ├── diagramas/                ← archivos .drawio
    ├── MANUAL-TECNICO.md
    └── MANUAL-USUARIO.md
```

### Paquetes del backend

```
com.minierp.backend
├── config/         Seguridad, CORS, configuracion general
├── seguridad/      JWT: filtro, generacion y validacion de tokens
├── modelo/         Entidades JPA
├── repositorio/    Interfaces Spring Data
├── dto/            Records de entrada y salida de la API
├── servicio/       Logica de negocio y transacciones
├── controlador/    Endpoints REST
└── excepcion/      Excepciones propias y manejador global
```

Regla de capas: `controlador → servicio → repositorio → modelo`. **Un
controlador nunca llama a un repositorio directamente.**

---

## 4. La base de datos (diseño ya hecho y verificado)

El esquema y la carga inicial **ya están escritos y probados** en PostgreSQL.
Son tres archivos que el estudiante coloca en `migraciones-iniciales/` antes de
empezar:

| Archivo | Contenido |
|---|---|
| `V1__esquema.sql` | Todas las tablas, restricciones e índices |
| `V2__datos_iniciales.sql` | Carga inicial consistente con PEPS |
| `V3__vistas.sql` | Vistas `v_existencia` y `v_alerta_stock` |

La carga inicial fue **verificada**: 4 usuarios, 30 productos en 6 categorías, 8
proveedores, 20 clientes, 15 compras y 25 ventas; el kardex cuadra con los
lotes, ningún lote queda negativo y todas las ventas respetan el orden PEPS.

### ⛔ Reglas absolutas sobre las migraciones

1. **No reescribas ni regeneres** V1, V2 ni V3. Cópialos tal cual
2. **Nunca los edites** después de aplicados. Flyway guarda un checksum; si
   cambian, el backend no arranca (`Migration checksum mismatch`)
3. Cualquier cambio al esquema va en una migración **nueva**: `V4__...sql`
4. **Nunca los ejecutes a mano con `psql`**: Flyway no sabría que ya se
   aplicaron y fallaría con `relation already exists`

Si al empezar la Fase 1 la carpeta `migraciones-iniciales/` no existe o le falta algún
archivo, **detente y pídeselos al estudiante**. No intentes recrearlos.

### Tablas

| Tabla | Propósito |
|---|---|
| `rol` | ADMINISTRACION, COMPRAS, INVENTARIO, VENTAS |
| `usuario` | Credenciales (BCrypt) y rol |
| `categoria` | Clasificación de productos |
| `producto` | Catálogo. **No tiene columna de existencia** |
| `proveedor` | Proveedores |
| `producto_proveedor` | Relación M:N con costo de referencia |
| `compra` / `detalle_compra` | Compras y sus líneas |
| `lote` | Una capa de inventario por cada línea de compra |
| `cliente` | Clientes. NIT único excepto `'CF'` (consumidor final) |
| `venta` / `detalle_venta` | Ventas y sus líneas |
| `detalle_venta_lote` | De qué lotes salió cada línea de venta (trazabilidad PEPS) |
| `movimiento_inventario` | Kardex: cada entrada y salida con su origen |
| `bitacora` | Registro de acciones de los usuarios |

### Decisiones de diseño que el código debe respetar

1. **La existencia se CALCULA, no se guarda.** Es la suma de
   `lote.cantidad_disponible`. Léela desde la vista `v_existencia`. Nunca
   agregues una columna `existencia` a `producto`

2. **Borrado lógico.** `producto`, `proveedor`, `cliente`, `categoria` y
   `usuario` tienen `activo`. Nunca se hace `DELETE` sobre ellos: el enunciado
   exige conservar el historial aunque dejen de usarse

3. **Precio histórico.** `detalle_venta.precio_unitario` guarda el precio al
   momento de vender. Una factura vieja no cambia si el precio cambia después

4. **Columnas generadas.** `detalle_compra.subtotal` y `detalle_venta.subtotal`
   son `GENERATED ALWAYS`. En JPA: `@Column(insertable = false, updatable = false)`

5. **Restricciones en la base.** Los `CHECK` (lote no negativo, total = subtotal
   + IVA, cantidades positivas) son la **última línea de defensa**. El backend
   valida primero para dar mensajes claros, pero la base garantiza la
   integridad aunque el backend falle

6. **Número de factura.** Lo asigna la secuencia `seq_numero_factura` (empieza
   en 1001). En JPA: no insertable; leerlo después del `save` + `flush`

### Usuarios de la carga inicial

Contraseña de todos: **`Erp2026!`**

| Usuario | Rol |
|---|---|
| `admin` | ADMINISTRACION |
| `compras` | COMPRAS |
| `inventario` | INVENTARIO |
| `ventas` | VENTAS |

---

## 5. Reglas de negocio centrales

### 5.1 Método de inventario: PEPS

El carné del estudiante es **par → PEPS** (primeras entradas, primeras salidas).

**Al registrar una COMPRA**, por cada línea:
1. Insertar `detalle_compra`
2. Crear un `lote` con `cantidad_inicial = cantidad_disponible = cantidad` y el
   costo de esa línea
3. Insertar un `movimiento_inventario` tipo `ENTRADA`, origen `COMPRA`, con la
   existencia resultante

**Al registrar una VENTA**, por cada línea:
1. Verificar que la existencia alcance. Si no, rechazar **toda** la venta
2. Buscar los lotes del producto con `cantidad_disponible > 0`, **ordenados por
   `fecha_ingreso` ascendente y luego `id` ascendente** (el más antiguo primero)
3. Consumir del primero; si no alcanza, pasar al siguiente
4. Por cada lote tocado: descontar `cantidad_disponible`, insertar un
   `detalle_venta_lote` y un `movimiento_inventario` tipo `SALIDA`, origen `VENTA`
5. Calcular subtotal, **IVA del 12 %** (Guatemala) y total

**Toda la venta es UNA transacción** (`@Transactional`). Si cualquier línea
falla, no queda nada escrito.

### 5.2 Concurrencia al vender

Dos vendedores podrían vender el último producto al mismo tiempo. Al leer los
lotes para consumirlos usa **bloqueo pesimista** (`SELECT ... FOR UPDATE`; en
JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)`). Coméntalo: es exactamente el
problema de acceso concurrente estudiado en clase.

### 5.3 Otras reglas

- No se permiten existencias negativas ni cantidades o montos inválidos
- Una compra solo puede incluir productos que ese proveedor surte
  (`producto_proveedor`). Rechaza la línea si no existe la relación
- No se puede vender un producto inactivo ni a un cliente inactivo
- Al desactivar un producto, su historial se conserva intacto
- Las alertas de stock salen de `v_alerta_stock` (existencia ≤ stock mínimo)

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

## 7. Reportes requeridos

SQL nativo en el repositorio (`@Query(nativeQuery = true)` o `JdbcTemplate`),
devolviendo DTO.

**Productos e inventario**
1. Top 10 productos más vendidos (por unidades)
2. Productos con menor existencia disponible
3. Productos con mayor cantidad de movimientos
4. Historial de movimientos de un producto seleccionado (kardex)

**Compras y proveedores**
5. Compras realizadas dentro de un rango de fechas
6. Top 5 proveedores con mayor monto acumulado de compras
7. Productos adquiridos con mayor frecuencia

**Ventas y clientes**
8. Ventas realizadas dentro de un rango de fechas
9. Top 10 clientes con mayor monto acumulado de compras
10. Top 10 productos que han generado mayores ingresos
11. Resumen de ventas agrupado por período (día, semana, mes o año, como
    parámetro; usar `date_trunc`)

**Logs**
12. Interacciones de los usuarios por módulo, con filtros de usuario, módulo y
    rango de fechas

---

## 8. Convenciones de código

- **Idioma:** clases, métodos, variables y comentarios **en español**,
  coherente con la base (`Producto`, `registrarVenta`, `cantidadDisponible`)
- **DTO:** siempre `record`. Nunca expongas una entidad JPA en la API: evita
  ciclos de serialización y fugas de datos como `password_hash`
- **Montos:** siempre `BigDecimal`, nunca `double`. `HALF_UP` a 2 decimales
- **Validación:** Jakarta Validation (`@NotBlank`, `@Positive`, `@Size`) en los
  DTO de entrada
- **Errores:** un `@RestControllerAdvice` que devuelva JSON consistente:
  ```json
  { "estado": 400, "error": "Existencia insuficiente", "detalle": "...", "fecha": "..." }
  ```
- **Bitácora:** registrar login (exitoso y fallido), creación, actualización,
  desactivación, compras, ventas y generación de facturas

---

## 9. Cómo trabajar en cada fase

El estudiante te pedirá, por ejemplo: *"Ejecuta fases/FASE-3-catalogos.md"*.

1. Lee **este archivo** y **el archivo de la fase** completos antes de empezar
2. Verifica que la fase anterior esté terminada (sección *Requisito* de cada fase)
3. Implementa solo lo que la fase pide
4. **Compila y ejecuta** con Java 21 exportado
5. **Prueba** con los comandos de verificación de la fase
6. Si algo falla, arréglalo antes de reportar
7. Al terminar reporta: qué hiciste, qué decisiones tomaste y cómo se verificó
8. Sugiere el mensaje de commit
9. **Detente** y espera confirmación

---

## 10. Índice de fases

| # | Archivo | Contenido |
|:-:|---|---|
| 0 | `fases/FASE-0-entorno.md` | Instalar y configurar Java, Node, git y PostgreSQL |
| 1 | `fases/FASE-1-backend-base.md` | Proyecto Spring Boot, Flyway, entidades y repositorios |
| 2 | `fases/FASE-2-seguridad.md` | Login con JWT y control de acceso por roles |
| 3 | `fases/FASE-3-catalogos.md` | Categorías, productos, proveedores y clientes |
| 4 | `fases/FASE-4-compras.md` | Compras, lotes y kardex |
| 5 | `fases/FASE-5-ventas-peps.md` | ⭐ Ventas con PEPS y factura en PDF |
| 6 | `fases/FASE-6-reportes.md` | Los 12 reportes y la bitácora |
| 7 | `fases/FASE-7-frontend.md` | Aplicación Angular completa |
| 8 | `fases/FASE-8-entrega.md` | Diagramas, manuales y entrega en GitHub |

Las fases son **secuenciales**: cada una asume terminada la anterior.
