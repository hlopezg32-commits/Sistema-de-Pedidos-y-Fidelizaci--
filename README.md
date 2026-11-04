# Subnotway — Sistema de Pedidos y Fidelización

> **Universidad Mariano Gálvez de Guatemala (UMG)**  
> **Facultad de Ingeniería en Sistemas de Información**  
> **Curso:** Programación II  
> **Proyecto Final:** "Pan, Puntos y Premios — Sistema de Pedidos y Fidelización"

---

## 🥪 Acerca de Subnotway

**Subnotway** es una solución integral orientada a la administración de ventas, control de inventario y fidelización de clientes para cadenas de sándwiches y comida rápida.

El sistema está compuesto por dos plataformas interconectadas:
1. **Plataforma Web Administrativa y Punto de Venta (POS):** Desarrollada con **Java 17+**, **Spring Boot 3.4**, **Thymeleaf**, **Bootstrap 5** y **MySQL**.
2. **Aplicación Móvil para Clientes:** Aplicación nativa para **Android (Java)** que se comunica de forma desacoplada con el servidor mediante una **API REST** para consultar el saldo acumulado de puntos y estado de cuenta.

---

## 🏛️ Arquitectura del Sistema

El proyecto sigue una arquitectura en capas desacopladas con el patrón **Modelo-Vista-Controlador (MVC)** y **Data Access Object (DAO)**:

- **Capa de Presentación (Vista):** Plantillas HTML5 con Thymeleaf, Bootstrap 5 y responsive design.
- **Capa Controladora:** Controladores Spring Web MVC (`@Controller`) para las vistas administrativas y `@RestController` para el API móvil.
- **Capa de Negocio (Servicios):** `FidelizacionService`, `VentaServicio`, `ReportePdfService` y `ClienteServicio` implementando la lógica empresarial, cálculo de puntos y transacciones atómicas.
- **Capa de Acceso a Datos (DAO):** Implementaciones JDBC con `PreparedStatement` para garantizar seguridad contra inyecciones SQL (`ClienteDAO`, `ProductoDAO`, `PedidoDAO`, `MenuDAO`, `RecompensaDAO`, `CanjeDAO`, `ReporteDAO`).
- **Base de Datos Relacional:** MySQL con motor de almacenamiento InnoDB, claves foráneas e integridad referencial estricta.

---

## 🚀 Módulos del Sistema

### 1. Punto de Venta (POS) y Pedidos (`/ventas`)
- Búsqueda y selección interactiva de clientes activos.
- Carrito de compras en tiempo real con adición de sándwiches individuales o combos de menú.
- Cálculo automático de subtotal, total, cambio en efectivo y acumulación de puntos.
- Generación de comprobante digital y transición de estados del pedido (`PAGADO` $\rightarrow$ `ENTREGADO`).

### 2. Catálogo de Productos e Inventario (`/productos`)
- Gestión de ingredientes, panes, aderezos, bebidas y acompañamientos.
- Modificación de existencias con auditoría en la tabla `movimientos_inventario`.
- Alertas visuales para productos con existencias bajas.

### 3. Directorio de Clientes (`/clientes`)
- Registro y actualización de clientes con NIT o DPI único.
- Consulta de saldo actual de puntos.
- Visualización de historial de pedidos por cliente.

### 4. Sistema de Fidelización y Canjes (`/fidelizacion`)
- **Fórmula de Acumulación Oficial:**
  $$Puntos = (Sándwiches \times 2) + (Menús \times 8)$$
- Catálogo oficial de recompensas:
  - 🥤 **Bebida Gratuita:** 10 puntos
  - 🍟 **Acompañamiento Gratuito:** 15 puntos
  - 🥪 **Sándwich Gratuito:** 30 puntos
  - 🍱 **Menú Completo Gratuito:** 50 puntos
- Transacción atómica de canje: valida existencia de stock físico y saldo suficiente, descuenta inventario, descuenta puntos y genera auditoría en `canjes`, `movimientos_puntos` y `movimientos_inventario`.

### 5. Centro de Reportes Oficiales en PDF (`/reportes`)
Generación de documentos PDF descargables utilizando **OpenPDF**:
1. Ventas por período con totales e IVA.
2. Productos más vendidos.
3. Productos con existencias bajas (stock crítico).
4. Puntos acumulados por cliente.
5. Historial completo de canjes de recompensas.
6. Ventas detalladas de menús/combos.

### 6. Aplicación Móvil Android (`Subnotway-Android`)
- Desarrollada en **Java nativo** para Android con Material Design.
- Consulta en segundo plano (*background thread*) mediante `HttpURLConnection` y `org.json`.
- Compatibilidad con emulador Android (`http://10.0.2.2:8080`) y teléfonos físicos por red Wi-Fi local.

---

## 📂 Estructura de Entregables del Repositorio

| Archivo / Carpeta | Descripción |
| :--- | :--- |
| **`Subnotway/`** | Proyecto Web MVC en Spring Boot 3.4 (`src/`, `pom.xml`, `ejecutar_servidor.bat`). |
| **`Subnotway-Android/`** | Proyecto nativo de Android Studio (`app/`, `gradle/`, `build.gradle`, etc.). |
| **`pan_puntos_premios.sql`** | Script oficial de base de datos con las 12 tablas y datos iniciales de prueba. |
| **`Diagrama_Clases_UML.png`** | Diagrama de clases UML con la arquitectura completa en capas. |
| **`Diagrama_Entidad_Relacion.pdf`** | Diagrama Entidad-Relación de la base de datos MySQL Workbench. |
| **`Guia_Defensa_Proyecto.md`** | Guía de preguntas y respuestas técnicas para el examen oral. |
| **`Instrucciones_de_Uso.md`** | Manual detallado de instalación, configuración y ejecución paso a paso. |

---

## 🛠️ Puesta en Marcha Rápida

1. **Base de Datos:** Importa `pan_puntos_premios.sql` en phpMyAdmin (XAMPP).
2. **Servidor Web:** Ejecuta `ejecutar_servidor.bat` en la carpeta `Subnotway/` (o abre el proyecto en NetBeans/IntelliJ) y navega a `http://localhost:8080`.
3. **App Móvil:** Abre la carpeta `Subnotway-Android/` en Android Studio, compila en tu emulador y consulta el cliente `CL-001`.
