# Hospedaje Quispe - Backend API 🏨⛏️

Sistema de gestión web de alquiler de habitaciones para un hospedaje independiente ubicado en una zona rural minera en Perú.

## 🛠️ Tecnologías y Arquitectura
- **Lenguaje:** Java 21 LTS
- **Framework:** Spring Boot 3.4.3
- **Gestor de Dependencias:** Maven (con Maven Wrapper `mvnw`)
- **Persistencia:** Spring Data JPA / Hibernate
- **Base de Datos:** MySQL 8.0 (`hotel_quispe_db`)
- **Validación:** Jakarta Validation (`spring-boot-starter-validation`)
- **Productividad:** Project Lombok, Spring Boot DevTools
- **Manejo de Archivos:** Subida local multipart para comprobantes / vouchers (Yape, Transferencia)

## 📌 Módulos Funcionales
1. **Habitaciones:**
   - Precios mensuales directamente editables por cuarto y piso.
   - Control de estados en tiempo real (`DISPONIBLE`, `OCUPADA`, `MANTENIMIENTO`).
2. **Huéspedes:**
   - Registro de datos personales: DNI, nombres, apellidos, teléfono y correo opcional.
   - Validación de unicidad por documento de identidad.
3. **Contratos Indefinidos:**
   - Asignación de huésped a habitación con día de pago mensual de ciclo.
   - Emisión automática del primer recibo al aperturar contrato.
   - Finalización de contrato con liberación inmediata del cuarto a `DISPONIBLE`.
4. **Cobros y Mensualidades:**
   - Registro de cobros con métodos: **Efectivo**, **Transferencia Bancaria** y **Yape**.
   - Subida y visualización de fotos/comprobantes de pago (vouchers).
   - **Pagos en Partes (Abonos Parciales):** seguimiento de monto pagado y saldo pendiente con estado `PARCIAL`.
   - **Pagos Adelantados Multi-Mes:** emisión y cancelación masiva de 1, 2, 3 hasta 12 meses anticipados.
   - **Servicios Adicionales Editables:** CRUD para tarifas fijas de servicios (Internet Fibra, Limpieza, Luz extra, Cochera).
5. **Dashboard & Alertas:**
   - Indicadores KPI (ocupación, deudas, moras).
   - Detección de pagos atrasados y próximos vencimientos (< 5 días).
   - Enlaces directos para cobranza por **WhatsApp** y llamada telefónica.

## ⚙️ Configuración y Variables de Entorno
El archivo `src/main/resources/application.properties` está preparado con variables de entorno y valores predeterminados para desarrollo local:

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_HOST` | Host de la base de datos MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Nombre de la base de datos | `hotel_quispe_db` |
| `DB_USER` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | `1234` |
| `server.port` | Puerto de escucha del backend | `8080` |

## 🚀 Ejecución Local

### Prerrequisitos
- Java Development Kit (JDK) 21 instalado.
- MySQL Server 8.0 en ejecución.

### Iniciar el servidor
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

El servidor iniciará en: `http://localhost:8080`
Los datos de prueba iniciales se inicializan automáticamente en la primera ejecución (`DataInitializer.java`).
