-- =============================================================================
-- PROYECTO: Little Horizons ERP - Sistema de Gestión Educativa
-- ARCHIVO: docs/database.sql
-- MOTOR: MySQL 8.0+
-- JUEGO DE CARACTERES: utf8mb4 (Collation: utf8mb4_unicode_ci)
-- ARQUITECTURA: MVC en Consola con JDBC
-- =============================================================================

-- =============================================================================
-- SECCIÓN 0: CREACIÓN Y SELECCIÓN DE BASE DE DATOS
-- =============================================================================
DROP DATABASE IF EXISTS `erp_little_horizons`;
CREATE DATABASE `erp_little_horizons`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `erp_little_horizons`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- SECCIÓN 1: DDL (DEFINICIÓN DE ESTRUCTURA Y TABLAS)
-- =============================================================================

-- 1. Tabla de Grados Escolares
DROP TABLE IF EXISTS `grados`;
CREATE TABLE `grados` (
    `id_grado` INT AUTO_INCREMENT PRIMARY KEY,
    `nombre_grado` VARCHAR(50) NOT NULL UNIQUE,
    `nivel` ENUM('Preescolar', 'Primaria', 'Secundaria', 'Media') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tabla de Estudiantes
DROP TABLE IF EXISTS `estudiantes`;
CREATE TABLE `estudiantes` (
    `id_estudiante` INT AUTO_INCREMENT PRIMARY KEY,
    `tipo_documento` ENUM('TI', 'CC', 'CE', 'Pasaporte') NOT NULL,
    `numero_documento` VARCHAR(20) NOT NULL UNIQUE,
    `nombres` VARCHAR(100) NOT NULL,
    `apellidos` VARCHAR(100) NOT NULL,
    `fecha_nacimiento` DATE NOT NULL,
    `genero` ENUM('M', 'F', 'Otro') NOT NULL,
    `direccion` VARCHAR(150) NOT NULL,
    `telefono` VARCHAR(20),
    `email` VARCHAR(100) UNIQUE,
    `estado` ENUM('Activo', 'Graduado', 'Retirado', 'Suspendido') NOT NULL DEFAULT 'Activo',
    `fecha_registro` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tabla de Acudientes / Padres de Familia
DROP TABLE IF EXISTS `acudientes`;
CREATE TABLE `acudientes` (
    `id_acudiente` INT AUTO_INCREMENT PRIMARY KEY,
    `tipo_documento` ENUM('CC', 'CE', 'Pasaporte') NOT NULL,
    `numero_documento` VARCHAR(20) NOT NULL UNIQUE,
    `nombres` VARCHAR(100) NOT NULL,
    `apellidos` VARCHAR(100) NOT NULL,
    `parentesco` VARCHAR(50) NOT NULL,
    `telefono` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `direccion` VARCHAR(150) NOT NULL,
    `profesion_ocupacion` VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tabla Intermedia Estudiante - Acudiente (Relación con soporte a hermanos)
DROP TABLE IF EXISTS `estudiante_acudiente`;
CREATE TABLE `estudiante_acudiente` (
    `id_estudiante` INT NOT NULL,
    `id_acudiente` INT NOT NULL,
    `es_principal` BOOLEAN NOT NULL DEFAULT TRUE,
    `relacion` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id_estudiante`, `id_acudiente`),
    CONSTRAINT `fk_ea_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_ea_acudiente` FOREIGN KEY (`id_acudiente`) REFERENCES `acudientes` (`id_acudiente`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Tabla de Matrículas Anuales
DROP TABLE IF EXISTS `matriculas`;
CREATE TABLE `matriculas` (
    `id_matricula` INT AUTO_INCREMENT PRIMARY KEY,
    `id_estudiante` INT NOT NULL,
    `id_grado` INT NOT NULL,
    `año_academico` INT NOT NULL,
    `fecha_matricula` DATE NOT NULL,
    `estado_matricula` ENUM('Matriculado', 'Promovido', 'Reprobado', 'Retirado') DEFAULT 'Matriculado',
    `costo_matricula` DECIMAL(10,2) NOT NULL,
    UNIQUE KEY `uk_estudiante_año` (`id_estudiante`, `año_academico`),
    CONSTRAINT `fk_mat_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_mat_grado` FOREIGN KEY (`id_grado`) REFERENCES `grados` (`id_grado`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Tabla de Materias / Asignaturas
DROP TABLE IF EXISTS `materias`;
CREATE TABLE `materias` (
    `id_materia` INT AUTO_INCREMENT PRIMARY KEY,
    `codigo_materia` VARCHAR(20) NOT NULL UNIQUE,
    `nombre_materia` VARCHAR(100) NOT NULL,
    `descripcion` TEXT,
    `intensidad_horaria` INT NOT NULL,
    `area` VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Tabla de Profesores / Docentes
DROP TABLE IF EXISTS `profesores`;
CREATE TABLE `profesores` (
    `id_profesor` INT AUTO_INCREMENT PRIMARY KEY,
    `tipo_documento` ENUM('CC', 'CE', 'Pasaporte') NOT NULL,
    `numero_documento` VARCHAR(20) NOT NULL UNIQUE,
    `nombres` VARCHAR(100) NOT NULL,
    `apellidos` VARCHAR(100) NOT NULL,
    `especialidad` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `telefono` VARCHAR(20) NOT NULL,
    `fecha_contratacion` DATE NOT NULL,
    `estado` ENUM('Activo', 'Inactivo', 'Licencia') DEFAULT 'Activo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Tabla de Asignaciones Académicas de Profesores
DROP TABLE IF EXISTS `asignaciones_profesor`;
CREATE TABLE `asignaciones_profesor` (
    `id_asignacion` INT AUTO_INCREMENT PRIMARY KEY,
    `id_profesor` INT NOT NULL,
    `id_materia` INT NOT NULL,
    `id_grado` INT NOT NULL,
    `seccion` VARCHAR(5) NOT NULL DEFAULT 'A',
    `año_academico` INT NOT NULL,
    `dias_horario` VARCHAR(60) NOT NULL DEFAULT 'Lunes',
    `hora_inicio` TIME NOT NULL DEFAULT '07:00:00',
    `hora_fin` TIME NOT NULL DEFAULT '08:30:00',
    UNIQUE KEY `uk_prof_mat_grado_sec_año` (`id_profesor`, `id_materia`, `id_grado`, `seccion`, `año_academico`),
    CONSTRAINT `fk_asig_profesor` FOREIGN KEY (`id_profesor`) REFERENCES `profesores` (`id_profesor`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_asig_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_asig_grado` FOREIGN KEY (`id_grado`) REFERENCES `grados` (`id_grado`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Tabla de Inscripciones de Estudiantes a Materias
DROP TABLE IF EXISTS `inscripciones_materia`;
CREATE TABLE `inscripciones_materia` (
    `id_inscripcion` INT AUTO_INCREMENT PRIMARY KEY,
    `id_estudiante` INT NOT NULL,
    `id_asignacion` INT NOT NULL,
    `año_academico` INT NOT NULL,
    `estado_inscripcion` ENUM('Cursando', 'Aprobada', 'Reprobada', 'Cancelada') DEFAULT 'Cursando',
    UNIQUE KEY `uk_est_asig_año` (`id_estudiante`, `id_asignacion`, `año_academico`),
    CONSTRAINT `fk_insc_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_insc_asignacion` FOREIGN KEY (`id_asignacion`) REFERENCES `asignaciones_profesor` (`id_asignacion`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Tabla de Registros Académicos (Calificaciones con CHECK 0.0 a 5.0)
DROP TABLE IF EXISTS `registros_academicos`;
CREATE TABLE `registros_academicos` (
    `id_registro` INT AUTO_INCREMENT PRIMARY KEY,
    `id_inscripcion` INT NOT NULL,
    `año_academico` INT NOT NULL,
    `periodo` ENUM('I', 'II', 'III', 'IV', 'Final') NOT NULL,
    `valor_nota` DECIMAL(2,1) NOT NULL,
    `porcentaje` DECIMAL(5,2) DEFAULT 25.00,
    `observaciones` VARCHAR(255),
    `fecha_registro` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `chk_valor_nota` CHECK (`valor_nota` >= 0.0 AND `valor_nota` <= 5.0),
    CONSTRAINT `fk_ra_inscripcion` FOREIGN KEY (`id_inscripcion`) REFERENCES `inscripciones_materia` (`id_inscripcion`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Tabla de Asistencias Diarias
DROP TABLE IF EXISTS `asistencias`;
CREATE TABLE `asistencias` (
    `id_asistencia` INT AUTO_INCREMENT PRIMARY KEY,
    `id_estudiante` INT NOT NULL,
    `id_asignacion` INT NOT NULL,
    `fecha` DATE NOT NULL,
    `estado_asistencia` ENUM('Presente', 'Ausente', 'Tardanza', 'Excusa Médica') NOT NULL,
    `observaciones` VARCHAR(255),
    UNIQUE KEY `uk_est_asig_fecha` (`id_estudiante`, `id_asignacion`, `fecha`),
    CONSTRAINT `fk_asis_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_asis_asignacion` FOREIGN KEY (`id_asignacion`) REFERENCES `asignaciones_profesor` (`id_asignacion`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Tabla de Clubes Extracurriculares
DROP TABLE IF EXISTS `clubes`;
CREATE TABLE `clubes` (
    `id_club` INT AUTO_INCREMENT PRIMARY KEY,
    `nombre_club` VARCHAR(100) NOT NULL UNIQUE,
    `descripcion` TEXT,
    `id_profesor_supervisor` INT NOT NULL,
    `capacidad_maxima` INT NOT NULL DEFAULT 30,
    `horario_reunion` VARCHAR(100),
    `lugar_reunion` VARCHAR(100),
    `estado` ENUM('Activo', 'Inactivo') DEFAULT 'Activo',
    CONSTRAINT `fk_club_supervisor` FOREIGN KEY (`id_profesor_supervisor`) REFERENCES `profesores` (`id_profesor`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Tabla de Membresías en Clubes
DROP TABLE IF EXISTS `membresias_club`;
CREATE TABLE `membresias_club` (
    `id_membresia` INT AUTO_INCREMENT PRIMARY KEY,
    `id_club` INT NOT NULL,
    `id_estudiante` INT NOT NULL,
    `fecha_ingreso` DATE NOT NULL,
    `estado` ENUM('Activa', 'Retirada') DEFAULT 'Activa',
    UNIQUE KEY `uk_club_estudiante` (`id_club`, `id_estudiante`),
    CONSTRAINT `fk_mem_club` FOREIGN KEY (`id_club`) REFERENCES `clubes` (`id_club`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_mem_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Tabla de Eventos Institucionales
DROP TABLE IF EXISTS `eventos`;
CREATE TABLE `eventos` (
    `id_evento` INT AUTO_INCREMENT PRIMARY KEY,
    `nombre_evento` VARCHAR(150) NOT NULL,
    `tipo_evento` ENUM('Académico', 'Cultural', 'Deportivo', 'Social', 'Científico') NOT NULL,
    `descripcion` TEXT,
    `fecha_evento` DATE NOT NULL,
    `lugar` VARCHAR(100) NOT NULL,
    `tiene_competencia` BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Tabla de Participantes en Eventos
DROP TABLE IF EXISTS `participantes_evento`;
CREATE TABLE `participantes_evento` (
    `id_participacion` INT AUTO_INCREMENT PRIMARY KEY,
    `id_evento` INT NOT NULL,
    `id_estudiante` INT NOT NULL,
    `puesto` ENUM('1er Lugar', '2do Lugar', '3er Lugar', 'Participante') DEFAULT 'Participante',
    `observaciones` VARCHAR(255),
    UNIQUE KEY `uk_evento_estudiante` (`id_evento`, `id_estudiante`),
    CONSTRAINT `fk_pe_evento` FOREIGN KEY (`id_evento`) REFERENCES `eventos` (`id_evento`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_pe_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Tabla de Conceptos de Pago
DROP TABLE IF EXISTS `conceptos_pago`;
CREATE TABLE `conceptos_pago` (
    `id_concepto` INT AUTO_INCREMENT PRIMARY KEY,
    `codigo_concepto` VARCHAR(20) NOT NULL UNIQUE,
    `nombre_concepto` VARCHAR(100) NOT NULL,
    `descripcion` TEXT,
    `monto_base` DECIMAL(12,2) NOT NULL,
    `es_periodico` BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Tabla de Pagos
DROP TABLE IF EXISTS `pagos`;
CREATE TABLE `pagos` (
    `id_pago` INT AUTO_INCREMENT PRIMARY KEY,
    `numero_recibo` VARCHAR(30) UNIQUE,
    `id_estudiante` INT NOT NULL,
    `id_concepto` INT NOT NULL,
    `año_academico` INT NOT NULL,
    `monto_pagado` DECIMAL(12,2) NOT NULL,
    `metodo_pago` ENUM('Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque') NOT NULL,
    `fecha_pago` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `estado_pago` ENUM('Pagado', 'Pendiente', 'Anulado') DEFAULT 'Pagado',
    `descuento_aplicado` DECIMAL(12,2) DEFAULT 0.00,
    `observaciones` VARCHAR(255),
    CONSTRAINT `fk_pago_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_pago_concepto` FOREIGN KEY (`id_concepto`) REFERENCES `conceptos_pago` (`id_concepto`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Tabla de Facturas Electrónicas
-- NOTA: nombre_archivo_pdf almacena el nombre del archivo plano .txt generado nativamente (ej. factura_000123.txt)
DROP TABLE IF EXISTS `facturas`;
CREATE TABLE `facturas` (
    `id_factura` INT AUTO_INCREMENT PRIMARY KEY,
    `id_pago` INT NOT NULL UNIQUE,
    `numero_factura` VARCHAR(50) NOT NULL UNIQUE,
    `cufe` VARCHAR(255) NOT NULL,
    `nombre_archivo_pdf` VARCHAR(255) NOT NULL,
    `ruta_archivo` VARCHAR(255) NOT NULL,
    `fecha_emision` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `subtotal` DECIMAL(12,2) NOT NULL,
    `iva` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `total` DECIMAL(12,2) NOT NULL,
    CONSTRAINT `fk_factura_pago` FOREIGN KEY (`id_pago`) REFERENCES `pagos` (`id_pago`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Tabla de Categorías de Recursos
DROP TABLE IF EXISTS `categorias_recurso`;
CREATE TABLE `categorias_recurso` (
    `id_categoria` INT AUTO_INCREMENT PRIMARY KEY,
    `nombre_categoria` VARCHAR(100) NOT NULL UNIQUE,
    `descripcion` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Tabla de Recursos (Biblioteca, Laboratorios, Tecnología)
DROP TABLE IF EXISTS `recursos`;
CREATE TABLE `recursos` (
    `id_recurso` INT AUTO_INCREMENT PRIMARY KEY,
    `codigo_unico` VARCHAR(50) NOT NULL UNIQUE,
    `nombre_recurso` VARCHAR(150) NOT NULL,
    `id_categoria` INT NOT NULL,
    `ubicacion_fisica` VARCHAR(100) NOT NULL,
    `estado_recurso` ENUM('Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja') NOT NULL DEFAULT 'Disponible',
    `fecha_adquisicion` DATE,
    CONSTRAINT `fk_rec_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categorias_recurso` (`id_categoria`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. Tabla de Préstamos a Estudiantes
DROP TABLE IF EXISTS `prestamos_estudiante`;
CREATE TABLE `prestamos_estudiante` (
    `id_prestamo_estudiante` INT AUTO_INCREMENT PRIMARY KEY,
    `id_recurso` INT NOT NULL,
    `id_estudiante` INT NOT NULL,
    `fecha_prestamo` DATE NOT NULL,
    `fecha_devolucion_programada` DATE NOT NULL,
    `fecha_devolucion_real` DATE,
    `estado_prestamo` ENUM('Activo', 'Devuelto', 'Vencido') NOT NULL DEFAULT 'Activo',
    `observaciones` VARCHAR(255),
    CONSTRAINT `fk_pe_recurso` FOREIGN KEY (`id_recurso`) REFERENCES `recursos` (`id_recurso`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_pe_est` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. Tabla de Préstamos a Profesores
DROP TABLE IF EXISTS `prestamos_profesor`;
CREATE TABLE `prestamos_profesor` (
    `id_prestamo_profesor` INT AUTO_INCREMENT PRIMARY KEY,
    `id_recurso` INT NOT NULL,
    `id_profesor` INT NOT NULL,
    `fecha_prestamo` DATE NOT NULL,
    `fecha_devolucion_programada` DATE NOT NULL,
    `fecha_devolucion_real` DATE,
    `estado_prestamo` ENUM('Activo', 'Devuelto', 'Vencido') NOT NULL DEFAULT 'Activo',
    `observaciones` VARCHAR(255),
    CONSTRAINT `fk_pp_recurso` FOREIGN KEY (`id_recurso`) REFERENCES `recursos` (`id_recurso`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_pp_prof` FOREIGN KEY (`id_profesor`) REFERENCES `profesores` (`id_profesor`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- SECCIÓN 2: DML (POBLACIÓN DE DATOS SEMILLA REALISTAS PARA COLOMBIA)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 2.1 INSERCIÓN DE GRADOS ESCOLARES (12 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `grados` (`id_grado`, `nombre_grado`, `nivel`) VALUES
(1, 'Transición', 'Preescolar'),
(2, 'Primero', 'Primaria'),
(3, 'Segundo', 'Primaria'),
(4, 'Tercero', 'Primaria'),
(5, 'Cuarto', 'Primaria'),
(6, 'Quinto', 'Primaria'),
(7, 'Sexto', 'Secundaria'),
(8, 'Séptimo', 'Secundaria'),
(9, 'Octavo', 'Secundaria'),
(10, 'Noveno', 'Secundaria'),
(11, 'Décimo', 'Media'),
(12, 'Undécimo', 'Media');

-- -----------------------------------------------------------------------------
-- 2.2 INSERCIÓN DE ESTUDIANTES (32 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `estudiantes` (`id_estudiante`, `tipo_documento`, `numero_documento`, `nombres`, `apellidos`, `fecha_nacimiento`, `genero`, `direccion`, `telefono`, `email`, `estado`) VALUES
(1, 'TI', '1090123451', 'Juan David', 'Gómez Torres', '2010-04-15', 'M', 'Calle 15 # 4-32, La Playa, Cúcuta', '3104567890', 'juan.gomez@littlehorizons.edu.co', 'Activo'),
(2, 'TI', '1090123452', 'Sofía Valentina', 'Rodríguez Silva', '2011-08-22', 'F', 'Av. 0 # 12-45, Barrio Blanco, Cúcuta', '3115678901', 'sofia.rodriguez@littlehorizons.edu.co', 'Activo'),
(3, 'TI', '1090123453', 'Mateo Andrés', 'Martínez Castro', '2009-02-10', 'M', 'Calle 8 # 19-20, Caobos, Cúcuta', '3126789012', 'mateo.martinez@littlehorizons.edu.co', 'Activo'),
(4, 'TI', '1090123454', 'Isabella', 'Morales Peña', '2012-11-05', 'F', 'Av. Guaimaral # 5E-30, Cúcuta', '3137890123', 'isabella.morales@littlehorizons.edu.co', 'Activo'),
(5, 'TI', '1090123455', 'Santiago Alejandro', 'Díaz Rojas', '2008-06-18', 'M', 'Calle 11 # 2E-15, Bellavista, Cúcuta', '3148901234', 'santiago.diaz@littlehorizons.edu.co', 'Activo'),
(6, 'TI', '1090123456', 'Valentina María', 'Restrepo Gómez', '2010-09-12', 'F', 'Calle 10 # 3E-44, Quinta Oriental, Cúcuta', '3159012345', 'valentina.restrepo@littlehorizons.edu.co', 'Activo'),
(7, 'TI', '1090123457', 'Camilo Andrés', 'Restrepo Gómez', '2013-03-25', 'M', 'Calle 10 # 3E-44, Quinta Oriental, Cúcuta', '3159012345', 'camilo.restrepo@littlehorizons.edu.co', 'Activo'),
(8, 'TI', '1090123458', 'Mariana', 'López Vargas', '2011-01-30', 'F', 'Av. 4 # 8-12, Centro, Cúcuta', '3160123456', 'mariana.lopez@littlehorizons.edu.co', 'Activo'),
(9, 'TI', '1090123459', 'Samuel Esteban', 'Pérez Herrera', '2012-07-14', 'M', 'Calle 18 # 0-40, La Riviera, Cúcuta', '3171234567', 'samuel.perez@littlehorizons.edu.co', 'Activo'),
(10, 'TI', '1090123460', 'Luciana', 'Hernández Duque', '2009-10-08', 'F', 'Av. 11E # 5-60, Colsag, Cúcuta', '3182345678', 'luciana.hernandez@littlehorizons.edu.co', 'Activo'),
(11, 'TI', '1090123461', 'Nicolás Felipe', 'Castillo Moreno', '2010-12-03', 'M', 'Calle 2N # 4-18, Prados del Norte, Cúcuta', '3193456789', 'nicolas.castillo@littlehorizons.edu.co', 'Activo'),
(12, 'TI', '1090123462', 'Gabriela', 'Castro Mendoza', '2013-05-19', 'F', 'Av. 6 # 14-22, Lleras Restrepo, Cúcuta', '3204567891', 'gabriela.castro@littlehorizons.edu.co', 'Activo'),
(13, 'TI', '1090123463', 'Tomás Eduardo', 'Ramírez Ortiz', '2008-09-27', 'M', 'Calle 13 # 1E-80, Ceiba, Cúcuta', '3215678902', 'tomas.ramirez@littlehorizons.edu.co', 'Activo'),
(14, 'TI', '1090123464', 'Salomé', 'Vargas Rincón', '2011-04-02', 'F', 'Av. Grancolombia # 8-50, Cúcuta', '3226789013', 'salome.vargas@littlehorizons.edu.co', 'Activo'),
(15, 'TI', '1090123465', 'Emiliano', 'Pardo Suárez', '2012-08-16', 'M', 'Calle 7 # 15-30, San Luis, Cúcuta', '3237890124', 'emiliano.pardo@littlehorizons.edu.co', 'Activo'),
(16, 'TI', '1090123466', 'Martín Alfonso', 'Ospina Beltrán', '2010-03-11', 'M', 'Av. Libertadores # 10-05, Cúcuta', '3001234568', 'martin.ospina@littlehorizons.edu.co', 'Activo'),
(17, 'TI', '1090123467', 'Daniel Felipe', 'Rivas Navarro', '2009-07-21', 'M', 'Calle 16 # 3-10, La Playa, Cúcuta', '3012345679', 'daniel.rivas@littlehorizons.edu.co', 'Activo'),
(18, 'TI', '1090123468', 'Sara Sofía', 'Cárdenas Gil', '2013-10-04', 'F', 'Av. 2 # 9-45, Latino, Cúcuta', '3023456780', 'sara.cardenas@littlehorizons.edu.co', 'Activo'),
(19, 'TI', '1090123469', 'Jerónimo', 'Cruz Bustamante', '2008-11-29', 'M', 'Calle 12 # 5E-12, Popular, Cúcuta', '3034567891', 'jeronimo.cruz@littlehorizons.edu.co', 'Activo'),
(20, 'TI', '1090123470', 'Valeria', 'Méndez Parra', '2011-06-17', 'F', 'Av. 5 # 16-35, Guaimaral, Cúcuta', '3045678902', 'valeria.mendez@littlehorizons.edu.co', 'Activo'),
(21, 'TI', '1090123471', 'Emmanuel', 'Vega Salcedo', '2012-02-24', 'M', 'Calle 9 # 8-60, Centro, Cúcuta', '3056789013', 'emmanuel.vega@littlehorizons.edu.co', 'Activo'),
(22, 'TI', '1090123472', 'Antonella', 'Duarte Quintero', '2010-05-09', 'F', 'Av. 3E # 11-18, Quinta Oriental, Cúcuta', '3067890124', 'antonella.duarte@littlehorizons.edu.co', 'Activo'),
(23, 'TI', '1090123473', 'Matías', 'Caicedo Barreto', '2009-12-14', 'M', 'Calle 6 # 20-15, Caobos, Cúcuta', '3078901235', 'matias.caicedo@littlehorizons.edu.co', 'Activo'),
(24, 'TI', '1090123474', 'Juan José', 'Flórez Villamizar', '2013-01-18', 'M', 'Av. 0 # 15-50, Barrio Blanco, Cúcuta', '3089012346', 'juanjose.florez@littlehorizons.edu.co', 'Activo'),
(25, 'TI', '1090123475', 'Laura Camila', 'Serrano Peña', '2008-08-03', 'F', 'Calle 14 # 4E-22, La Riviera, Cúcuta', '3090123457', 'laura.serrano@littlehorizons.edu.co', 'Activo'),
(26, 'TI', '1090123476', 'Samuel David', 'Monroy Cadena', '2011-09-09', 'M', 'Av. 8E # 3-40, Colsag, Cúcuta', '3101122334', 'samuel.monroy@littlehorizons.edu.co', 'Activo'),
(27, 'TI', '1090123477', 'María José', 'Roa León', '2012-04-12', 'F', 'Calle 17 # 6-30, San Mateo, Cúcuta', '3112233445', 'mariajose.roa@littlehorizons.edu.co', 'Activo'),
(28, 'TI', '1090123478', 'David Santiago', 'Carvajal Prieto', '2010-07-28', 'M', 'Av. 1 # 10-15, Bellavista, Cúcuta', '3123344556', 'david.carvajal@littlehorizons.edu.co', 'Activo'),
(29, 'TI', '1090123479', 'Manuela', 'Benítez Salazar', '2009-03-06', 'F', 'Calle 5 # 18-05, Caobos, Cúcuta', '3134455667', 'manuela.benitez@littlehorizons.edu.co', 'Activo'),
(30, 'CC', '1090123480', 'Lucas', 'Guerrero Polo', '2007-11-20', 'M', 'Av. Libertadores # 4-55, Cúcuta', '3145566778', 'lucas.guerrero@littlehorizons.edu.co', 'Graduado'),
(31, 'CE', '950123481', 'Andrés Felipe', 'Quintana Bravo', '2010-10-10', 'M', 'Calle 10 # 2E-70, Quinta Oriental, Cúcuta', '3156677889', 'andres.quintana@littlehorizons.edu.co', 'Suspendido'),
(32, 'Pasaporte', 'P1090123482', 'Juliana', 'Páez Camacho', '2011-12-15', 'F', 'Av. 4E # 13-40, Riviera, Cúcuta', '3167788990', 'juliana.paez@littlehorizons.edu.co', 'Retirado');

-- -----------------------------------------------------------------------------
-- 2.3 INSERCIÓN DE ACUDIENTES (30 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `acudientes` (`id_acudiente`, `tipo_documento`, `numero_documento`, `nombres`, `apellidos`, `parentesco`, `telefono`, `email`, `direccion`, `profesion_ocupacion`) VALUES
(1, 'CC', '1098765401', 'Fernando', 'Gómez Medina', 'Padre', '3151112233', 'fernando.gomez@gmail.com', 'Calle 15 # 4-32, La Playa, Cúcuta', 'Ingeniero Civil'),
(2, 'CC', '1098765402', 'Claudia Marcela', 'Silva Lozano', 'Madre', '3152223344', 'claudia.silva@hotmail.com', 'Av. 0 # 12-45, Barrio Blanco, Cúcuta', 'Médica Pediatra'),
(3, 'CC', '1098765403', 'Jorge Andrés', 'Martínez Pardo', 'Padre', '3153334455', 'jorge.martinez@yahoo.com', 'Calle 8 # 19-20, Caobos, Cúcuta', 'Abogado'),
(4, 'CC', '1098765404', 'Patricia', 'Peña Hurtado', 'Madre', '3154445566', 'patricia.pena@outlook.com', 'Av. Guaimaral # 5E-30, Cúcuta', 'Contadora Pública'),
(5, 'CC', '1098765405', 'Ricardo', 'Díaz Forero', 'Padre', '3155556677', 'ricardo.diaz@gmail.com', 'Calle 11 # 2E-15, Bellavista, Cúcuta', 'Arquitecto'),
(6, 'CC', '1098765406', 'Carlos Alberto', 'Restrepo Ortiz', 'Padre', '3156667788', 'carlos.restrepo@empresa.com.co', 'Calle 10 # 3E-44, Quinta Oriental, Cúcuta', 'Empresario'),
(7, 'CC', '1098765407', 'Andrea', 'Vargas Jaramillo', 'Madre', '3157778899', 'andrea.vargas@gmail.com', 'Av. 4 # 8-12, Centro, Cúcuta', 'Diseñadora de Modas'),
(8, 'CC', '1098765408', 'Wilson', 'Pérez Cárdenas', 'Padre', '3158889900', 'wilson.perez@hotmail.com', 'Calle 18 # 0-40, La Riviera, Cúcuta', 'Docente Universitario'),
(9, 'CC', '1098765409', 'Martha Lucía', 'Duque Osorio', 'Madre', '3159990011', 'martha.duque@outlook.com', 'Av. 11E # 5-60, Colsag, Cúcuta', 'Bióloga'),
(10, 'CC', '1098765410', 'Alberto', 'Castillo Rivera', 'Padre', '3161112233', 'alberto.castillo@gmail.com', 'Calle 2N # 4-18, Prados del Norte, Cúcuta', 'Odontólogo'),
(11, 'CC', '1098765411', 'Esperanza', 'Mendoza Salcedo', 'Madre', '3162223344', 'esperanza.mendoza@yahoo.es', 'Av. 6 # 14-22, Lleras Restrepo, Cúcuta', 'Economista'),
(12, 'CC', '1098765412', 'Eduardo', 'Ramírez Ortiz', 'Padre', '3163334455', 'eduardo.ramirez@gmail.com', 'Calle 13 # 1E-80, Ceiba, Cúcuta', 'Comerciante'),
(13, 'CC', '1098765413', 'Luz Marina', 'Rincón Parra', 'Madre', '3164445566', 'luz.rincon@gmail.com', 'Av. Grancolombia # 8-50, Cúcuta', 'Psicóloga'),
(14, 'CC', '1098765414', 'Guillermo', 'Pardo Beltrán', 'Padre', '3165556677', 'guillermo.pardo@hotmail.com', 'Calle 7 # 15-30, San Luis, Cúcuta', 'Ingeniero Mecánico'),
(15, 'CC', '1098765415', 'Nelly', 'Beltrán Castro', 'Madre', '3166667788', 'nelly.beltran@gmail.com', 'Av. Libertadores # 10-05, Cúcuta', 'Administradora de Empresas'),
(16, 'CC', '1098765416', 'Felipe', 'Rivas Morales', 'Padre', '3167778899', 'felipe.rivas@gmail.com', 'Calle 16 # 3-10, La Playa, Cúcuta', 'Químico Farmacéutico'),
(17, 'CC', '1098765417', 'Gloria Inés', 'Gil Restrepo', 'Madre', '3168889900', 'gloria.gil@outlook.com', 'Av. 2 # 9-45, Latino, Cúcuta', 'Comunicadora Social'),
(18, 'CC', '1098765418', 'Hernando', 'Cruz Vega', 'Padre', '3169990011', 'hernando.cruz@yahoo.com', 'Calle 12 # 5E-12, Popular, Cúcuta', 'Médico Cirujano'),
(19, 'CC', '1098765419', 'Adriana', 'Parra Silva', 'Madre', '3171112233', 'adriana.parra@gmail.com', 'Av. 5 # 16-35, Guaimaral, Cúcuta', 'Enfermera Jefe'),
(20, 'CC', '1098765420', 'Javier', 'Vega Salcedo', 'Padre', '3172223344', 'javier.vega@hotmail.com', 'Calle 9 # 8-60, Centro, Cúcuta', 'Ingeniero de Sistemas'),
(21, 'CC', '1098765421', 'Beatriz', 'Quintero Mora', 'Madre', '3173334455', 'beatriz.quintero@gmail.com', 'Av. 3E # 11-18, Quinta Oriental, Cúcuta', 'Veterinaria'),
(22, 'CC', '1098765422', 'Oscar', 'Caicedo Barreto', 'Padre', '3174445566', 'oscar.caicedo@outlook.com', 'Calle 6 # 20-15, Caobos, Cúcuta', 'Banquero'),
(23, 'CC', '1098765423', 'Yolanda', 'Villamizar León', 'Madre', '3175556677', 'yolanda.villamizar@gmail.com', 'Av. 0 # 15-50, Barrio Blanco, Cúcuta', 'Docente de Primaria'),
(24, 'CC', '1098765424', 'Raúl', 'Serrano Peña', 'Padre', '3176667788', 'raul.serrano@gmail.com', 'Calle 14 # 4E-22, La Riviera, Cúcuta', 'Notario'),
(25, 'CC', '1098765425', 'Carmen', 'Cadena Meza', 'Madre', '3177778899', 'carmen.cadena@yahoo.es', 'Av. 8E # 3-40, Colsag, Cúcuta', 'Nutricionista'),
(26, 'CC', '1098765426', 'Héctor', 'Roa León', 'Padre', '3178889900', 'hector.roa@hotmail.com', 'Calle 17 # 6-30, San Mateo, Cúcuta', 'Ingeniero Electrónico'),
(27, 'CC', '1098765427', 'Lucía', 'Prieto Villamizar', 'Madre', '3179990011', 'lucia.prieto@gmail.com', 'Av. 1 # 10-15, Bellavista, Cúcuta', 'Secretaria Ejecutiva'),
(28, 'CC', '1098765428', 'Gabriel', 'Benítez Mora', 'Padre', '3181112233', 'gabriel.benitez@outlook.com', 'Calle 5 # 18-05, Caobos, Cúcuta', 'Consultor Financiero'),
(29, 'CE', '850123429', 'Enrique', 'Guerrero Bravo', 'Padre', '3182223344', 'enrique.guerrero@gmail.com', 'Av. Libertadores # 4-55, Cúcuta', 'Piloto Comercial'),
(30, 'Pasaporte', 'P1098765430', 'Elena', 'Camacho Vivas', 'Madre', '3183334455', 'elena.camacho@gmail.com', 'Av. 4E # 13-40, Riviera, Cúcuta', 'Traductora Oficial');

-- -----------------------------------------------------------------------------
-- 2.4 INSERCIÓN DE RELACIÓN ESTUDIANTE - ACUDIENTE (33 Registros - Hermanos soportados)
-- -----------------------------------------------------------------------------
INSERT INTO `estudiante_acudiente` (`id_estudiante`, `id_acudiente`, `es_principal`, `relacion`) VALUES
(1, 1, TRUE, 'Padre'),
(2, 2, TRUE, 'Madre'),
(3, 3, TRUE, 'Padre'),
(4, 4, TRUE, 'Madre'),
(5, 5, TRUE, 'Padre'),
(6, 6, TRUE, 'Padre'),
(7, 6, TRUE, 'Padre'),
(8, 7, TRUE, 'Madre'),
(9, 8, TRUE, 'Padre'),
(10, 9, TRUE, 'Madre'),
(11, 10, TRUE, 'Padre'),
(12, 11, TRUE, 'Madre'),
(13, 12, TRUE, 'Padre'),
(14, 13, TRUE, 'Madre'),
(15, 14, TRUE, 'Padre'),
(16, 15, TRUE, 'Madre'),
(17, 16, TRUE, 'Padre'),
(18, 17, TRUE, 'Madre'),
(19, 18, TRUE, 'Padre'),
(20, 19, TRUE, 'Madre'),
(21, 20, TRUE, 'Padre'),
(22, 21, TRUE, 'Madre'),
(23, 22, TRUE, 'Padre'),
(24, 23, TRUE, 'Madre'),
(25, 24, TRUE, 'Padre'),
(26, 25, TRUE, 'Madre'),
(27, 26, TRUE, 'Padre'),
(28, 27, TRUE, 'Madre'),
(29, 28, TRUE, 'Padre'),
(30, 29, TRUE, 'Padre'),
(31, 30, TRUE, 'Madre'),
(32, 30, TRUE, 'Madre'),
(1, 2, FALSE, 'Madre');

-- -----------------------------------------------------------------------------
-- 2.5 INSERCIÓN DE MATRÍCULAS (32 Registros - Año Académico 2026)
-- -----------------------------------------------------------------------------
INSERT INTO `matriculas` (`id_matricula`, `id_estudiante`, `id_grado`, `año_academico`, `fecha_matricula`, `estado_matricula`, `costo_matricula`) VALUES
(1, 1, 9, 2026, '2026-01-15', 'Matriculado', 1200000.00),
(2, 2, 8, 2026, '2026-01-16', 'Matriculado', 1200000.00),
(3, 3, 10, 2026, '2026-01-15', 'Matriculado', 1350000.00),
(4, 4, 7, 2026, '2026-01-18', 'Matriculado', 1150000.00),
(5, 5, 11, 2026, '2026-01-14', 'Matriculado', 1450000.00),
(6, 6, 9, 2026, '2026-01-15', 'Matriculado', 1200000.00),
(7, 7, 6, 2026, '2026-01-15', 'Matriculado', 1100000.00),
(8, 8, 8, 2026, '2026-01-17', 'Matriculado', 1200000.00),
(9, 9, 7, 2026, '2026-01-19', 'Matriculado', 1150000.00),
(10, 10, 10, 2026, '2026-01-15', 'Matriculado', 1350000.00),
(11, 11, 9, 2026, '2026-01-18', 'Matriculado', 1200000.00),
(12, 12, 6, 2026, '2026-01-20', 'Matriculado', 1100000.00),
(13, 13, 11, 2026, '2026-01-14', 'Matriculado', 1450000.00),
(14, 14, 8, 2026, '2026-01-16', 'Matriculado', 1200000.00),
(15, 15, 7, 2026, '2026-01-19', 'Matriculado', 1150000.00),
(16, 16, 9, 2026, '2026-01-15', 'Matriculado', 1200000.00),
(17, 17, 10, 2026, '2026-01-16', 'Matriculado', 1350000.00),
(18, 18, 6, 2026, '2026-01-20', 'Matriculado', 1100000.00),
(19, 19, 11, 2026, '2026-01-14', 'Matriculado', 1450000.00),
(20, 20, 8, 2026, '2026-01-17', 'Matriculado', 1200000.00),
(21, 21, 7, 2026, '2026-01-19', 'Matriculado', 1150000.00),
(22, 22, 9, 2026, '2026-01-15', 'Matriculado', 1200000.00),
(23, 23, 10, 2026, '2026-01-16', 'Matriculado', 1350000.00),
(24, 24, 6, 2026, '2026-01-20', 'Matriculado', 1100000.00),
(25, 25, 11, 2026, '2026-01-14', 'Matriculado', 1450000.00),
(26, 26, 8, 2026, '2026-01-17', 'Matriculado', 1200000.00),
(27, 27, 7, 2026, '2026-01-19', 'Matriculado', 1150000.00),
(28, 28, 9, 2026, '2026-01-15', 'Matriculado', 1200000.00),
(29, 29, 10, 2026, '2026-01-16', 'Matriculado', 1350000.00),
(30, 30, 12, 2026, '2025-01-10', 'Promovido', 1500000.00),
(31, 31, 9, 2026, '2026-01-18', 'Matriculado', 1200000.00),
(32, 32, 8, 2026, '2026-01-17', 'Retirado', 1200000.00);

-- -----------------------------------------------------------------------------
-- 2.6 INSERCIÓN DE MATERIAS (31 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `materias` (`id_materia`, `codigo_materia`, `nombre_materia`, `descripcion`, `intensidad_horaria`, `area`) VALUES
(1, 'MAT-101', 'Matemáticas Básicas', 'Fundamentos aritméticos y algebraicos básicos', 5, 'Matemáticas'),
(2, 'MAT-201', 'Álgebra y Funciones', 'Ecuaciones, polinomios y funciones lineales y cuadráticas', 5, 'Matemáticas'),
(3, 'MAT-301', 'Geometría y Trigonometría', 'Teoremas geométricos, ángulos y funciones trigonométricas', 4, 'Matemáticas'),
(4, 'MAT-401', 'Cálculo Diferencial e Integral', 'Límites, derivadas, integrales y optimización', 5, 'Matemáticas'),
(5, 'MAT-501', 'Estadística y Probabilidad', 'Distribuciones, muestreo, regresión y probabilidad clásica', 3, 'Matemáticas'),
(6, 'LEN-101', 'Lengua Castellana I', 'Comprensión lectora, ortografía y gramática estructural', 4, 'Humanidades y Lenguas'),
(7, 'LEN-201', 'Lengua Castellana II', 'Análisis sintáctico, tipologías textuales y ensayo argumentativo', 4, 'Humanidades y Lenguas'),
(8, 'LEN-301', 'Literatura Universal y Colombiana', 'Corrientes literarias, realismo mágico e interpretación crítica', 4, 'Humanidades y Lenguas'),
(9, 'ING-101', 'Inglés Básico A1', 'Gramática básica, vocabulario esencial y conversación simple', 5, 'Humanidades y Lenguas'),
(10, 'ING-201', 'Inglés Intermedio B1', 'Estructuras intermedias, comprensión auditiva y lectura fluida', 5, 'Humanidades y Lenguas'),
(11, 'ING-301', 'Inglés Avanzado B2', 'Debate académico, redacción de ensayos y certificaciones Cambridge', 5, 'Humanidades y Lenguas'),
(12, 'FRA-101', 'Francés Inicial A1', 'Fonética francesa, vocabulario y expresiones cotidianas', 3, 'Humanidades y Lenguas'),
(13, 'CIE-101', 'Ciencias Naturales y Entorno', 'Sistemas biológicos, ecosistemas y recursos renovables', 4, 'Ciencias Naturales'),
(14, 'CIE-201', 'Biología Celular y Genética', 'Estructura celular, ADN, herencia mendeliana y biotecnología', 4, 'Ciencias Naturales'),
(15, 'QUI-101', 'Química General e Inorgánica', 'Tabla periódica, enlaces químicos, nomenclatura y estequiometría', 4, 'Ciencias Naturales'),
(16, 'QUI-201', 'Química Orgánica y Bioquímica', 'Hidrocarburos, grupos funcionales y biomoléculas esenciales', 4, 'Ciencias Naturales'),
(17, 'FIS-101', 'Física Mecánica', 'Cinemática, leyes de Newton, trabajo, energía y dinámica', 4, 'Ciencias Naturales'),
(18, 'FIS-201', 'Física Eléctrica y Ondas', 'Electromagnetismo, termodinámica, movimiento armónico y óptica', 4, 'Ciencias Naturales'),
(19, 'SOC-101', 'Historia de Colombia', 'Época precolombina, colonia, independencia y siglo XX en Colombia', 3, 'Ciencias Sociales'),
(20, 'SOC-201', 'Geografía Universal y de Colombia', 'Geografía física, geopolítica, regiones naturales y demografía', 3, 'Ciencias Sociales'),
(21, 'SOC-301', 'Ciencias Políticas y Económicas', 'Modelos económicos, Constitución Política, derechos humanos y Estado', 3, 'Ciencias Sociales'),
(22, 'FIL-101', 'Filosofía y Pensamiento Crítico', 'Historia de la filosofía, epistemología, lógica y ética contemporánea', 3, 'Ética y Filosofía'),
(23, 'ETI-101', 'Ética y Convivencia Ciudadana', 'Valores cívicos, resolución pacífica de conflictos y ciudadanía global', 2, 'Ética y Filosofía'),
(24, 'TEC-101', 'Tecnología e Innovación', 'Evolución técnica, mecanismos simples, energías limpias y prototipado', 3, 'Tecnología e Informática'),
(25, 'INF-101', 'Fundamentos de Algoritmia y Python', 'Lógica secuencial, estructuras de control, funciones y programación', 4, 'Tecnología e Informática'),
(26, 'INF-201', 'Bases de Datos y Desarrollo Web', 'Modelado relacional, consultas SQL, maquetación HTML/CSS y Javascript', 4, 'Tecnología e Informática'),
(27, 'ART-101', 'Artes Plásticas y Visuales', 'Teoría del color, técnicas de dibujo, escultura y pintura contemporánea', 2, 'Educación Artística'),
(28, 'MUS-101', 'Música y Apreciación Sonora', 'Gramática musical, solfeo, coro, ensamble de percusión y flauta', 2, 'Educación Artística'),
(29, 'EDF-101', 'Educación Física y Acondicionamiento', 'Desarrollo motriz, atletismo, voleibol, fútbol y hábitos saludables', 2, 'Educación Física'),
(30, 'EMP-101', 'Emprendimiento y Liderazgo Juvenil', 'Plan de negocios escolar, finanzas personales, pitch y marketing básico', 2, 'Ciencias Sociales'),
(31, 'ROB-101', 'Robótica Educativa y Sistemas Mecatrónicos', 'Microcontroladores Arduino, servomotores, sensores y automatismos', 3, 'Tecnología e Informática');

-- -----------------------------------------------------------------------------
-- 2.7 INSERCIÓN DE PROFESORES (31 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `profesores` (`id_profesor`, `tipo_documento`, `numero_documento`, `nombres`, `apellidos`, `especialidad`, `email`, `telefono`, `fecha_contratacion`, `estado`) VALUES
(1, 'CC', '1091234501', 'Carlos Alberto', 'Moreno Rivas', 'Licenciado en Matemáticas y Física', 'carlos.moreno@littlehorizons.edu.co', '3153010001', '2019-01-10', 'Activo'),
(2, 'CC', '1091234502', 'María Fernanda', 'Gómez Pérez', 'Licenciada en Humanidades y Lengua Castellana', 'maria.gomez@littlehorizons.edu.co', '3153010002', '2018-07-15', 'Activo'),
(3, 'CC', '1091234503', 'Hernando José', 'Soto Marín', 'Magíster en Ciencias Físicas', 'hernando.soto@littlehorizons.edu.co', '3153010003', '2020-01-12', 'Activo'),
(4, 'CC', '1091234504', 'Diana Marcela', 'Cuéllar Cruz', 'Licenciada en Química Orgánica', 'diana.cuellar@littlehorizons.edu.co', '3153010004', '2017-08-01', 'Activo'),
(5, 'CC', '1091234505', 'Andrés Felipe', 'Vargas Lozano', 'Biólogo y Especialista en Pedagogía', 'andres.vargas@littlehorizons.edu.co', '3153010005', '2021-01-20', 'Activo'),
(6, 'CC', '1091234506', 'Sandra Milena', 'Torres Ortiz', 'Licenciada en Ciencias Sociales e Historia', 'sandra.torres@littlehorizons.edu.co', '3153010006', '2016-02-14', 'Activo'),
(7, 'CC', '1091234507', 'Juan Pablo', 'Castañeda Vega', 'Filósofo y Magíster en Ética Aplicada', 'juan.castaneda@littlehorizons.edu.co', '3153010007', '2019-08-10', 'Activo'),
(8, 'CC', '1091234508', 'Clara Inés', 'Mendoza Salcedo', 'Licenciada en Idiomas Modernos (Inglés/Español)', 'clara.mendoza@littlehorizons.edu.co', '3153010008', '2018-01-15', 'Activo'),
(9, 'CC', '1091234509', 'Diego Armando', 'Navarro Peña', 'Licenciado en Lenguas Extranjeras (Francés/Inglés)', 'diego.navarro@littlehorizons.edu.co', '3153010009', '2020-02-01', 'Activo'),
(10, 'CC', '1091234510', 'Patricia Elena', 'Reyes Morales', 'Maestra en Artes Plásticas y Visuales', 'patricia.reyes@littlehorizons.edu.co', '3153010010', '2015-09-01', 'Activo'),
(11, 'CC', '1091234511', 'Jorge Enrique', 'Suárez Rojas', 'Músico Profesional y Director Coral', 'jorge.suarez@littlehorizons.edu.co', '3153010011', '2021-07-15', 'Activo'),
(12, 'CC', '1091234512', 'Luis Eduardo', 'Barbosa Cárdenas', 'Licenciado en Educación Física y Deporte', 'luis.barbosa@littlehorizons.edu.co', '3153010012', '2017-01-16', 'Activo'),
(13, 'CC', '1091234513', 'Mónica Viviana', 'Gil Restrepo', 'Ingeniera de Sistemas y Especialista en TIC', 'monica.gil@littlehorizons.edu.co', '3153010013', '2019-01-22', 'Activo'),
(14, 'CC', '1091234514', 'Álvaro Javier', 'Caicedo Quintero', 'Ingeniero Mecatrónico y Docente Maker', 'alvaro.caicedo@littlehorizons.edu.co', '3153010014', '2022-01-10', 'Activo'),
(15, 'CC', '1091234515', 'Beatriz Eugenia', 'Ospina Ruiz', 'Licenciada en Geografía y Medio Ambiente', 'beatriz.ospina@littlehorizons.edu.co', '3153010015', '2018-03-01', 'Activo'),
(16, 'CC', '1091234516', 'Camilo Andrés', 'Parra Silva', 'Economista y Docente de Emprendimiento', 'camilo.parra@littlehorizons.edu.co', '3153010016', '2020-08-15', 'Activo'),
(17, 'CC', '1091234517', 'Gloria Stella', 'Beltrán Castro', 'Maestra en Arte Dramático y Teatro', 'gloria.beltran@littlehorizons.edu.co', '3153010017', '2016-10-01', 'Activo'),
(18, 'CC', '1091234518', 'Roberto Carlos', 'Duque Salazar', 'Ingeniero Electrónico y Robótica', 'roberto.duque@littlehorizons.edu.co', '3153010018', '2021-02-01', 'Activo'),
(19, 'CC', '1091234519', 'Liliana Rocío', 'Serrano Meza', 'Licenciada en Matemáticas y Estadística', 'liliana.serrano@littlehorizons.edu.co', '3153010019', '2019-07-01', 'Activo'),
(20, 'CC', '1091234520', 'Fernando Alonso', 'Prieto Villamizar', 'Arquitecto y Dibujante Técnico', 'fernando.prieto@littlehorizons.edu.co', '3153010020', '2017-02-20', 'Activo'),
(21, 'CC', '1091234521', 'Esperanza', 'Rincón Pardo', 'Química Farmacéutica y Bioquímica', 'esperanza.rincon@littlehorizons.edu.co', '3153010021', '2018-09-10', 'Activo'),
(22, 'CC', '1091234522', 'Gabriel Antonio', 'Roa Carvajal', 'Bailarín Profesional y Licenciado en Danza', 'gabriel.roa@littlehorizons.edu.co', '3153010022', '2022-02-15', 'Activo'),
(23, 'CC', '1091234523', 'Carolina', 'Paz Hurtado', 'Politóloga y Docente de Democracia', 'carolina.paz@littlehorizons.edu.co', '3153010023', '2020-01-18', 'Activo'),
(24, 'CC', '1091234524', 'Mauricio', 'León Monroy', 'Físico y Astrónomo Aficionado', 'mauricio.leon@littlehorizons.edu.co', '3153010024', '2021-08-01', 'Activo'),
(25, 'CC', '1091234525', 'Natalia Andrea', 'Benítez Mora', 'Licenciada en Literatura y Escritura Creativa', 'natalia.benitez@littlehorizons.edu.co', '3153010025', '2019-03-12', 'Activo'),
(26, 'CC', '1091234526', 'Víctor Hugo', 'Guerrero Polo', 'Entrenador Deportivo y Salvavidas Certificado', 'victor.guerrero@littlehorizons.edu.co', '3153010026', '2017-05-02', 'Activo'),
(27, 'CC', '1091234527', 'Paola Cristina', 'Rojas Bravo', 'Gran Maestra Nacional de Ajedrez y Matemática', 'paola.rojas@littlehorizons.edu.co', '3153010027', '2020-09-01', 'Activo'),
(28, 'CC', '1091234528', 'Germán Darío', 'Páez Camacho', 'Periodista y Redactor de Contenidos', 'german.paez@littlehorizons.edu.co', '3153010028', '2018-11-15', 'Activo'),
(29, 'CC', '1091234529', 'Sonia Mercedes', 'Cárdenas Vivas', 'Diseñadora Gráfica y Fotógrafa', 'sonia.cardenas@littlehorizons.edu.co', '3153010029', '2021-03-01', 'Activo'),
(30, 'CE', '850123530', 'José Ignacio', 'Bustamante Vega', 'Físico Experimental', 'jose.bustamante@littlehorizons.edu.co', '3153010030', '2022-08-10', 'Activo'),
(31, 'Pasaporte', 'P1091234531', 'Adriana Marcela', 'Méndez Forero', 'Filóloga Francófona Internacional', 'adriana.mendez@littlehorizons.edu.co', '3153010031', '2023-01-15', 'Activo');

-- -----------------------------------------------------------------------------
-- 2.8 INSERCIÓN DE ASIGNACIONES DE PROFESORES (35 Registros - Año 2026)
-- -----------------------------------------------------------------------------
INSERT INTO `asignaciones_profesor` (`id_asignacion`, `id_profesor`, `id_materia`, `id_grado`, `seccion`, `año_academico`, `dias_horario`, `hora_inicio`, `hora_fin`) VALUES
(1, 1, 1, 7, 'A', 2026, 'Lunes, Miercoles', '07:00:00', '08:30:00'),
(2, 1, 2, 8, 'A', 2026, 'Martes, Jueves', '07:00:00', '08:30:00'),
(3, 1, 2, 9, 'A', 2026, 'Lunes, Miercoles', '08:30:00', '10:00:00'),
(4, 1, 3, 10, 'A', 2026, 'Martes, Jueves', '08:30:00', '10:00:00'),
(5, 1, 4, 11, 'A', 2026, 'Viernes', '07:00:00', '09:00:00'),
(6, 2, 6, 7, 'A', 2026, 'Lunes, Miercoles', '07:00:00', '08:30:00'),
(7, 2, 7, 8, 'A', 2026, 'Martes, Jueves', '07:00:00', '08:30:00'),
(8, 2, 7, 9, 'A', 2026, 'Lunes, Miercoles', '10:30:00', '12:00:00'),
(9, 2, 8, 10, 'A', 2026, 'Martes, Jueves', '10:30:00', '12:00:00'),
(10, 2, 8, 11, 'A', 2026, 'Viernes', '09:00:00', '11:00:00'),
(11, 3, 17, 10, 'A', 2026, 'Lunes, Miercoles', '08:30:00', '10:00:00'),
(12, 3, 18, 11, 'A', 2026, 'Martes, Jueves', '08:30:00', '10:00:00'),
(13, 4, 15, 10, 'A', 2026, 'Lunes, Miercoles', '10:30:00', '12:00:00'),
(14, 4, 16, 11, 'A', 2026, 'Martes, Jueves', '10:30:00', '12:00:00'),
(15, 5, 13, 7, 'A', 2026, 'Lunes, Miercoles', '08:30:00', '10:00:00'),
(16, 5, 14, 9, 'A', 2026, 'Martes, Jueves', '08:30:00', '10:00:00'),
(17, 6, 19, 8, 'A', 2026, 'Lunes, Miercoles', '07:00:00', '08:30:00'),
(18, 6, 20, 9, 'A', 2026, 'Martes, Jueves', '07:00:00', '08:30:00'),
(19, 6, 21, 11, 'A', 2026, 'Viernes', '07:00:00', '09:00:00'),
(20, 7, 22, 10, 'A', 2026, 'Lunes, Miercoles', '13:00:00', '14:30:00'),
(21, 7, 23, 8, 'A', 2026, 'Martes, Jueves', '13:00:00', '14:30:00'),
(22, 8, 9, 6, 'A', 2026, 'Lunes, Miercoles', '07:00:00', '08:30:00'),
(23, 8, 10, 8, 'A', 2026, 'Martes, Jueves', '07:00:00', '08:30:00'),
(24, 8, 11, 10, 'A', 2026, 'Viernes', '10:30:00', '12:00:00'),
(25, 9, 12, 9, 'A', 2026, 'Viernes', '07:00:00', '09:00:00'),
(26, 10, 27, 7, 'A', 2026, 'Martes', '13:00:00', '14:30:00'),
(27, 11, 28, 8, 'A', 2026, 'Jueves', '13:00:00', '14:30:00'),
(28, 12, 29, 9, 'A', 2026, 'Viernes', '13:00:00', '14:30:00'),
(29, 13, 24, 7, 'A', 2026, 'Lunes', '10:30:00', '12:00:00'),
(30, 13, 25, 9, 'A', 2026, 'Miercoles', '10:30:00', '12:00:00'),
(31, 13, 26, 11, 'A', 2026, 'Viernes', '10:30:00', '12:00:00'),
(32, 14, 31, 8, 'A', 2026, 'Lunes', '13:00:00', '14:30:00'),
(33, 16, 30, 10, 'A', 2026, 'Miercoles', '13:00:00', '14:30:00'),
(34, 19, 5, 11, 'A', 2026, 'Martes, Jueves', '10:30:00', '12:00:00'),
(35, 27, 31, 9, 'A', 2026, 'Viernes', '08:30:00', '10:00:00');

-- -----------------------------------------------------------------------------
-- 2.9 INSERCIÓN DE INSCRIPCIONES A MATERIAS (40 Registros - Año 2026)
-- -----------------------------------------------------------------------------
INSERT INTO `inscripciones_materia` (`id_inscripcion`, `id_estudiante`, `id_asignacion`, `año_academico`, `estado_inscripcion`) VALUES
(1, 1, 3, 2026, 'Cursando'),
(2, 1, 8, 2026, 'Cursando'),
(3, 1, 16, 2026, 'Cursando'),
(4, 1, 18, 2026, 'Cursando'),
(5, 1, 30, 2026, 'Cursando'),
(6, 2, 2, 2026, 'Cursando'),
(7, 2, 7, 2026, 'Cursando'),
(8, 2, 17, 2026, 'Cursando'),
(9, 2, 23, 2026, 'Cursando'),
(10, 3, 4, 2026, 'Cursando'),
(11, 3, 9, 2026, 'Cursando'),
(12, 3, 11, 2026, 'Cursando'),
(13, 3, 13, 2026, 'Cursando'),
(14, 4, 1, 2026, 'Cursando'),
(15, 4, 6, 2026, 'Cursando'),
(16, 4, 15, 2026, 'Cursando'),
(17, 4, 26, 2026, 'Cursando'),
(18, 5, 5, 2026, 'Cursando'),
(19, 5, 10, 2026, 'Cursando'),
(20, 5, 12, 2026, 'Cursando'),
(21, 5, 14, 2026, 'Cursando'),
(22, 5, 31, 2026, 'Cursando'),
(23, 6, 3, 2026, 'Cursando'),
(24, 6, 8, 2026, 'Cursando'),
(25, 6, 16, 2026, 'Cursando'),
(26, 6, 30, 2026, 'Cursando'),
(27, 7, 22, 2026, 'Cursando'),
(28, 8, 2, 2026, 'Cursando'),
(29, 8, 7, 2026, 'Cursando'),
(30, 9, 1, 2026, 'Cursando'),
(31, 9, 6, 2026, 'Cursando'),
(32, 10, 4, 2026, 'Cursando'),
(33, 10, 11, 2026, 'Cursando'),
(34, 11, 3, 2026, 'Cursando'),
(35, 11, 8, 2026, 'Cursando'),
(36, 13, 5, 2026, 'Cursando'),
(37, 13, 12, 2026, 'Cursando'),
(38, 16, 3, 2026, 'Cursando'),
(39, 17, 4, 2026, 'Cursando'),
(40, 19, 5, 2026, 'Cursando');

-- -----------------------------------------------------------------------------
-- 2.10 INSERCIÓN DE REGISTROS ACADÉMICOS (45 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `registros_academicos` (`id_registro`, `id_inscripcion`, `año_academico`, `periodo`, `valor_nota`, `porcentaje`, `observaciones`) VALUES
(1, 1, 2026, 'I', 4.5, 25.00, 'Excelente desempeño en álgebra de polinomios'),
(2, 1, 2026, 'II', 4.8, 25.00, 'Participación destacada y resolución analítica'),
(3, 2, 2026, 'I', 4.0, 25.00, 'Buen ensayo argumentativo sobre literatura'),
(4, 2, 2026, 'II', 4.2, 25.00, 'Progreso continuo en comprensión lectora'),
(5, 3, 2026, 'I', 4.7, 25.00, 'Destacado informe de laboratorio de genética'),
(6, 4, 2026, 'I', 3.8, 25.00, 'Cumplimiento adecuado de talleres de geografía'),
(7, 5, 2026, 'I', 5.0, 25.00, 'Proyecto de software calificado con honores'),
(8, 6, 2026, 'I', 3.9, 25.00, 'Buen manejo de ecuaciones lineales'),
(9, 7, 2026, 'I', 4.1, 25.00, 'Buena redacción en análisis de fábulas'),
(10, 8, 2026, 'I', 4.3, 25.00, 'Apropiación de conceptos históricos colombianos'),
(11, 9, 2026, 'I', 4.6, 25.00, 'Alto nivel de expresión oral en inglés'),
(12, 10, 2026, 'I', 2.8, 25.00, 'Dificultades en identidades trigonométricas'),
(13, 10, 2026, 'II', 3.2, 25.00, 'Recuperación parcial en examen temático'),
(14, 11, 2026, 'I', 3.5, 25.00, 'Entrega básica de ensayos en lengua castellana'),
(15, 12, 2026, 'I', 2.5, 25.00, 'Falta de entrega de guías de laboratorio físico'),
(16, 12, 2026, 'II', 3.0, 25.00, 'Aprobó con nota mínima en sustentación'),
(17, 13, 2026, 'I', 3.4, 25.00, 'Pruebas teóricas regulares de química general'),
(18, 14, 2026, 'I', 4.8, 25.00, 'Sobresaliente en geometría euclidiana'),
(19, 15, 2026, 'I', 4.5, 25.00, 'Ortografía y vocabulario impecables'),
(20, 16, 2026, 'I', 4.6, 25.00, 'Conocimiento sobresaliente del entorno natural'),
(21, 17, 2026, 'I', 5.0, 25.00, 'Obra pictórica seleccionada para exposición'),
(22, 18, 2026, 'I', 4.9, 25.00, 'Destreza superior en límites y cálculo'),
(23, 18, 2026, 'II', 4.7, 25.00, 'Resolución analítica de derivadas complejas'),
(24, 19, 2026, 'I', 4.4, 25.00, 'Ensayo de literatura universal bien sustentado'),
(25, 20, 2026, 'I', 4.6, 25.00, 'Excelente comprensión de cinemática y ondas'),
(26, 21, 2026, 'I', 4.8, 25.00, 'Laboratorio de síntesis orgánica impecable'),
(27, 22, 2026, 'I', 5.0, 25.00, 'Diseño e implementación de base de datos completa'),
(28, 23, 2026, 'I', 4.4, 25.00, 'Buen rendimiento en álgebra intermedia'),
(29, 24, 2026, 'I', 4.0, 25.00, 'Lectura crítica y análisis textual apropiado'),
(30, 25, 2026, 'I', 4.5, 25.00, 'Buen informe de laboratorio en biología'),
(31, 26, 2026, 'I', 4.8, 25.00, 'Excelente proyecto en Python y estructuras de datos'),
(32, 27, 2026, 'I', 4.2, 25.00, 'Participación activa en speaking de inglés básico'),
(33, 28, 2026, 'I', 3.7, 25.00, 'Comprensión intermedia de álgebra lineal'),
(34, 29, 2026, 'I', 4.1, 25.00, 'Comprensión lectora y redacción aceptable'),
(35, 30, 2026, 'I', 4.3, 25.00, 'Resolución de problemas aritméticos'),
(36, 31, 2026, 'I', 4.5, 25.00, 'Redacción creativa y fluidez verbal'),
(37, 32, 2026, 'I', 4.7, 25.00, 'Excelente deducción trigonométrica'),
(38, 33, 2026, 'I', 4.4, 25.00, 'Pruebas físicas sustentadas con rigor'),
(39, 34, 2026, 'I', 3.6, 25.00, 'Talleres entregados con observaciones menores'),
(40, 35, 2026, 'I', 4.0, 25.00, 'Participación en análisis histórico'),
(41, 36, 2026, 'I', 4.6, 25.00, 'Buen análisis de cálculo diferencial'),
(42, 37, 2026, 'I', 4.2, 25.00, 'Experimentos de laboratorio físico'),
(43, 38, 2026, 'I', 4.1, 25.00, 'Buen trabajo en trigonometría'),
(44, 39, 2026, 'I', 3.9, 25.00, 'Revisión periódica de álgebra'),
(45, 40, 2026, 'I', 4.8, 25.00, 'Excelente rendimiento general en matemáticas');

-- -----------------------------------------------------------------------------
-- 2.11 INSERCIÓN DE ASISTENCIAS (40 Registros - Con Patrón de >3 Ausencias en 1 Semana)
-- -----------------------------------------------------------------------------
INSERT INTO `asistencias` (`id_asistencia`, `id_estudiante`, `id_asignacion`, `fecha`, `estado_asistencia`, `observaciones`) VALUES
(1, 1, 3, '2026-03-02', 'Presente', 'Puntual'),
(2, 1, 3, '2026-03-04', 'Presente', 'Puntual'),
(3, 1, 3, '2026-03-06', 'Presente', 'Puntual'),
(4, 2, 2, '2026-03-02', 'Presente', 'Puntual'),
(5, 2, 2, '2026-03-03', 'Tardanza', 'Ingreso 15 minutos tarde por tráfico vehicular'),
(6, 2, 2, '2026-03-05', 'Presente', 'Puntual'),
-- Patrón de inasistencia severa para el estudiante Mateo Martínez (id=3) en la semana del 9 al 13 de marzo de 2026:
(7, 3, 4, '2026-03-09', 'Ausente', 'No asistió a primera hora sin justificación previa'),
(8, 3, 4, '2026-03-10', 'Ausente', 'Inasistencia reiterada, sin comunicación del acudiente'),
(9, 3, 4, '2026-03-11', 'Ausente', 'Tercer día consecutivo sin asistir'),
(10, 3, 4, '2026-03-12', 'Ausente', 'Cuarto día consecutivo de inasistencia - Reportar a coordinación'),
(11, 3, 4, '2026-03-13', 'Excusa Médica', 'Acudiente presentó incapacidad de IPS por virosis'),
(12, 4, 1, '2026-03-02', 'Presente', 'Puntual'),
(13, 4, 1, '2026-03-03', 'Presente', 'Puntual'),
(14, 5, 5, '2026-03-02', 'Presente', 'Puntual'),
(15, 5, 5, '2026-03-04', 'Presente', 'Puntual'),
(16, 6, 3, '2026-03-02', 'Presente', 'Puntual'),
(17, 6, 3, '2026-03-04', 'Presente', 'Puntual'),
(18, 6, 3, '2026-03-06', 'Tardanza', 'Llegó 10 min tarde'),
(19, 7, 22, '2026-03-02', 'Presente', 'Puntual'),
(20, 7, 22, '2026-03-05', 'Presente', 'Puntual'),
(21, 8, 2, '2026-03-02', 'Presente', 'Puntual'),
(22, 8, 2, '2026-03-04', 'Ausente', 'Cita médica familiar informada'),
(23, 9, 1, '2026-03-02', 'Presente', 'Puntual'),
(24, 9, 1, '2026-03-05', 'Presente', 'Puntual'),
(25, 10, 4, '2026-03-02', 'Presente', 'Puntual'),
(26, 10, 4, '2026-03-04', 'Presente', 'Puntual'),
(27, 11, 3, '2026-03-02', 'Presente', 'Puntual'),
(28, 11, 3, '2026-03-05', 'Presente', 'Puntual'),
(29, 13, 5, '2026-03-02', 'Presente', 'Puntual'),
(30, 13, 5, '2026-03-04', 'Presente', 'Puntual'),
(31, 14, 2, '2026-03-02', 'Presente', 'Puntual'),
(32, 15, 1, '2026-03-02', 'Presente', 'Puntual'),
(33, 16, 3, '2026-03-02', 'Presente', 'Puntual'),
(34, 17, 4, '2026-03-02', 'Presente', 'Puntual'),
(35, 18, 22, '2026-03-02', 'Presente', 'Puntual'),
(36, 19, 5, '2026-03-02', 'Presente', 'Puntual'),
(37, 20, 2, '2026-03-02', 'Presente', 'Puntual'),
(38, 21, 1, '2026-03-02', 'Presente', 'Puntual'),
(39, 22, 3, '2026-03-02', 'Presente', 'Puntual'),
(40, 23, 4, '2026-03-02', 'Presente', 'Puntual');

-- -----------------------------------------------------------------------------
-- 2.12 INSERCIÓN DE CLUBES (30 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `clubes` (`id_club`, `nombre_club`, `descripcion`, `id_profesor_supervisor`, `capacidad_maxima`, `horario_reunion`, `lugar_reunion`, `estado`) VALUES
(1, 'Club de Robótica e Inteligencia Artificial', 'Desarrollo de prototipos mecatrónicos y algoritmos de visión por computador', 14, 25, 'Miércoles y Viernes 15:00 - 17:00', 'Laboratorio Maker 1', 'Activo'),
(2, 'Club de Astronomía y Ciencias del Espacio', 'Observación con telescopio, astrofísica divulgativa y misiones espaciales', 24, 30, 'Jueves 18:30 - 20:30', 'Observatorio / Terraza Bloque B', 'Activo'),
(3, 'Club de Debate y Modelo de Naciones Unidas (MUN)', 'Oratoria, resolución diplomática de conflictos y relaciones internacionales', 7, 30, 'Martes 15:30 - 17:30', 'Auditorio Principal', 'Activo'),
(4, 'Club de Matemáticas Olímpicas', 'Entrenamiento para olimpiadas colombianas y regionales de matemáticas', 1, 20, 'Lunes y Miércoles 15:00 - 16:30', 'Aula 204 Bloque A', 'Activo'),
(5, 'Club de Ajedrez y Pensamiento Estratégico', 'Estrategia de aperturas, finales de juego y torneos suizos', 27, 25, 'Viernes 14:30 - 16:30', 'Sala de Juegos Mentales', 'Activo'),
(6, 'Club de Teatro Entre Bambalinas', 'Expresión corporal, improvisación, dramaturgia y puesta en escena', 17, 35, 'Martes y Jueves 15:00 - 17:00', 'Teatro Auditorio Little Horizons', 'Activo'),
(7, 'Coro Polifónico Little Horizons', 'Técnica vocal, repertorio polifónico nacional e internacional', 11, 40, 'Lunes y Miércoles 16:00 - 18:00', 'Salón de Ensayos Musicales', 'Activo'),
(8, 'Club de Pintura y Acuarela Creativa', 'Técnicas mixtas, composición sobre lienzo y acuarela botánica', 10, 20, 'Viernes 15:00 - 17:00', 'Taller de Arte Bloque C', 'Activo'),
(9, 'Orquesta Sinfónica Juvenil', 'Instrumentación de vientos, cuerdas y percusión clásica', 11, 45, 'Sábados 08:00 - 12:00', 'Sala Sinfónica', 'Activo'),
(10, 'Club de Danza Folclórica y Contemporánea', 'Coreografía de bailes típicos de Colombia y danzas del mundo', 22, 30, 'Lunes y Jueves 15:30 - 17:30', 'Gimnasio Cubierto Sala 2', 'Activo'),
(11, 'Club de Fútbol Horizontes FC', 'Fundamentos tácticos, preparación física y torneos intercolegiales', 12, 35, 'Martes y Jueves 16:00 - 18:00', 'Cancha Sintética de Fútbol', 'Activo'),
(12, 'Equipo de Baloncesto Águilas', 'Dribling, lanzamientos de triple y sistemas tácticos defensivos', 12, 25, 'Lunes y Miércoles 16:00 - 18:00', 'Coliseo Polideportivo', 'Activo'),
(13, 'Club de Voleibol Femenino y Masculino', 'Saques, remates en red, recepción y juego posicional', 12, 25, 'Viernes 15:00 - 17:30', 'Cancha Múltiple 1', 'Activo'),
(14, 'Club de Natación y Salvamento Acuático', 'Estilos libre, espalda, pecho, mariposa y rescate acuático', 26, 20, 'Martes y Viernes 15:00 - 17:00', 'Complejo Acuático Little Horizons', 'Activo'),
(15, 'Club de Atletismo y Pista', 'Carreras de velocidad, relevos, salto alto y fondo', 26, 30, 'Lunes y Miércoles 15:00 - 16:30', 'Pista Atlética Exterior', 'Activo'),
(16, 'Semillero de Jóvenes Científicos', 'Metodología de la investigación, experimentos y ferias de ciencia', 5, 25, 'Miércoles 14:30 - 16:30', 'Laboratorio de Biología', 'Activo'),
(17, 'Club de Química Experimental', 'Reacciones estequiométricas, síntesis verde y cristales químicos', 4, 20, 'Jueves 15:00 - 17:00', 'Laboratorio de Química', 'Activo'),
(18, 'Club de Ecología y Jardín Botánico', 'Compostaje orgánico, huerta escolar y reforestación de especies nativas', 15, 30, 'Viernes 14:00 - 16:00', 'Vivero y Huerto Institucional', 'Activo'),
(19, 'Club de Emprendimiento y Finanzas Personales', 'Creación de startups escolares, presupuestos y economía circular', 16, 25, 'Martes 15:00 - 17:00', 'Aula de Negocios 301', 'Activo'),
(20, 'Club de Desarrollo de Videojuegos', 'Programación en Python, diseño de sprites, animación y mecánicas lúdicas', 13, 20, 'Miércoles y Jueves 15:30 - 17:30', 'Sala de Sistemas 2', 'Activo'),
(21, 'Club de Periodismo Escolar La Voz de Horizontes', 'Redacción de crónicas, entrevistas, edición de periódico y podcast escolar', 28, 25, 'Lunes 15:00 - 17:00', 'Sala de Prensa y Medios', 'Activo'),
(22, 'Club de Lectura y Poesía Palabras Vivas', 'Tertulias literarias, café literario y recitales poéticos', 25, 25, 'Jueves 16:00 - 17:30', 'Biblioteca Sala de Lectura', 'Activo'),
(23, 'Club de Cine y Producción Audiovisual', 'Guionismo cinematográfico, encuadres de cámara, sonido y montaje', 29, 20, 'Viernes 15:30 - 18:00', 'Estudio Multimedia', 'Activo'),
(24, 'Club de Fotografía Digital y Paisajismo', 'Fotografía macro, retrato, técnicas de iluminación y edición digital', 29, 20, 'Sábados 09:00 - 11:30', 'Estudio de Fotografía y Campus', 'Activo'),
(25, 'Club de Idioma Francés y Cultura Francófona', 'Conversación cotidiana, gastronomía, música francesa y cine-club', 9, 20, 'Martes 15:00 - 16:30', 'Laboratorio de Idiomas', 'Activo'),
(26, 'Club de Liderazgo y Oratoria Juvenil', 'Técnicas de persuasión, comunicación no verbal e impacto de mensaje', 23, 25, 'Miércoles 15:00 - 16:30', 'Sala de Conferencias B', 'Activo'),
(27, 'Club de Primeros Auxilios y Brigada Escolar', 'Reanimación RCP, vendajes, inmovilización y evacuación de emergencias', 12, 30, 'Jueves 15:00 - 16:30', 'Enfermería y Patios', 'Activo'),
(28, 'Club de Modelado 3D e Impresión Digital', 'Diseño CAD en Tinkercad/Blender, calibración e impresión aditiva', 14, 15, 'Lunes 15:30 - 17:30', 'Laboratorio Maker 2', 'Activo'),
(29, 'Club de Cocina Saludable y Mini Chefs', 'Principios nutricionales, manipulación de alimentos y recetas saludables', 21, 20, 'Sábados 10:00 - 12:30', 'Taller Gastronómico Escolar', 'Activo'),
(30, 'Club de Origami y Geometría Creativa', 'Pliegues tradicionales japoneses, figuras modulares y aplicaciones geométricas', 19, 25, 'Viernes 15:00 - 16:30', 'Taller de Creatividad', 'Activo');

-- -----------------------------------------------------------------------------
-- 2.13 INSERCIÓN DE MEMBRESÍAS EN CLUBES (35 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `membresias_club` (`id_membresia`, `id_club`, `id_estudiante`, `fecha_ingreso`, `estado`) VALUES
(1, 1, 1, '2026-02-01', 'Activa'),
(2, 4, 1, '2026-02-01', 'Activa'),
(3, 5, 2, '2026-02-02', 'Activa'),
(4, 7, 2, '2026-02-02', 'Activa'),
(5, 11, 3, '2026-02-03', 'Activa'),
(6, 6, 4, '2026-02-01', 'Activa'),
(7, 8, 4, '2026-02-05', 'Activa'),
(8, 1, 5, '2026-02-01', 'Activa'),
(9, 20, 5, '2026-02-01', 'Activa'),
(10, 2, 6, '2026-02-02', 'Activa'),
(11, 18, 6, '2026-02-02', 'Activa'),
(12, 11, 7, '2026-02-03', 'Activa'),
(13, 29, 7, '2026-02-03', 'Activa'),
(14, 7, 8, '2026-02-01', 'Activa'),
(15, 3, 9, '2026-02-02', 'Activa'),
(16, 17, 10, '2026-02-01', 'Activa'),
(17, 14, 10, '2026-02-01', 'Activa'),
(18, 1, 11, '2026-02-02', 'Activa'),
(19, 10, 12, '2026-02-04', 'Activa'),
(20, 20, 13, '2026-02-01', 'Activa'),
(21, 28, 13, '2026-02-01', 'Activa'),
(22, 5, 14, '2026-02-02', 'Activa'),
(23, 15, 15, '2026-02-03', 'Activa'),
(24, 11, 16, '2026-02-01', 'Activa'),
(25, 12, 17, '2026-02-01', 'Activa'),
(26, 30, 18, '2026-02-04', 'Activa'),
(27, 3, 19, '2026-02-01', 'Activa'),
(28, 21, 19, '2026-02-01', 'Activa'),
(29, 22, 20, '2026-02-02', 'Activa'),
(30, 13, 21, '2026-02-03', 'Activa'),
(31, 23, 22, '2026-02-01', 'Activa'),
(32, 24, 23, '2026-02-02', 'Activa'),
(33, 26, 24, '2026-02-04', 'Activa'),
(34, 25, 25, '2026-02-01', 'Activa'),
(35, 1, 2, '2026-02-10', 'Retirada');

-- -----------------------------------------------------------------------------
-- 2.14 INSERCIÓN DE EVENTOS INSTITUCIONALES (10 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `eventos` (`id_evento`, `nombre_evento`, `tipo_evento`, `descripcion`, `fecha_evento`, `lugar`, `tiene_competencia`) VALUES
(1, 'Olimpiadas Científicas Little Horizons 2026', 'Científico', 'Competencia interdisciplinaria de robótica, física y química experimental', '2026-04-18', 'Auditorio y Laboratorios Centrales', TRUE),
(2, 'Festival Nacional de Danza y Tradición Colombiana', 'Cultural', 'Muestra artística y competitiva de comparsas y danzas folclóricas', '2026-05-22', 'Coliseo Polideportivo', TRUE),
(3, 'Copa Intercolegial de Baloncesto y Fútbol 2026', 'Deportivo', 'Torneo de integración deportiva con colegios invitados del departamento', '2026-06-05', 'Canchas Deportivas y Coliseo', TRUE),
(4, 'Modelo de Naciones Unidas LH-MUN 2026', 'Académico', 'Debate internacional de delegaciones sobre crisis geopolíticas y paz mundial', '2026-08-14', 'Auditorio y Aulas Magistrales', TRUE),
(5, 'Feria Escolar del Libro y Poesía de Cúcuta', 'Cultural', 'Exposición literaria, firmas de autores locales y recitales juveniles', '2026-04-23', 'Biblioteca Central y Plazoleta', FALSE),
(6, 'Día de la Familia y la Convivencia Escolar', 'Social', 'Jornada recreativa, bazar comunitario y presentaciones de talentos', '2026-09-19', 'Zonas Verdes del Campus', FALSE),
(7, 'Feria de Innovación, Startups y Tecnología', 'Científico', 'Presentación de prototipos funcionales y aplicaciones móviles de estudiantes', '2026-10-09', 'Pabellón Tecnológico', TRUE),
(8, 'Torneo Abierto de Ajedrez Jaque al Horizonte', 'Académico', 'Competencia por sistema suizo a 5 rondas avalada por la liga escolar', '2026-05-09', 'Sala de Juegos Mentales', TRUE),
(9, 'Concierto de Gala de la Orquesta y Coro', 'Cultural', 'Presentación sinfónica semestral para padres de familia e invitados', '2026-06-19', 'Teatro Zulima de Cúcuta', FALSE),
(10, 'Jornada Ambiental y Siembra del Árbol Institucional', 'Social', 'Siembra de árboles nativos y charlas de preservación de cuencas hídricas', '2026-10-24', 'Sendero Ecológico Escolar', FALSE);

-- -----------------------------------------------------------------------------
-- 2.15 INSERCIÓN DE PARTICIPANTES EN EVENTOS (25 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `participantes_evento` (`id_participacion`, `id_evento`, `id_estudiante`, `puesto`, `observaciones`) VALUES
(1, 1, 1, '1er Lugar', 'Medalla de oro en diseño de rover espacial con Arduino'),
(2, 1, 5, '2do Lugar', 'Prototipo de clasificación automatizada de residuos'),
(3, 1, 11, '3er Lugar', 'Brazo robótico con servomotores'),
(4, 1, 13, 'Participante', 'Mención por creatividad en simulación virtual'),
(5, 2, 4, '1er Lugar', 'Danza solista tradicional de cumbia colombiana'),
(6, 2, 12, '2do Lugar', 'Pareja de joropo llanero'),
(7, 2, 22, 'Participante', 'Coreografía de currulao pacífico'),
(8, 3, 3, '1er Lugar', 'Capitán del equipo campeón de fútbol intercolegial'),
(9, 3, 16, 'Participante', 'Goleador de la fase de grupos'),
(10, 3, 7, 'Participante', 'Excelente participación en categoría infantil'),
(11, 4, 9, '1er Lugar', 'Mejor delegado en el comité de Consejo de Seguridad'),
(12, 4, 19, '2do Lugar', 'Delegado sobresaliente en crisis económica'),
(13, 4, 24, 'Participante', 'Ponencia destacada en derechos humanos'),
(14, 7, 1, '1er Lugar', 'App móvil escolar Little Horizons Connect'),
(15, 7, 5, '2do Lugar', 'Sistema domótico con control por voz'),
(16, 7, 13, '3er Lugar', 'Plataforma web de tutorías estudiantiles'),
(17, 8, 2, '1er Lugar', 'Campeona invicta en torneo suizo categoría Sub-16'),
(18, 8, 14, '2do Lugar', 'Subcampeón tras desempate Buchholz'),
(19, 8, 20, 'Participante', 'Excelente desempeño táctico'),
(20, 5, 6, 'Participante', 'Lectura de poemas propios en recital inaugural'),
(21, 5, 8, 'Participante', 'Presentación de libro de cuentos ilustrados'),
(22, 6, 7, 'Participante', 'Presentación en banda rítmica'),
(23, 6, 18, 'Participante', 'Conducción de la ceremonia protocolaria'),
(24, 9, 2, 'Participante', 'Primera flauta en concierto de gala'),
(25, 9, 8, 'Participante', 'Voz soprano en coro polifónico');

-- -----------------------------------------------------------------------------
-- 2.16 INSERCIÓN DE CONCEPTOS DE PAGO (10 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `conceptos_pago` (`id_concepto`, `codigo_concepto`, `nombre_concepto`, `descripcion`, `monto_base`, `es_periodico`) VALUES
(1, 'CONC-MAT-01', 'Matrícula Ordinaria Anual', 'Derecho a matrícula institucional para el año académico correspondiente', 1200000.00, FALSE),
(2, 'CONC-PEN-01', 'Pensión Mensual Escolar', 'Cuota mensual de enseñanza y servicios pedagógicos de febrero a noviembre', 650000.00, TRUE),
(3, 'CONC-TRA-01', 'Servicio de Transporte Escolar', 'Ruta ida y vuelta en microbús escolar certificado para el área metropolitana', 280000.00, TRUE),
(4, 'CONC-ALM-01', 'Almuerzo y Nutrición Escolar', 'Alimentación balanceada supervisada por nutricionista institucional', 320000.00, TRUE),
(5, 'CONC-SEG-01', 'Póliza de Seguro Estudiantil', 'Seguro de accidentes escolares 24/7 con cobertura en clínicas de alta complejidad', 95000.00, FALSE),
(6, 'CONC-ROB-01', 'Taller Extracurricular de Robótica', 'Material de ensamble, componentes electrónicos y acceso al laboratorio maker', 180000.00, TRUE),
(7, 'CONC-DEP-01', 'Escuela Deportiva Formativa', 'Entrenamiento intensivo vespertino con uniforme de competencia oficial', 150000.00, TRUE),
(8, 'CONC-SAL-01', 'Salida Pedagógica e Inmersión', 'Transporte especial, guía temática y entrada a reservas y centros científicos', 120000.00, FALSE),
(9, 'CONC-CER-01', 'Certificado y Constancia Académica', 'Expedición de certificados oficiales de notas, constancias de estudio y paz y salvo', 25000.00, FALSE),
(10, 'CONC-GRA-01', 'Derechos de Grado y Diploma', 'Acto solemne de graduación, diploma caligrafiado y carpeta conmemorativa', 350000.00, FALSE);

-- -----------------------------------------------------------------------------
-- 2.17 INSERCIÓN DE PAGOS (32 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `pagos` (`id_pago`, `numero_recibo`, `id_estudiante`, `id_concepto`, `año_academico`, `monto_pagado`, `metodo_pago`, `fecha_pago`, `estado_pago`, `descuento_aplicado`, `observaciones`) VALUES
(1, 'REC-2026-0001', 1, 1, 2026, 1200000.00, 'Transferencia', '2026-01-15 09:30:00', 'Pagado', 0.00, 'Matrícula ordinaria Juan David Gómez'),
(2, 'REC-2026-0002', 2, 1, 2026, 1200000.00, 'Tarjeta Crédito', '2026-01-16 11:15:00', 'Pagado', 0.00, 'Matrícula ordinaria Sofía Valentina Rodríguez'),
(3, 'REC-2026-0003', 3, 1, 2026, 1350000.00, 'Efectivo', '2026-01-15 14:00:00', 'Pagado', 0.00, 'Matrícula Mateo Andrés Martínez'),
(4, 'REC-2026-0004', 4, 1, 2026, 1150000.00, 'Transferencia', '2026-01-18 10:20:00', 'Pagado', 0.00, 'Matrícula Isabella Morales Peña'),
(5, 'REC-2026-0005', 5, 1, 2026, 1450000.00, 'Tarjeta Débito', '2026-01-14 16:45:00', 'Pagado', 0.00, 'Matrícula Santiago Alejandro Díaz'),
(6, 'REC-2026-0006', 6, 1, 2026, 1080000.00, 'Transferencia', '2026-01-15 08:30:00', 'Pagado', 120000.00, 'Matrícula Valentina Restrepo con 10% descuento por hermano'),
(7, 'REC-2026-0007', 7, 1, 2026, 990000.00, 'Transferencia', '2026-01-15 08:35:00', 'Pagado', 110000.00, 'Matrícula Camilo Restrepo con 10% descuento por hermano'),
(8, 'REC-2026-0008', 8, 1, 2026, 1200000.00, 'Cheque', '2026-01-17 12:00:00', 'Pagado', 0.00, 'Matrícula Mariana López Vargas - Cheque Banco de Bogotá'),
(9, 'REC-2026-0009', 9, 1, 2026, 1150000.00, 'Transferencia', '2026-01-19 09:10:00', 'Pagado', 0.00, 'Matrícula Samuel Esteban Pérez'),
(10, 'REC-2026-0010', 10, 1, 2026, 1350000.00, 'Tarjeta Débito', '2026-01-15 15:30:00', 'Pagado', 0.00, 'Matrícula Luciana Hernández Duque'),
(11, 'REC-2026-0011', 1, 2, 2026, 650000.00, 'Transferencia', '2026-02-05 10:00:00', 'Pagado', 0.00, 'Pensión Febrero 2026 - Juan David Gómez'),
(12, 'REC-2026-0012', 2, 2, 2026, 650000.00, 'Tarjeta Crédito', '2026-02-05 11:20:00', 'Pagado', 0.00, 'Pensión Febrero 2026 - Sofía Rodríguez'),
(13, 'REC-2026-0013', 4, 2, 2026, 650000.00, 'Transferencia', '2026-02-06 09:40:00', 'Pagado', 0.00, 'Pensión Febrero 2026 - Isabella Morales'),
(14, 'REC-2026-0014', 5, 2, 2026, 650000.00, 'Tarjeta Débito', '2026-02-05 16:10:00', 'Pagado', 0.00, 'Pensión Febrero 2026 - Santiago Díaz'),
(15, 'REC-2026-0015', 6, 2, 2026, 585000.00, 'Transferencia', '2026-02-04 08:50:00', 'Pagado', 65000.00, 'Pensión Febrero 2026 - Valentina Restrepo (Descuento hermano)'),
(16, 'REC-2026-0016', 7, 2, 2026, 585000.00, 'Transferencia', '2026-02-04 08:55:00', 'Pagado', 65000.00, 'Pensión Febrero 2026 - Camilo Restrepo (Descuento hermano)'),
(17, 'REC-2026-0017', 8, 2, 2026, 650000.00, 'Efectivo', '2026-02-07 10:30:00', 'Pagado', 0.00, 'Pensión Febrero 2026 - Mariana López'),
(18, 'REC-2026-0018', 9, 2, 2026, 650000.00, 'Transferencia', '2026-02-05 14:15:00', 'Pagado', 0.00, 'Pensión Febrero 2026 - Samuel Pérez'),
(19, 'REC-2026-0019', 10, 2, 2026, 650000.00, 'Tarjeta Débito', '2026-02-06 17:00:00', 'Pagado', 0.00, 'Pensión Febrero 2026 - Luciana Hernández'),
(20, 'REC-2026-0020', 1, 3, 2026, 280000.00, 'Transferencia', '2026-02-05 10:05:00', 'Pagado', 0.00, 'Transporte Escolar Febrero - Juan David Gómez'),
(21, 'REC-2026-0021', 2, 4, 2026, 320000.00, 'Tarjeta Crédito', '2026-02-05 11:25:00', 'Pagado', 0.00, 'Almuerzo Escolar Febrero - Sofía Rodríguez'),
(22, 'REC-2026-0022', 6, 3, 2026, 280000.00, 'Transferencia', '2026-02-04 09:00:00', 'Pagado', 0.00, 'Transporte Escolar Febrero - Valentina Restrepo'),
(23, 'REC-2026-0023', 7, 3, 2026, 280000.00, 'Transferencia', '2026-02-04 09:05:00', 'Pagado', 0.00, 'Transporte Escolar Febrero - Camilo Restrepo'),
(24, 'REC-2026-0024', 1, 6, 2026, 180000.00, 'Efectivo', '2026-02-08 11:00:00', 'Pagado', 0.00, 'Club de Robótica Mensualidad Febrero'),
(25, 'REC-2026-0025', 2, 5, 2026, 95000.00, 'Transferencia', '2026-01-20 10:00:00', 'Pagado', 0.00, 'Póliza Seguro Estudiantil Anual 2026'),
(26, 'REC-2026-0026', 3, 5, 2026, 95000.00, 'Efectivo', '2026-01-21 11:30:00', 'Pagado', 0.00, 'Póliza Seguro Estudiantil Anual 2026'),
(27, 'REC-2026-0027', 4, 5, 2026, 95000.00, 'Transferencia', '2026-01-20 15:45:00', 'Pagado', 0.00, 'Póliza Seguro Estudiantil Anual 2026'),
(28, 'REC-2026-0028', 5, 5, 2026, 95000.00, 'Tarjeta Débito', '2026-01-20 16:30:00', 'Pagado', 0.00, 'Póliza Seguro Estudiantil Anual 2026'),
(29, 'REC-2026-0029', 6, 5, 2026, 95000.00, 'Transferencia', '2026-01-20 09:15:00', 'Pagado', 0.00, 'Póliza Seguro Estudiantil Anual 2026'),
(30, 'REC-2026-0030', 7, 5, 2026, 95000.00, 'Transferencia', '2026-01-20 09:20:00', 'Pagado', 0.00, 'Póliza Seguro Estudiantil Anual 2026'),
(31, 'REC-2026-0031', 30, 10, 2026, 350000.00, 'Transferencia', '2025-11-28 10:00:00', 'Pagado', 0.00, 'Derechos de grado de bachiller Lucas Guerrero Polo'),
(32, 'REC-2026-0032', 11, 9, 2026, 25000.00, 'Efectivo', '2026-02-12 11:30:00', 'Pagado', 0.00, 'Certificado oficial de calificaciones para trámite de visa'),
(33, NULL, 3, 2, 2026, 650000.00, 'Transferencia', '2026-02-05 08:00:00', 'Pendiente', 0.00, 'Pensión Febrero 2026 vencida - Más de 30 días en mora'),
(34, NULL, 17, 2, 2026, 650000.00, 'Efectivo', '2026-02-05 08:00:00', 'Pendiente', 0.00, 'Pensión Febrero 2026 pendiente de recaudo');

-- -----------------------------------------------------------------------------
-- 2.18 INSERCIÓN DE FACTURAS ELECTRÓNICAS (32 Registros - Sincronizadas con Pagos)
-- -----------------------------------------------------------------------------
INSERT INTO `facturas` (`id_factura`, `id_pago`, `numero_factura`, `cufe`, `nombre_archivo_pdf`, `ruta_archivo`, `fecha_emision`, `subtotal`, `iva`, `total`) VALUES
(1, 1, 'FE-2026-000001', '08f5ef1b0903ece9fe119843f45ad18cfab045dcaf3fa32ccaa5abb61eddaea8', 'factura_000001.txt', 'invoices/factura_000001.txt', '2026-01-15 09:30:00', 1200000.00, 0.00, 1200000.00),
(2, 2, 'FE-2026-000002', '4b3b3d94005af7a993700ae2be56c3fc88fd23739cf59027423a2167c0fc7753', 'factura_000002.txt', 'invoices/factura_000002.txt', '2026-01-16 11:15:00', 1200000.00, 0.00, 1200000.00),
(3, 3, 'FE-2026-000003', '89fa295df602cc163d0867781c54d569ab38525539b94a624025551070122b2c', 'factura_000003.txt', 'invoices/factura_000003.txt', '2026-01-15 14:00:00', 1350000.00, 0.00, 1350000.00),
(4, 4, 'FE-2026-000004', 'f40d7e269127c16cf7507f2edfe492731ecb5e5ae2d7638ab362062cf3e887ae', 'factura_000004.txt', 'invoices/factura_000004.txt', '2026-01-18 10:20:00', 1150000.00, 0.00, 1150000.00),
(5, 5, 'FE-2026-000005', '57e1c11113b133d6b52858aef261150aa0d3ac7f4b22e0ac25fa1db88e73c3fb', 'factura_000005.txt', 'invoices/factura_000005.txt', '2026-01-14 16:45:00', 1450000.00, 0.00, 1450000.00),
(6, 6, 'FE-2026-000006', '20efc5a996cccb6eb3f5cdbcbb008f2c250bd1bbd7df347db22698eaa6ca8fb4', 'factura_000006.txt', 'invoices/factura_000006.txt', '2026-01-15 08:30:00', 1080000.00, 0.00, 1080000.00),
(7, 7, 'FE-2026-000007', '22d30a33d89f2c6d69ee81eafde689e210c62cf135a075c427acc10d5bf7eaf7', 'factura_000007.txt', 'invoices/factura_000007.txt', '2026-01-15 08:35:00', 990000.00, 0.00, 990000.00),
(8, 8, 'FE-2026-000008', 'bc36f3fd571fdc4d27e79a72c3ec8c4e68d5d85528e3d3e108becaa36e5e693b', 'factura_000008.txt', 'invoices/factura_000008.txt', '2026-01-17 12:00:00', 1200000.00, 0.00, 1200000.00),
(9, 9, 'FE-2026-000009', 'a572314acbf96febad38f1adda27a37b477565af9cb2e32f682725f5ddfc4437', 'factura_000009.txt', 'invoices/factura_000009.txt', '2026-01-19 09:10:00', 1150000.00, 0.00, 1150000.00),
(10, 10, 'FE-2026-000010', '4f876f6c55ad0f8780ec6a2747c3ae8af2b4fd407cfa9097aa3fd4ce322e42ce', 'factura_000010.txt', 'invoices/factura_000010.txt', '2026-01-15 15:30:00', 1350000.00, 0.00, 1350000.00),
(11, 11, 'FE-2026-000011', 'eb700647efbdaa04e5b4f369785fdb2dcd458a21f2650c7204d7609c4678e7f1', 'factura_000011.txt', 'invoices/factura_000011.txt', '2026-02-05 10:00:00', 650000.00, 0.00, 650000.00),
(12, 12, 'FE-2026-000012', 'c8c6fba46550b94bc6855574c80adbb4238db4c825601a87e36d1c7aa16d39e6', 'factura_000012.txt', 'invoices/factura_000012.txt', '2026-02-05 11:20:00', 650000.00, 0.00, 650000.00),
(13, 13, 'FE-2026-000013', '13de8a39a0c78fb913e7168645f2818d83e1be9c0f83ca6a2c762993eede5246', 'factura_000013.txt', 'invoices/factura_000013.txt', '2026-02-06 09:40:00', 650000.00, 0.00, 650000.00),
(14, 14, 'FE-2026-000014', '4281e3ebd24cf788fe822d62e483ba9c087094d497fea32ac3048691b974af45', 'factura_000014.txt', 'invoices/factura_000014.txt', '2026-02-05 16:10:00', 650000.00, 0.00, 650000.00),
(15, 15, 'FE-2026-000015', '9eda1b095e1917d1b8c586cdfc9b74c326301019f908bec378a69db1879b17c1', 'factura_000015.txt', 'invoices/factura_000015.txt', '2026-02-04 08:50:00', 585000.00, 0.00, 585000.00),
(16, 16, 'FE-2026-000016', '1a8600d68086541f0de543a8a11f4528dd66376ebb890e55d4be70b698f27fbe', 'factura_000016.txt', 'invoices/factura_000016.txt', '2026-02-04 08:55:00', 585000.00, 0.00, 585000.00),
(17, 17, 'FE-2026-000017', 'e98bbf72dc21e3b779a624dee22a24cee15bb1eaa7372e8576048266aff54e84', 'factura_000017.txt', 'invoices/factura_000017.txt', '2026-02-07 10:30:00', 650000.00, 0.00, 650000.00),
(18, 18, 'FE-2026-000018', '2e033da7a89ba2ae158ecd9fe5c149776b3feefce74c0cb6b1315af2f60282ce', 'factura_000018.txt', 'invoices/factura_000018.txt', '2026-02-05 14:15:00', 650000.00, 0.00, 650000.00),
(19, 19, 'FE-2026-000019', '1dd6c12828d60d2880873c43e6bdaed8a955b129f7f32efab99e2d9caba1a363', 'factura_000019.txt', 'invoices/factura_000019.txt', '2026-02-06 17:00:00', 650000.00, 0.00, 650000.00),
(20, 20, 'FE-2026-000020', 'a249c92f789dd76581eaaccc1465b0ab42a890eeeb86992bcbb317b2a28b27de', 'factura_000020.txt', 'invoices/factura_000020.txt', '2026-02-05 10:05:00', 280000.00, 0.00, 280000.00),
(21, 21, 'FE-2026-000021', 'e64dc45fd3e69f84f8717c433a951d2ae69c0a1d3dce36fff62af97d69e5772e', 'factura_000021.txt', 'invoices/factura_000021.txt', '2026-02-05 11:25:00', 320000.00, 0.00, 320000.00),
(22, 22, 'FE-2026-000022', '21900bd1e5230c363aa321f15aa69f4c06264af046ed525a86d91e0ac6805ba4', 'factura_000022.txt', 'invoices/factura_000022.txt', '2026-02-04 09:00:00', 280000.00, 0.00, 280000.00),
(23, 23, 'FE-2026-000023', '1dc0514485c7b7a43889b3c1c1f7ecac05af6e73cc2a207c0b0c46e20ae2d7ec', 'factura_000023.txt', 'invoices/factura_000023.txt', '2026-02-04 09:05:00', 280000.00, 0.00, 280000.00),
(24, 24, 'FE-2026-000024', '97ef727537925617a75bae20c8d6905bdac027fe1b62bb6b581ff7ba434e43fd', 'factura_000024.txt', 'invoices/factura_000024.txt', '2026-02-08 11:00:00', 180000.00, 0.00, 180000.00),
(25, 25, 'FE-2026-000025', '03327119cb914c75c4d520d6330321a81e92b3e08696aee9a49d39041890846a', 'factura_000025.txt', 'invoices/factura_000025.txt', '2026-01-20 10:00:00', 95000.00, 0.00, 95000.00),
(26, 26, 'FE-2026-000026', 'af7671ff7c96d1cfb65b3fcfc50a38c326f6006ac632795b4d89713f81e7250c', 'factura_000026.txt', 'invoices/factura_000026.txt', '2026-01-21 11:30:00', 95000.00, 0.00, 95000.00),
(27, 27, 'FE-2026-000027', '125ecad02d2afa8a8ce479e20252ce5aced028351d5814e2e1e370e3f92dfd81', 'factura_000027.txt', 'invoices/factura_000027.txt', '2026-01-20 15:45:00', 95000.00, 0.00, 95000.00),
(28, 28, 'FE-2026-000028', 'e6b5fa0667322b8887fbbe02b81555b286c9c47768929aa7845d23200a999c21', 'factura_000028.txt', 'invoices/factura_000028.txt', '2026-01-20 16:30:00', 95000.00, 0.00, 95000.00),
(29, 29, 'FE-2026-000029', '4e4434f7ca6195f9a0f71c0ca16c9ed2a3ef047a403d96f2473b1689c16d5e3b', 'factura_000029.txt', 'invoices/factura_000029.txt', '2026-01-20 09:15:00', 95000.00, 0.00, 95000.00),
(30, 30, 'FE-2026-000030', '9b113d8fd26a9c7429378d98958caf73c65916f7e6820d66ea01449d91d0c02a', 'factura_000030.txt', 'invoices/factura_000030.txt', '2026-01-20 09:20:00', 95000.00, 0.00, 95000.00),
(31, 31, 'FE-2026-000031', '78fbaaeef65eede30400e1815714e1477ea0809949a459fd8d4982ead18b2fc3', 'factura_000031.txt', 'invoices/factura_000031.txt', '2025-11-28 10:00:00', 350000.00, 0.00, 350000.00),
(32, 32, 'FE-2026-000032', '8295cebf35989b0340431c8c7c388c0b04f87fcfead7e04769f052867ccc58e9', 'factura_000032.txt', 'invoices/factura_000032.txt', '2026-02-12 11:30:00', 25000.00, 0.00, 25000.00),
(33, 33, 'FE-2026-000033', 'fc4e407b078a1a35d60f3cba3c600ed0d3f089e0660ef4db300eef30789a3041', 'factura_000033.txt', 'invoices/factura_000033.txt', '2026-02-05 08:00:00', 650000.00, 0.00, 650000.00),
(34, 34, 'FE-2026-000034', 'fd3791a80212dd7d775a097c0d0fae252b237e3168b8f01de5aaec7cb1953c99', 'factura_000034.txt', 'invoices/factura_000034.txt', '2026-02-05 08:00:00', 650000.00, 0.00, 650000.00);

-- -----------------------------------------------------------------------------
-- 2.19 INSERCIÓN DE CATEGORÍAS DE RECURSOS (8 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `categorias_recurso` (`id_categoria`, `nombre_categoria`, `descripcion`) VALUES
(1, 'Libros y Literatura', 'Obras clásicas, enciclopedias, diccionarios y colecciones bibliográficas'),
(2, 'Equipos de Cómputo y Tecnología', 'Portátiles, tablets, proyectores, pantallas y dispositivos de cálculo'),
(3, 'Laboratorio de Ciencias Naturales', 'Microscopios, balanzas analíticas, kits de química y tubos de ensayo'),
(4, 'Audiovisuales y Multimedia', 'Cámaras fotográficas, micrófonos inalámbricos, grabadoras y parlantes'),
(5, 'Robótica y Maker', 'Kits Lego Spike, tarjetas Arduino, sensores ultrasónicos y servomotores'),
(6, 'Deportes y Recreación', 'Balones oficiales, cronómetros digitales, conos, petos y colchonetas'),
(7, 'Instrumentos Musicales', 'Guitarras acústicas, teclados electrónicos, flautas y percusión'),
(8, 'Material Didáctico y Juegos de Mesa', 'Tableros de ajedrez reglamentarios, maquetas anatómicas y mapas murales');

-- -----------------------------------------------------------------------------
-- 2.20 INSERCIÓN DE RECURSOS (32 Registros - Mínimo 30)
-- -----------------------------------------------------------------------------
INSERT INTO `recursos` (`id_recurso`, `codigo_unico`, `nombre_recurso`, `id_categoria`, `ubicacion_fisica`, `estado_recurso`, `fecha_adquisicion`) VALUES
(1, 'LIB-001', 'Cien Años de Soledad - Gabriel García Márquez (Edición Conmemorativa)', 1, 'Biblioteca - Pasillo A Estante 3', 'Disponible', '2023-01-15'),
(2, 'LIB-002', 'Don Quijote de la Mancha - Miguel de Cervantes Saavedra', 1, 'Biblioteca - Pasillo A Estante 4', 'Disponible', '2023-01-15'),
(3, 'LIB-003', 'Principios Matemáticos de la Filosofía Natural - Isaac Newton', 1, 'Biblioteca - Pasillo B Estante 1', 'Disponible', '2023-02-10'),
(4, 'LIB-004', 'Biología Celular y Molecular - Gerald Karp (7ma Edición)', 1, 'Biblioteca - Pasillo B Estante 5', 'Prestado', '2023-03-12'),
(5, 'LIB-005', 'Química General - Raymond Chang (10ma Edición)', 1, 'Biblioteca - Pasillo B Estante 6', 'Disponible', '2023-03-12'),
(6, 'LIB-006', 'Física Universitaria Vol 1 - Sears & Zemansky (14va Edición)', 1, 'Biblioteca - Pasillo C Estante 2', 'Prestado', '2023-04-05'),
(7, 'LIB-007', 'Cálculo de una Variable - James Stewart (8va Edición)', 1, 'Biblioteca - Pasillo C Estante 3', 'Disponible', '2023-04-05'),
(8, 'LIB-008', 'Breve Historia del Tiempo - Stephen Hawking', 1, 'Biblioteca - Pasillo A Estante 5', 'Disponible', '2023-05-18'),
(9, 'LIB-009', 'Atlas Geográfico Universal Ilustrado y de Colombia', 1, 'Biblioteca - Pasillo D Estante 1', 'Disponible', '2023-01-20'),
(10, 'LIB-010', 'Oxford Advanced Learner Dictionary (Hardcover English-Spanish)', 1, 'Biblioteca - Pasillo D Estante 3', 'Disponible', '2023-01-20'),
(11, 'CMP-001', 'Portátil Dell Latitude 3420 Core i5 16GB RAM 512GB SSD #01', 2, 'Sala de Cómputo Móvil - Carrito 1', 'Prestado', '2024-02-15'),
(12, 'CMP-002', 'Portátil Dell Latitude 3420 Core i5 16GB RAM 512GB SSD #02', 2, 'Sala de Cómputo Móvil - Carrito 1', 'Disponible', '2024-02-15'),
(13, 'CMP-003', 'Portátil Dell Latitude 3420 Core i5 16GB RAM 512GB SSD #03', 2, 'Sala de Cómputo Móvil - Carrito 1', 'En Mantenimiento', '2024-02-15'),
(14, 'CMP-004', 'Tableta Gráfica Wacom Intuos Pro Medium #01', 2, 'Laboratorio Maker - Gabinete 3', 'Disponible', '2024-03-10'),
(15, 'CMP-005', 'Tableta Gráfica Wacom Intuos Pro Medium #02', 2, 'Laboratorio Maker - Gabinete 3', 'Prestado', '2024-03-10'),
(16, 'LAB-001', 'Microscopio Óptico Binocular Olympus CX23 #01', 3, 'Laboratorio de Biología - Armario A', 'Disponible', '2023-06-20'),
(17, 'LAB-002', 'Microscopio Óptico Binocular Olympus CX23 #02', 3, 'Laboratorio de Biología - Armario A', 'Prestado', '2023-06-20'),
(18, 'LAB-003', 'Balanza Analítica Digital de Precisión Ohaus Pioneer 0.001g', 3, 'Laboratorio de Química - Mesa Central', 'Disponible', '2023-07-15'),
(19, 'LAB-004', 'Centrífuga de Mesa para Tubos Clínicos 4000 RPM', 3, 'Laboratorio de Química - Armario B', 'Disponible', '2023-08-01'),
(20, 'LAB-005', 'Sensor Digital Vernier LabQuest para Medición de pH y Temperatura', 3, 'Laboratorio de Ciencias - Vitrina 2', 'Prestado', '2024-01-25'),
(21, 'LAB-006', 'Kit de Dinámica y Sensores de Fuerza y Movimiento Pasco', 3, 'Laboratorio de Física - Estante F1', 'Disponible', '2024-01-25'),
(22, 'AUD-001', 'Videoproyector Láser Epson PowerLite FH52 Full HD 4000 Lúmenes', 4, 'Almacén Audiovisual - Casillero 05', 'Prestado', '2023-09-10'),
(23, 'AUD-002', 'Pantalla Portátil Trípode para Proyección 100 Pulgadas', 4, 'Almacén Audiovisual - Zona Trípodes', 'Disponible', '2023-09-10'),
(24, 'AUD-003', 'Sistema de Micrófonos Inalámbricos Shure BLX288/PG58 Dual', 4, 'Almacén Audiovisual - Caja Fuerte 2', 'Disponible', '2023-10-15'),
(25, 'AUD-004', 'Cámara Fotográfica Réflex Canon EOS Rebel T7 con Lente 18-55mm', 4, 'Estudio Multimedia - Gabinete Foto', 'Prestado', '2024-02-01'),
(26, 'AUD-005', 'Grabadora de Voz Digital Zoom H4n Pro 4 Pistas', 4, 'Estudio Multimedia - Gaveta 4', 'Disponible', '2024-02-01'),
(27, 'ROB-001', 'Kit Educativo Robótica Lego Spike Prime Set Principal #01', 5, 'Laboratorio Maker - Estantería R1', 'Prestado', '2024-01-15'),
(28, 'ROB-002', 'Kit Educativo Robótica Lego Spike Prime Set Principal #02', 5, 'Laboratorio Maker - Estantería R1', 'Disponible', '2024-01-15'),
(29, 'ROB-003', 'Set Didáctico Arduino Uno R4 WiFi con Sensores y Actuadores #01', 5, 'Laboratorio Maker - Maletín Maker', 'Disponible', '2024-02-18'),
(30, 'DEP-001', 'Cronómetro Digital Profesional Seiko S056 Resistente al Agua #01', 6, 'Bodega de Deportes - Casillero D1', 'Disponible', '2023-05-10'),
(31, 'DEP-002', 'Juego de 5 Balones de Baloncesto Mikasa Molten GG7X Oficiales', 6, 'Bodega de Deportes - Canasta B1', 'Disponible', '2023-05-10'),
(32, 'DEP-003', 'Set de Cronometraje y Silbatos Fox 40 Classic para Árbitros', 6, 'Bodega de Deportes - Casillero D2', 'Dado de Baja', '2021-03-15');

-- -----------------------------------------------------------------------------
-- 2.21 INSERCIÓN DE PRÉSTAMOS A ESTUDIANTES (18 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `prestamos_estudiante` (`id_prestamo_estudiante`, `id_recurso`, `id_estudiante`, `fecha_prestamo`, `fecha_devolucion_programada`, `fecha_devolucion_real`, `estado_prestamo`, `observaciones`) VALUES
(1, 1, 1, '2026-02-05', '2026-02-12', '2026-02-11', 'Devuelto', 'Entregado en perfecto estado'),
(2, 4, 1, '2026-02-20', '2026-02-27', NULL, 'Vencido', 'Estudiante no devolvió el libro en la fecha pactada'),
(3, 11, 1, '2026-03-01', '2026-03-08', NULL, 'Activo', 'Préstamo para proyecto de informática'),
(4, 2, 2, '2026-02-10', '2026-02-17', '2026-02-16', 'Devuelto', 'Devuelto a tiempo'),
(5, 15, 2, '2026-03-02', '2026-03-09', NULL, 'Activo', 'Uso en taller de dibujo digital'),
(6, 6, 3, '2026-02-15', '2026-02-22', NULL, 'Vencido', 'Mora de más de dos semanas en biblioteca'),
(7, 3, 4, '2026-02-01', '2026-02-08', '2026-02-07', 'Devuelto', 'Devuelto sin novedad'),
(8, 27, 5, '2026-03-01', '2026-03-08', NULL, 'Activo', 'Kit de robótica para olimpiada'),
(9, 5, 6, '2026-02-12', '2026-02-19', '2026-02-18', 'Devuelto', 'Sin observaciones'),
(10, 7, 7, '2026-02-14', '2026-02-21', '2026-02-21', 'Devuelto', 'Devuelto el último día'),
(11, 8, 8, '2026-02-18', '2026-02-25', '2026-02-24', 'Devuelto', 'Excelente estado'),
(12, 9, 9, '2026-02-11', '2026-02-18', '2026-02-17', 'Devuelto', 'Uso en geografía'),
(13, 10, 10, '2026-02-10', '2026-02-17', '2026-02-16', 'Devuelto', 'Consulta de inglés'),
(14, 12, 11, '2026-02-22', '2026-03-01', '2026-02-28', 'Devuelto', 'Portátil sin rayones'),
(15, 14, 12, '2026-02-15', '2026-02-22', '2026-02-20', 'Devuelto', 'Tableta devuelta con lápiz óptico'),
(16, 28, 13, '2026-02-10', '2026-02-17', '2026-02-16', 'Devuelto', 'Lego Spike completo'),
(17, 29, 14, '2026-02-18', '2026-02-25', '2026-02-24', 'Devuelto', 'Kit Arduino revisado'),
(18, 30, 15, '2026-02-12', '2026-02-19', '2026-02-18', 'Devuelto', 'Cronómetro entregado con pila funcional');

-- -----------------------------------------------------------------------------
-- 2.22 INSERCIÓN DE PRÉSTAMOS A PROFESORES (15 Registros)
-- -----------------------------------------------------------------------------
INSERT INTO `prestamos_profesor` (`id_prestamo_profesor`, `id_recurso`, `id_profesor`, `fecha_prestamo`, `fecha_devolucion_programada`, `fecha_devolucion_real`, `estado_prestamo`, `observaciones`) VALUES
(1, 22, 1, '2026-03-01', '2026-03-08', NULL, 'Activo', 'Proyector para clase magistral de trigonometría'),
(2, 17, 5, '2026-03-01', '2026-03-08', NULL, 'Activo', 'Microscopio para práctica de genética celular'),
(3, 20, 4, '2026-02-15', '2026-02-22', NULL, 'Vencido', 'Sensor de pH pendiente de devolución en laboratorio'),
(4, 25, 29, '2026-03-02', '2026-03-09', NULL, 'Activo', 'Cámara para cobertura fotográfica de eventos'),
(5, 23, 2, '2026-02-10', '2026-02-17', '2026-02-16', 'Devuelto', 'Pantalla de proyección devuelta en perfecto estado'),
(6, 24, 11, '2026-02-14', '2026-02-21', '2026-02-20', 'Devuelto', 'Micrófonos inalámbricos probados para coro'),
(7, 26, 28, '2026-02-18', '2026-02-25', '2026-02-24', 'Devuelto', 'Grabadora Zoom para entrevistas del periódico'),
(8, 16, 5, '2026-02-01', '2026-02-08', '2026-02-07', 'Devuelto', 'Microscopio binocular usado en clase de 9°'),
(9, 18, 4, '2026-02-05', '2026-02-12', '2026-02-11', 'Devuelto', 'Balanza analítica calibrada'),
(10, 19, 21, '2026-02-12', '2026-02-19', '2026-02-18', 'Devuelto', 'Centrífuga devuelta en químicas'),
(11, 21, 3, '2026-02-15', '2026-02-22', '2026-02-21', 'Devuelto', 'Kit de dinámica física sin rayaduras'),
(12, 31, 12, '2026-02-02', '2026-02-09', '2026-02-08', 'Devuelto', 'Balones de baloncesto inflados y completos'),
(13, 14, 10, '2026-02-08', '2026-02-15', '2026-02-14', 'Devuelto', 'Tableta Wacom para diseño'),
(14, 27, 14, '2026-02-10', '2026-02-17', '2026-02-16', 'Devuelto', 'Kit Spike Prime desarmado y completo'),
(15, 29, 18, '2026-02-20', '2026-02-27', '2026-02-26', 'Devuelto', 'Arduino Uno R4 con cable USB');

-- =============================================================================
-- FIN DEL SCRIPT: docs/database.sql
-- ERP Little Horizons - Inicialización Exitosa
-- =============================================================================
