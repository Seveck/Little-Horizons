# 1. Little Horizons ERP

Sistema de Planificación de Recursos Empresariales (ERP) en consola para la gestión integral del **Colegio Internacional Little Horizons S.A.S.**, desarrollado en Java bajo arquitectura **Modelo - Vista - Controlador (MVC)** conectado a **MySQL 8.0+** mediante JDBC.

---

## 2. Descripción

### Resumen del Propósito
Centralizar en un solo sistema la operación académica, extracurricular, financiera y de préstamos de recursos educativos del colegio, eliminando registros dispersos en papel y hojas de cálculo.

### Problemas que Resuelve
- **Pérdida de información académica:** Persiste notas (`0.0` a `5.0`), inscripciones y asistencias diarias con integridad referencial en MySQL.
- **Desorganización extracurricular:** Controla cupos máximos en clubes y resultados en eventos/competencias.
- **Caos en la facturación:** Registra pagos con descuentos por becas/hermanos, detecta morosos (`> 30` días) y genera facturas electrónicas simuladas DIAN en `.txt` (`invoices/factura_XXXXXX.txt`).
- **Pérdida de recursos:** Administra inventario prestable de biblioteca y laboratorios con préstamos transaccionales a estudiantes y profesores.
- **Baja visibilidad docente:** Calcula promedios e identifica alertas semanales de inasistencia (`> 3` ausencias en la misma semana).

### Funcionalidades Principales
- Gestión completa de estudiantes, acudientes (con soporte para hermanos), matrículas, profesores, horarios sin cruce de clases, materias, notas y asistencias.
- Catálogo e inscripción transaccional en clubes con control de cupo y seguimiento de eventos competitivos.
- Registro transaccional de pagos, seguimiento de morosos y emisión automática de facturas `.txt` con CUFE SHA-256 y QR simulado.
- Préstamos y devoluciones transaccionales de recursos a estudiantes y docentes con detección de vencidos.
- Tablero global de reportes y estadísticas calculadas con Java Stream API.

---

## 3. Autores

