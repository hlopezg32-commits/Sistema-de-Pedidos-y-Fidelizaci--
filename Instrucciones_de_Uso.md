# Subnotway — Instrucciones de Instalación y Uso

Proyecto académico oficial para el curso de **Programación II** (Universidad Mariano Gálvez de Guatemala).  
Sistema de Pedidos, Facturación y Fidelización con plataforma Web MVC y aplicación móvil Android.

---

## Contenido del Paquete

1. **`Subnotway/`**: Aplicación Web administrativa y de punto de venta (Spring Boot 3.4, Java 17+, Thymeleaf, OpenPDF, MySQL).
2. **`Subnotway-Android/`**: Aplicación móvil nativa en Java para consulta en tiempo real de puntos acumulados.
3. **`pan_puntos_premios.sql`**: Script SQL consolidado con todas las tablas del sistema, datos de prueba, menú y catálogo oficial de recompensas.
4. **`Diagrama_Clases_UML.png`**: Diagrama de clases UML con la arquitectura completa en capas (Modelos, DAOs, Servicios, Controladores, DTOs).
5. **`Diagrama_Entidad_Relacion.pdf`**: Diagrama Entidad-Relación oficial del modelo relacional de base de datos en MySQL Workbench.
6. **`Guia_Defensa_Proyecto.md`**: Guía exhaustiva con respuestas técnicas, justificaciones de arquitectura y preguntas clave para la defensa oral del proyecto.

---

## Paso 1: Configurar la Base de Datos (MySQL / XAMPP)

1. Abre el panel de control de **XAMPP** e inicia los servicios **Apache** y **MySQL**.
2. Ingresa a phpMyAdmin desde tu navegador: `http://localhost/phpmyadmin`.
3. Ve a la pestaña **Importar**.
4. Haz clic en **Seleccionar archivo** y elige `pan_puntos_premios.sql` (ubicado en la raíz de este proyecto).
5. Haz clic en **Importar** al final de la página.
6. Se creará automáticamente la base de datos `pan_puntos_premios` con las 12 tablas relacionales y datos iniciales de prueba (clientes, productos con stock, combos y recompensas oficiales de 10, 15, 30 y 50 puntos).

---

## Paso 2: Ejecutar el Servidor Web (Spring Boot)

### Opción A (Recomendada y Rápida):
- Ingresa a la carpeta `Subnotway/` y haz doble clic sobre el archivo `ejecutar_servidor.bat`.
- El script detectará automáticamente tu configuración de Maven o NetBeans y levantará el servidor web.

### Opción B (Desde tu IDE favorito):
- Abre **Apache NetBeans**, **IntelliJ IDEA** o **VS Code**.
- Selecciona **Open Project** y selecciona la carpeta `Subnotway`.
- Haz clic derecho sobre el proyecto y selecciona **Run Project** (o ejecuta la clase principal `SubnotwayApplication.java`).

### Opción C (Desde la terminal):
- Abre una consola o PowerShell en la carpeta `Subnotway/` y ejecuta:
  ```bash
  mvn spring-boot:run
  ```

Una vez iniciado el servidor, abre tu navegador web en:  
👉 **`http://localhost:8080`**

### Módulos Web Disponibles:
- **Inicio / Dashboard:** `http://localhost:8080/`
- **Punto de Venta (POS):** `http://localhost:8080/ventas`  
  *(Permite seleccionar clientes, agregar sándwiches o menús al carrito, calcular cambio, acumular puntos y marcar pedidos como entregados).*
- **Productos e Inventario:** `http://localhost:8080/productos`  
  *(Catálogo, creación, edición, estado activo/inactivo y reabastecimiento de stock con registro de auditoría).*
- **Clientes:** `http://localhost:8080/clientes`  
  *(Registro de nuevos clientes, edición, consulta de saldo de puntos y acceso directo a su historial).*
- **Fidelización y Canjes:** `http://localhost:8080/fidelizacion`  
  *(Consulta interactiva por cliente, catálogo de recompensas con validación de stock y saldo, y canje transaccional seguro).*
- **Centro de Reportes PDF:** `http://localhost:8080/reportes`  
  *(Generación y descarga dinámica en PDF de los 6 reportes empresariales requeridos por la rúbrica).*
- **Endpoint API REST:** `http://localhost:8080/api/clientes/puntos/{identificador}`  
  *(Retorna en formato JSON el saldo y estado del cliente para la app móvil).*

---

## Paso 3: Ejecutar la Aplicación Móvil (Android Studio)

1. Abre **Android Studio**.
2. Selecciona **File > Open** y elige la carpeta `Subnotway-Android`.
3. Espera que finalice la sincronización de dependencias Gradle.
4. Conecta un dispositivo físico con depuración USB activada o inicia un Emulador (ej. Pixel 6 / API 34).
5. Haz clic en el botón verde **Run 'app'** (icono de Play).
6. Una vez abierta la app en el dispositivo:
   - Ingresa un identificador de prueba: `CL-001` (o el NIT `12345678-9`).
   - Presiona el botón **Consultar Saldo**.
   - La aplicación mostrará el nombre del cliente, su estado (`ACTIVO`) y su balance de puntos en tiempo real.

### Notas de Conectividad Móvil:
- **Si usas el Emulador de Android Studio:** Deja el campo *Servidor API* con el valor predeterminado `http://10.0.2.2:8080`. Es la dirección especial que Android asigna para comunicarse con el `localhost` de tu computadora.
- **Si usas un Teléfono Físico por Wi-Fi:** Escribe en el campo *Servidor API* la dirección IP local de tu computadora en la red Wi-Fi (por ejemplo: `http://192.168.1.15:8080`). Asegúrate de que ambos dispositivos estén conectados a la misma red Wi-Fi.

---

## Datos de Prueba Rápidos para la Calificación

| Tipo de Dato | Valor de Prueba | Descripción |
| :--- | :--- | :--- |
| **Cliente Principal** | `CL-001` / `12345678-9` | Juan Carlos Pérez (Saldo alto para probar canjes). |
| **Cliente Secundario**| `CL-002` / `87654321-0` | María Mercedes Gómez. |
| **Recompensa 10 pts** | Bebida Gratuita | Descuenta 10 pts y 1 unidad del producto #4. |
| **Recompensa 15 pts** | Acompañamiento Gratuito | Descuenta 15 pts y 1 unidad del producto #7. |
| **Recompensa 30 pts** | Sándwich Gratuito | Descuenta 30 pts y 1 unidad del producto #1. |
| **Recompensa 50 pts** | Menú Gratuito | Descuenta 50 pts y 1 unidad del menú #1. |
