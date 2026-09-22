# Mini ERP — Muebles del Hogar y Oficina

Sistema de gestión comercial: productos, proveedores, compras, inventario,
clientes y ventas, con control de acceso por roles y método de inventario
PEPS (primeras entradas, primeras salidas).

Proyecto 1 del curso Manejo e Implementación de Archivos.

## Arquitectura

```
┌──────────────┐    HTTP/JSON     ┌──────────────────┐    JDBC    ┌─────────────┐
│  Angular 21  │ ───────────────► │  Spring Boot 4   │ ─────────► │ PostgreSQL  │
│  (frontend)  │ ◄─────────────── │  Java 21         │ ◄───────── │      18     │
│  :4200       │    API REST      │  (backend) :8080 │            │    :5432    │
└──────────────┘                  └──────────────────┘            └─────────────┘
```

Detalles de arquitectura, modelo de datos, decisiones de diseño y la lista
completa de endpoints: [`docs/MANUAL-TECNICO.md`](docs/MANUAL-TECNICO.md).
Guía de pantallas por rol: [`docs/MANUAL-USUARIO.md`](docs/MANUAL-USUARIO.md).

## Cómo ejecutarlo

### Requisitos

- JDK 21
- Node.js ≥ 20.19 y npm
- PostgreSQL 18 (con autenticación por contraseña)

### Base de datos

```bash
sudo -u postgres psql -c "CREATE ROLE erp LOGIN PASSWORD 'erp';"
sudo -u postgres psql -c "CREATE DATABASE mini_erp OWNER erp;"
```

Las tablas y la carga inicial las crea Flyway automáticamente al arrancar el
backend (migraciones en `backend/src/main/resources/db/migration/`).

### Backend

```bash
cd backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw spring-boot:run
```

→ `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install --legacy-peer-deps
npx ng serve
```

→ `http://localhost:4200`

## Usuarios de prueba

Contraseña de todos: **`Erp2026!`**

| Usuario | Rol | Puede |
|---|---|---|
| `admin` | ADMINISTRACION | Todo |
| `compras` | COMPRAS | Proveedores, compras, inventario (con alertas), reportes de compras |
| `inventario` | INVENTARIO | Categorías, productos, inventario, reportes de inventario |
| `ventas` | VENTAS | Clientes, ventas y facturas, inventario (solo lectura), reportes de ventas |

## Estructura del repositorio

```
├── CLAUDE.md                     Contexto del proyecto para el asistente
├── fases/                        Instrucciones por fase (no se sube al repo)
├── migraciones-iniciales/        Los 3 .sql originales (esquema, datos, vistas)
├── backend/                      Spring Boot (Maven)
├── frontend/                     Angular 21
└── docs/
    ├── diagramas/                Diagramas .drawio (ER, casos de uso, secuencia, despliegue)
    ├── MANUAL-TECNICO.md
    └── MANUAL-USUARIO.md
```
