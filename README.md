# Little Horizons ERP

Sistema de Planificación de Recursos Empresariales (ERP) en consola desarrollado en Java con arquitectura MVC y conexión JDBC a MySQL 8.0 para el **Colegio Internacional Little Horizons**.

## Tabla de Contenidos

1. [Descripción del Proyecto](#descripción-del-proyecto)
2. [Tecnologías Utilizadas](#tecnologías-utilizadas)
3. [Arquitectura del Sistema (MVC)](#arquitectura-del-sistema-mvc)
4. [Base de Datos (MySQL 8.0)](#base-de-datos-mysql-80)
5. [Requisitos Previos](#requisitos-previos)
6. [Instalación y Configuración](#instalación-y-configuración)
7. [Ejecución del Proyecto](#ejecución-del-proyecto)
8. [Módulos y Funcionalidades](#módulos-y-funcionalidades)
9. [Facturación Electrónica DIAN (.txt)](#facturación-electrónica-dian-txt)
10. [Registro de Errores (Log)](#registro-de-errores-log)

## Descripción del Proyecto

Centraliza la gestión académica, extracurricular, financiera y de recursos del colegio (estudiantes, acudientes, matrículas, profesores, horarios, calificaciones, asistencias, clubes, eventos, pagos, facturación simulada DIAN y préstamos de biblioteca/laboratorios).

## Tecnologías Utilizadas

* **Java 27** (`pom.xml` configurado con `maven.compiler.source=27` y `maven.compiler.target=27`)
* **Maven** para gestión de dependencias
* **MySQL 8.0** (contenedor Docker dedicado o instancia local)
* **MySQL Connector/J `8.4.0`** (`com.mysql:mysql-connector-j`)
* **Java Stream API y Lambdas** para cálculos, filtros y estadísticas en memoria

## Arquitectura del Sistema (MVC)

El proyecto sigue el patrón **Modelo - Vista - Controlador (MVC)** acompañado de una capa **DAO** para el acceso a datos:

```text
Little-Horizons/
├── config.example.txt          # Plantilla de conexión a MySQL (copiar como config.txt)
├── docs/
│   └── database.sql            # Script completo DDL + datos iniciales (22 tablas)
├── pom.xml
└── src/main/java/org/example/
    ├── Main.java               # Punto de entrada y menú principal
    ├── model/                  # 22 clases POJO y 17 enums alineados con SQL
    ├── dao/                    # Acceso JDBC con PreparedStatement y transacciones
    ├── controller/             # Lógica de negocio, validaciones y Java Streams
    ├── view/                   # Menús e interacción por consola
    └── util/
        ├── DatabaseConnection.java   # Singleton de conexión leyendo config.txt
        ├── ErrorLogger.java          # Registro de excepciones en error_log.txt
        ├── InputValidator.java       # Validación de entradas en consola
        └── TxtInvoiceGenerator.java  # Generador de facturas DIAN en formato .txt
```

## Base de Datos (MySQL 8.0)

El esquema `erp_little_horizons` definido en `docs/database.sql` contiene **22 tablas** organizadas en 4 bloques:

1. **Académico y Personal:** `grados`, `estudiantes`, `acudientes`, `estudiante_acudiente`, `matriculas`, `materias`, `profesores`, `asignaciones_profesor` (con días y rango horario), `inscripciones_materia`, `registros_academicos` (notas `0.0`–`5.0`), `asistencias`.
2. **Actividades Extracurriculares:** `clubes`, `membresias_club`, `eventos`, `participantes_evento`.
3. **Financiero:** `conceptos_pago`, `pagos` (con `numero_recibo`), `facturas`.
4. **Recursos y Préstamos:** `categorias_recurso`, `recursos`, `prestamos_estudiante`, `prestamos_profesor`.

## Requisitos Previos

* JDK 27 (o versión compatible configurada en tu IDE)
* Maven 3.8+
* MySQL 8.0+ (local o mediante Docker)

## Instalación y Configuración

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Seveck/Little-Horizons.git
   cd Little-Horizons
   ```

2. **Levantar MySQL (ejemplo con contenedor Docker exclusivo en el puerto `3308`):**
   ```bash
   docker run -d --name little_horizons_mysql \
     -e MYSQL_ROOT_PASSWORD=admin123 \
     -e MYSQL_DATABASE=erp_little_horizons \
     -p 3308:3306 mysql:8.0
   ```

3. **Cargar la base de datos:**
   ```bash
   mysql -h 127.0.0.1 -P 3308 -u root -p < docs/database.sql
   ```

4. **Configurar credenciales locales:**
   Copia `config.example.txt` como `config.txt` (archivo ignorado por `.gitignore`) y escribe tus credenciales reales:
   ```properties
   db.url=jdbc:mysql://localhost:3308/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8
   db.user=tu_usuario
   db.password=tu_clave
   ```

## Ejecución del Proyecto

Compila con Maven o desde IntelliJ IDEA y ejecuta `org.example.Main`:

```bash
mvn compile
```

También puedes compilar con `javac` incluyendo el JAR de `mysql-connector-j-8.4.0.jar` en el classpath y ejecutar `org.example.Main`.

## Módulos y Funcionalidades

Al ejecutar `Main.java` se presenta el menú principal con 8 opciones:

1. **Gestión de Estudiantes (`StudentView`):** Registro con acudiente, soporte de múltiples estudiantes por acudiente (hermanos), matrículas por grado (`Preescolar` a `11°`) y cambios de estado.
2. **Gestión Académica (`AcademicView`):** Registro de notas (`0.0`–`5.0`), historial completo desde el ingreso o por año, promedios con Java Streams y reportes de inasistencias (`> 3` ausencias semanales).
3. **Gestión de Profesores (`TeacherView`):** Directorio docente, catálogo de materias, asignación con validación básica de conflictos de horario e inscripción de estudiantes en materias.
4. **Actividades Extracurriculares (`ExtracurricularView`):** Gestión de clubes con validación transaccional de cupo máximo, calendario de eventos y puestos en competencias.
5. **Gestión Financiera (`FinancialView`):** Catálogo de conceptos, descuentos personalizados, registro de pagos con número de recibo, historial por estudiante/acudiente y detección de morosos (`> 30` días).
6. **Gestión de Recursos (`ResourceView`):** Inventario de biblioteca/laboratorios, préstamos transaccionales a estudiantes y profesores, devoluciones y alertas de vencidos.
7. **Reportes y Estadísticas (`ReportView`):** Tablero ejecutivo global e informes consolidados de los 4 módulos.
8. **Salir:** Cierra la conexión JDBC y finaliza la aplicación de forma segura.

## Facturación Electrónica DIAN (.txt)

Al confirmar o saldar un pago desde `FinancialView`, `PaymentDao` y `TxtInvoiceGenerator` generan automáticamente dentro de la carpeta `invoices/` un comprobante formateado en texto plano (`invoices/factura_XXXXXX.txt`) con:

* Datos institucionales del colegio y resolución simulada DIAN
* Datos del acudiente responsable y del estudiante beneficiario
* Detalle del concepto, descuento, total pagado, CUFE calculado con SHA-256 y bloque QR simulado

## Registro de Errores (Log)

Todas las excepciones SQL, de entrada/salida o de ejecución inesperada capturadas por el sistema se registran mediante `ErrorLogger` en el archivo `error_log.txt` en la raíz del proyecto, conservando fecha, hora, clase, método, mensaje y *stack trace*.