- **Nombre:** Seveck
- **Repositorio:** [https://github.com/Seveck/Little-Horizons](https://github.com/Seveck/Little-Horizons)
- **Contacto:** Ver sección [16. Contacto](#16-contacto)

---

## 4. Tecnologías Utilizadas

- **Lenguaje de programación:** Java 27
- **IDE utilizado:** IntelliJ IDEA
- **Base de datos:** MySQL 8.0+
- **Librería externa:** MySQL Connector/J `8.4.0` (`com.mysql:mysql-connector-j`) vía Maven (`pom.xml`)
- **Procesamiento en memoria:** Java Stream API y expresiones Lambda
- **Generación de facturas en texto plano:** Implementación nativa en Java (`TxtInvoiceGenerator`) generando archivos `.txt` formateados sin dependencias externas adicionales

---

## 5. Requerimientos Previos

- **JDK 27** (o superior compatible con `pom.xml`)
- **Maven 3.8+** (o gestor integrado de IntelliJ IDEA)
- **MySQL 8.0 o superior** (REQUERIDO para soportar `CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0)` y cotejamiento `utf8mb4_unicode_ci`)
- **Driver JDBC:** `mysql-connector-j:8.4.0`

---

## 6. Configuración de la Base de Datos

### 6.1. Creación de la base de datos
Ejecuta el script completo ubicado en [docs/database.sql](docs/database.sql) (basado en el esquema oficial del anexo del proyecto con ajustes justificados para horarios docentes y seguimiento financiero de mora/descuentos, y con al menos 30 registros por tabla principal):

```bash
mysql -h 127.0.0.1 -P 3308 -u root -p --default-character-set=utf8mb4 < docs/database.sql
```

*(Si usas Docker de forma dedicada para este proyecto)*:
```bash
docker run -d --name little_horizons_mysql \
  -e MYSQL_ROOT_PASSWORD=admin123 \
  -p 3308:3306 mysql:8.0
```

### 6.2. Configuración de credenciales
Copia la plantilla `config.example.txt` como `config.txt` en la raíz del proyecto (archivo ignorado en `.gitignore` para no exponer credenciales en el repositorio):

```properties
db.url=jdbc:mysql://localhost:3308/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8
db.user=tu_usuario
db.password=tu_contraseña
```

### 6.3. Instalación del driver JDBC
El driver `com.mysql:mysql-connector-j:8.4.0` se descarga automáticamente mediante `pom.xml` al recargar el proyecto Maven en IntelliJ IDEA.

---

## 7. Instalación y Ejecución

### 7.1. Clonar el repositorio
```bash
git clone https://github.com/Seveck/Little-Horizons.git
cd Little-Horizons
```

### 7.2. Abrir el proyecto en el IDE
Abre la carpeta `Little-Horizons` en **IntelliJ IDEA** como proyecto Maven y espera a que sincronice `pom.xml`.

### 7.3. Compilar
Desde IntelliJ IDEA (`Build -> Build Project`), con Maven:
```bash
mvn compile
```
O manualmente con `javac`:
```bash
javac -cp /ruta/a/mysql-connector-j-8.4.0.jar -d out $(find src/main/java -name "*.java")
```

### 7.4. Ejecutar
Ejecuta la clase principal `org.example.Main` desde el IDE o desde terminal:
```bash
java -cp "out:/ruta/a/mysql-connector-j-8.4.0.jar" org.example.Main
```

---

## 8. Estructura del Proyecto

El sistema implementa arquitectura estricta **MVC** separando presentación, lógica de negocio, acceso a datos y entidades:

```text
Little-Horizons/
├── config.example.txt              # Plantilla de conexión JDBC a MySQL
├── docs/
│   └── database.sql                # Script DDL (22 tablas) + DML (>=30 filas por tabla principal)
├── invoices/                       # Carpeta de salida para facturas electrónicas .txt
├── pom.xml                         # Configuración Maven (Java 27 + mysql-connector-j)
└── src/main/java/org/example/
    ├── Main.java                   # Menú principal interactivo
    ├── model/                      # Entidades POJO y enums mapeados exactamente a MySQL
    ├── dao/                        # Acceso JDBC con PreparedStatement, try-with-resources y transacciones
    ├── controller/                 # Reglas de negocio, cálculos con Streams y coordinación
    ├── view/                       # Vistas de consola por módulo y validación de entradas
    └── util/                       # DatabaseConnection, ErrorLogger, InputValidator, TxtInvoiceGenerator
```

---

## 9. Principios y Patrones de Diseño Aplicados

### 9.1. Principios SOLID
- **Responsabilidad Única (SRP):** Cada clase cumple una única función clara (ej. `ErrorLogger` solo escribe errores, `TxtInvoiceGenerator` solo construye archivos `.txt`, cada DAO gestiona una entidad/módulo).
- **Abierto/Cerrado (OCP):** El uso de `enum` tipados con `getDbValue()` (`EventType`, `PaymentMethod`, `AttendanceStatus`, `ResourceStatus`) facilita extender comportamientos sin alterar la lógica SQL existente.
- **Inversión de Dependencias y Separación por Capas:** Las vistas (`*View`) delegan completamente la lógica en los controladores (`*Controller`) y nunca ejecutan SQL directamente.

### 9.2. Patrones de Diseño
- **MVC (Modelo - Vista - Controlador):** Separa la interfaz de consola (`view/`) de las reglas de negocio (`controller/`) y de los datos (`model/` + `dao/`).
- **Singleton (`DatabaseConnection`):** Centraliza la lectura de `config.txt` y administra la conexión JDBC activa mediante `getInstance()`.
- **DAO (Data Access Object):** Encapsula todas las consultas SQL con `PreparedStatement` y transacciones (`setAutoCommit(false)`, `commit()`, `rollback()`) dentro del paquete `dao/`.

---

## 10. Funcionalidades Principales

1. **Módulo de Estudiantes y Matrícula (`StudentView` / `StudentController`):** Registro transaccional estudiante-acudiente, soporte para hermanos bajo un mismo acudiente, búsqueda por nombre o documento, cambio de estado y matrícula por grado/año.
2. **Módulo de Profesores y Materias (`TeacherView` / `TeacherController`):** Directorio docente, catálogo de materias por grado, asignación académica con detección de conflictos de horario e inscripción de estudiantes por período.
3. **Módulo Académico y Asistencias (`AcademicView` / `AcademicController`):** Registro de notas (`0.0`–`5.0`), historial completo multi-año, cálculo automático con Streams de promedios por materia/período/curso, control diario de asistencia y alertas por `> 3` ausencias semanales.
4. **Módulo Extracurricular (`ExtracurricularView` / `ExtracurricularController`):** Gestión de clubes con validación transaccional de cupo máximo, calendario de eventos por tipo y registro de posiciones competitivas.
5. **Módulo Financiero y Facturación DIAN en TXT (`FinancialView` / `FinancialController`):** Catálogo de conceptos de pago, descuentos por becas/hermanos, pagos transaccionales con emisión automática de factura `.txt` DIAN (`invoices/factura_XXXXXX.txt`, guardada en la columna `nombre_archivo_pdf` de `facturas`), historial consolidado por acudiente y reporte de morosos (`> 30` días).
6. **Módulo de Recursos (`ResourceView` / `ResourceController`):** Inventario por categoría/ubicación/estado, préstamos y devoluciones transaccionales a estudiantes y profesores, actualización automática de estado (`Disponible`/`Prestado`) y alertas de vencidos.
7. **Módulo de Reportes Globales (`ReportView` / `ReportController`):** Resumen ejecutivo institucional e informes consolidados de los 4 módulos.

---

## 11. Manejo de Errores y Logs

- **Ubicación del log:** `error_log.txt` en la raíz del proyecto (se crea automáticamente al ocurrir la primera excepción; ignorado por `.gitignore`).
- **Clase responsable:** `org.example.util.ErrorLogger`.
- **Estrategia:** Todas las excepciones (`SQLException`, `IOException`, entradas inválidas) se capturan con `try-catch` sin cerrar abruptamente la aplicación ni mostrar trazas crudas al usuario; los detalles técnicos (fecha/hora, clase, método, mensaje y stack trace) se guardan en `error_log.txt`.

---

## 12. Capturas de Pantalla (opcional)

*(Espacio reservado para evidencias de ejecución en consola y apertura de facturas `.txt` generadas en `invoices/`.)*

---

## 13. Problemas Conocidos y Limitaciones

- La validación de horarios detecta cruces directos por días y rango horario asignado a un mismo profesor dentro del mismo año académico.
- La firma CUFE y resolución DIAN en las facturas `.txt` son simuladas con hash SHA-256 para fines educativos.

---

## 14. Futuras Mejoras

- Interfaz gráfica (JavaFX) o API REST manteniendo intactos controladores, DAOs y modelos.
- Envío automático por correo electrónico de las facturas `.txt` generadas al acudiente responsable.

---

## 15. Licencia

Proyecto académico desarrollado para fines educativos en **Little Horizons ERP**.

---

## 16. Contacto

- **Autor:** Seveck
- **GitHub:** [https://github.com/Seveck/Little-Horizons](https://github.com/Seveck/Little-Horizons)
