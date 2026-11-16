-- =============================================================================
-- PROYECTO: Little Horizons ERP - Sistema de Gestión Educativa
-- ARCHIVO: docs/database.sql
-- BASE: Script oficial proporcionado en el anexo del proyecto (Versión 2.0)
-- MODIFICACIONES JUSTIFICADAS SOBRE EL SCRIPT BASE:
--   1. Tabla `asignaciones_profesor`: se agregaron `dias_horario`, `hora_inicio`
--      y `hora_fin` para cumplir con el requisito 1.2 (control básico de horarios
--      y detección de conflictos de cruce para un mismo profesor).
--   2. Tabla `pagos`: se agregaron `descuento_aplicado` y `estado_pago` para
--      cumplir con los requisitos 3.1 (descuentos por hermanos/becas) y 3.2
--      (identificación de pagos pendientes con más de un mes de mora).
-- =============================================================================

-- Anexo: Script de Base de Datos

-- A continuación se presenta el script completo DDL de MySQL para crear todas las tablas necesarias. 
-- Este script debe ejecutarse en una base de datos llamada `little_horizons_erp`.
-- Si encuentra detalles que modificar o agregar, puede editar el script siempre y cuando pueda justificar sus cambios.


-- ============================================
-- Little Horizons ERP - Esquema de Base de Datos
-- Sistema de Gestión Educativa
-- Versión: 2.0 (Corregida y Mejorada)
-- Motor: MySQL 8.0 o superior (REQUERIDO)
-- ============================================

SET NAMES utf8mb4;
DROP DATABASE IF EXISTS erp_little_horizons;
CREATE DATABASE erp_little_horizons CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_little_horizons;

-- ============================================
-- 1. MÓDULO DE ESTUDIANTES Y ACUDIENTES
-- ============================================

-- Tabla de grados académicos
CREATE TABLE grados (
    id_grado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_grado VARCHAR(50) NOT NULL UNIQUE COMMENT 'Ej: Preescolar, Primero, Segundo, ... Once',
    nivel_numerico INT NOT NULL UNIQUE COMMENT 'Nivel numérico para ordenamiento (0 para preescolar, 1-11 para grados)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Catálogo de grados académicos del colegio';

-- Tabla de estudiantes
CREATE TABLE estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('TI', 'CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'TI',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    genero ENUM('M', 'F', 'Otro') NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    correo_electronico VARCHAR(100),
    tipo_sangre VARCHAR(5),
    alergias TEXT,
    condiciones_medicas TEXT,
    fecha_ingreso DATE NOT NULL,
    estado ENUM('Activo', 'Graduado', 'Retirado', 'Suspendido') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido, primer_nombre)
) ENGINE=InnoDB COMMENT='Información completa de estudiantes';

-- Tabla de acudientes/tutores legales
CREATE TABLE acudientes (
    id_acudiente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    parentesco VARCHAR(50) NOT NULL COMMENT 'Padre, Madre, Abuelo, Tío, Tutor Legal, etc.',
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    correo_electronico VARCHAR(100),
    direccion VARCHAR(255),
    ocupacion VARCHAR(100),
    es_responsable_financiero BOOLEAN DEFAULT TRUE COMMENT 'Indica si es responsable de pagos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Acudientes y tutores legales';

-- Relación estudiantes-acudientes (muchos a muchos)
CREATE TABLE estudiante_acudiente (
    id_estudiante INT NOT NULL,
    id_acudiente INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE COMMENT 'Acudiente principal para comunicaciones',
    es_contacto_emergencia BOOLEAN DEFAULT TRUE COMMENT 'Disponible para emergencias',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_estudiante, id_acudiente),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_acudiente (id_acudiente)
) ENGINE=InnoDB COMMENT='Relación entre estudiantes y sus acudientes';

-- Tabla de matrículas (historial de grados por año)
CREATE TABLE matriculas (
    id_matricula INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_grado INT NOT NULL,
    año_academico INT NOT NULL COMMENT 'Año académico (ej: 2024)',
    estado_matricula ENUM('Activa', 'Completada', 'Retirada') DEFAULT 'Activa',
    fecha_matricula DATE NOT NULL,
    fecha_retiro DATE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    UNIQUE KEY matricula_unica (id_estudiante, año_academico),
    INDEX idx_año_grado (año_academico, id_grado),
    INDEX idx_estado (estado_matricula)
) ENGINE=InnoDB COMMENT='Historial de matrículas de estudiantes por año y grado';

-- ============================================
-- 2. MÓDULO ACADÉMICO
-- ============================================

-- Tabla de materias/asignaturas
CREATE TABLE materias (
    id_materia INT AUTO_INCREMENT PRIMARY KEY,
    codigo_materia VARCHAR(20) NOT NULL UNIQUE COMMENT 'Código único de la materia (ej: MAT-5, ESP-3)',
    nombre_materia VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_grado INT NOT NULL COMMENT 'Grado al que pertenece la materia',
    horas_semanales INT DEFAULT 2 COMMENT 'Horas semanales asignadas',
    esta_activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    INDEX idx_grado (id_grado),
    INDEX idx_activa (esta_activa),
    INDEX idx_codigo (codigo_materia)
) ENGINE=InnoDB COMMENT='Catálogo de materias por grado';

-- Tabla de profesores
CREATE TABLE profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    especializacion VARCHAR(100) COMMENT 'Área de especialización (Matemáticas, Español, etc.)',
    fecha_contratacion DATE NOT NULL,
    estado ENUM('Activo', 'Inactivo', 'Licencia') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Información de profesores';

-- Asignación de materias a profesores
CREATE TABLE asignaciones_profesor (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_profesor INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    seccion VARCHAR(10) COMMENT 'Sección o grupo (A, B, C)',
    dias_horario VARCHAR(60) NOT NULL DEFAULT 'Lunes' COMMENT 'Días asignados (justificado por req. 1.2)',
    hora_inicio TIME NOT NULL DEFAULT '07:00:00' COMMENT 'Hora de inicio (justificado por req. 1.2)',
    hora_fin TIME NOT NULL DEFAULT '08:30:00' COMMENT 'Hora de fin (justificado por req. 1.2)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY asignacion_unica (id_profesor, id_materia, año_academico, seccion),
    INDEX idx_profesor_año (id_profesor, año_academico),
    INDEX idx_materia_año (id_materia, año_academico)
) ENGINE=InnoDB COMMENT='Asignación de profesores a materias por año académico';

-- Inscripción de estudiantes en materias
CREATE TABLE inscripciones_materia (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV') NOT NULL COMMENT 'Período académico',
    seccion VARCHAR(10) COMMENT 'Sección o grupo asignado',
    fecha_inscripcion DATE NOT NULL,
    estado ENUM('Activa', 'Retirada', 'Completada') DEFAULT 'Activa',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY inscripcion_unica (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico)
) ENGINE=InnoDB COMMENT='Inscripción de estudiantes en materias específicas';

-- Tabla de registros académicos (calificaciones/notas)
CREATE TABLE registros_academicos (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    id_profesor INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Final') NOT NULL,
    valor_nota DECIMAL(2,1) NOT NULL COMMENT 'Nota de 0.0 a 5.0 según estándar colombiano',
    observaciones TEXT,
    fecha_registro DATE NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor),
    CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0),
    UNIQUE KEY registro_unico (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico),
    INDEX idx_valor_nota (valor_nota)
) ENGINE=InnoDB COMMENT='Registro de calificaciones de estudiantes';

-- Tabla de asistencia
CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    fecha_asistencia DATE NOT NULL,
    estado_asistencia ENUM('Presente', 'Ausente', 'Tardanza', 'Excusa Médica') NOT NULL,
    observaciones TEXT,
    registrado_por INT COMMENT 'ID del profesor que registró la asistencia',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (registrado_por) REFERENCES profesores(id_profesor),
    UNIQUE KEY asistencia_unica (id_estudiante, id_materia, fecha_asistencia),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_asistencia),
    INDEX idx_materia_fecha (id_materia, fecha_asistencia),
    INDEX idx_estado (estado_asistencia)
) ENGINE=InnoDB COMMENT='Registro de asistencia diaria de estudiantes';

-- ============================================
-- 3. MÓDULO DE ACTIVIDADES EXTRACURRICULARES
-- ============================================

-- Tabla de clubes
CREATE TABLE clubes (
    id_club INT AUTO_INCREMENT PRIMARY KEY,
    nombre_club VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_profesor_supervisor INT COMMENT 'Profesor encargado del club',
    capacidad_maxima INT DEFAULT 30 COMMENT 'Cupo máximo de estudiantes',
    horario_reunion VARCHAR(100) COMMENT 'Días y horario de reunión (ej: Martes 3-5 PM)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor_supervisor) REFERENCES profesores(id_profesor),
    INDEX idx_activo (esta_activo),
    INDEX idx_nombre (nombre_club)
) ENGINE=InnoDB COMMENT='Catálogo de clubes extracurriculares';

-- Membresías en clubes
CREATE TABLE membresias_club (
    id_membresia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_club INT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    estado_membresia ENUM('Activa', 'Retirada') DEFAULT 'Activa',
    fecha_retiro DATE,
    motivo_retiro TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_club) REFERENCES clubes(id_club) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_club_estado (id_club, estado_membresia)
) ENGINE=InnoDB COMMENT='Membresías de estudiantes en clubes';

-- Tabla de eventos
CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_evento VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_evento ENUM('Académico', 'Cultural', 'Deportivo', 'Social', 'Científico') NOT NULL,
    fecha_evento DATE NOT NULL,
    lugar VARCHAR(150),
    tiene_competencia BOOLEAN DEFAULT FALSE COMMENT 'Indica si el evento tiene competencia con posiciones',
    id_organizador INT COMMENT 'Profesor organizador del evento',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_organizador) REFERENCES profesores(id_profesor),
    INDEX idx_fecha (fecha_evento),
    INDEX idx_tipo (tipo_evento),
    INDEX idx_activo (esta_activo)
) ENGINE=InnoDB COMMENT='Catálogo de eventos del colegio';

-- Participantes en eventos
CREATE TABLE participantes_evento (
    id_participacion INT AUTO_INCREMENT PRIMARY KEY,
    id_evento INT NOT NULL,
    id_estudiante INT NOT NULL,
    rol_participacion ENUM('Participante', 'Competidor', 'Organizador', 'Asistente') DEFAULT 'Participante',
    posicion_obtenida VARCHAR(20) COMMENT 'Posición obtenida si hay competencia (1er Lugar, 2do Lugar, 3er Lugar, Participante)',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    UNIQUE KEY participacion_unica (id_evento, id_estudiante),
    INDEX idx_evento (id_evento),
    INDEX idx_estudiante (id_estudiante)
) ENGINE=InnoDB COMMENT='Registro de participación de estudiantes en eventos';

-- ============================================
-- 4. MÓDULO FINANCIERO
-- ============================================

-- Conceptos de pago (catálogo)
CREATE TABLE conceptos_pago (
    id_concepto INT AUTO_INCREMENT PRIMARY KEY,
    codigo_concepto VARCHAR(20) NOT NULL UNIQUE,
    nombre_concepto VARCHAR(100) NOT NULL,
    descripcion TEXT,
    monto_base DECIMAL(10,2) NOT NULL COMMENT 'Valor base del concepto en pesos colombianos',
    es_recurrente BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un pago recurrente (ej: pensión mensual)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (esta_activo),
    INDEX idx_codigo (codigo_concepto)
) ENGINE=InnoDB COMMENT='Catálogo de conceptos de pago del colegio';

-- Tabla de pagos
CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_acudiente INT COMMENT 'Acudiente que realizó el pago',
    id_concepto INT NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    descuento_aplicado DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Descuento por beca o hermanos (justificado por req. 3.1)',
    estado_pago ENUM('Pagado', 'Pendiente', 'Anulado') NOT NULL DEFAULT 'Pagado' COMMENT 'Estado del pago para control de mora (justificado por req. 3.2)',
    fecha_pago DATE NOT NULL,
    metodo_pago ENUM('Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque') NOT NULL,
    numero_recibo VARCHAR(50) UNIQUE,
    año_academico INT COMMENT 'Año al que aplica el pago',
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Anual') COMMENT 'Período al que aplica el pago',
    notas TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente),
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_pago),
    INDEX idx_concepto (id_concepto),
    INDEX idx_fecha (fecha_pago),
    INDEX idx_recibo (numero_recibo)
) ENGINE=InnoDB COMMENT='Registro de todos los pagos realizados';

-- Tabla de facturas electrónicas
CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL UNIQUE COMMENT 'Número consecutivo de factura',
    id_pago INT NOT NULL COMMENT 'Pago asociado a esta factura',
    fecha_emision DATE NOT NULL,
    cufe VARCHAR(100) COMMENT 'Código Único de Factura Electrónica (simulado)',
    nombre_archivo_pdf VARCHAR(255) COMMENT 'Nombre del archivo PDF generado',
    ruta_archivo VARCHAR(500) COMMENT 'Ruta completa donde se guardó el PDF',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES pagos(id_pago) ON DELETE CASCADE,
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha_emision (fecha_emision),
    INDEX idx_pago (id_pago)
) ENGINE=InnoDB COMMENT='Registro de facturas electrónicas generadas';

-- ============================================
-- 5. MÓDULO DE GESTIÓN DE RECURSOS
-- ============================================

-- Categorías de recursos
CREATE TABLE categorias_recurso (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Categorías de recursos (Libro, Equipo de Laboratorio, etc.)';

-- Tabla de recursos del colegio
CREATE TABLE recursos (
    id_recurso INT AUTO_INCREMENT PRIMARY KEY,
    codigo_recurso VARCHAR(50) NOT NULL UNIQUE COMMENT 'Código de inventario único',
    nombre_recurso VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion TEXT,
    fecha_adquisicion DATE,
    ubicacion_fisica VARCHAR(100) COMMENT 'Ubicación física (Biblioteca, Laboratorio 1, etc.)',
    estado_recurso ENUM('Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja') DEFAULT 'Disponible',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias_recurso(id_categoria),
    INDEX idx_codigo (codigo_recurso),
    INDEX idx_estado (estado_recurso),
    INDEX idx_categoria (id_categoria),
    INDEX idx_nombre (nombre_recurso)
) ENGINE=InnoDB COMMENT='Inventario de recursos prestables del colegio';

-- Tabla de préstamos de recursos a ESTUDIANTES
CREATE TABLE prestamos_estudiante (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_estudiante INT NOT NULL COMMENT 'Estudiante que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a estudiantes';

-- Tabla de préstamos de recursos a PROFESORES
CREATE TABLE prestamos_profesor (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_profesor INT NOT NULL COMMENT 'Profesor que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_profesor (id_profesor),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a profesores';

-- ============================================
-- DATOS INICIALES BÁSICOS
-- ============================================

-- Insertar grados académicos
INSERT INTO grados (nombre_grado, nivel_numerico) VALUES
('Preescolar', 0),
('Primero', 1),
('Segundo', 2),
('Tercero', 3),
('Cuarto', 4),
('Quinto', 5),
('Sexto', 6),
('Séptimo', 7),
('Octavo', 8),
('Noveno', 9),
('Décimo', 10),
('Once', 11);

-- Insertar categorías de recursos
INSERT INTO categorias_recurso (nombre_categoria, descripcion) VALUES
('Libro', 'Libros de la biblioteca escolar'),
('Equipo de Laboratorio', 'Equipos científicos y de laboratorio (microscopios, balanzas, etc.)'),
('Equipo Deportivo', 'Equipos y materiales deportivos (balones, conos, etc.)'),
('Dispositivo Tecnológico', 'Tablets, calculadoras científicas, computadores portátiles'),
('Material Didáctico', 'Materiales educativos diversos (mapas, modelos, etc.)');

-- Insertar conceptos de pago comunes
INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente) VALUES
('MAT', 'Matrícula', 'Pago anual de matrícula', 500000.00, FALSE),
('PENSION', 'Pensión Mensual', 'Pago mensual de pensión', 350000.00, TRUE),
('UNIFORME', 'Uniforme', 'Uniforme escolar completo', 250000.00, FALSE),
('TRANSPORTE', 'Transporte Escolar', 'Servicio de transporte mensual', 150000.00, TRUE),
('ALIMENTACION', 'Servicio de Alimentación', 'Almuerzo escolar mensual', 120000.00, TRUE),
('ACTIVIDAD', 'Actividad Especial', 'Salida pedagógica o actividad especial', 50000.00, FALSE),
('CERTIFICADO', 'Certificado', 'Certificado de estudio o constancia', 15000.00, FALSE);


-- ============================================
-- SECCIÓN 2: DML - DATOS DE PRUEBA COHERENTES (>= 30 POR TABLA PRINCIPAL)
-- ============================================

-- 1. Estudiantes (32 registros)
INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) VALUES
('TI', '1034567801', 'Mateo', 'Andrés', 'Rodríguez', 'Sánchez', '2010-01-03', 'M', 'Calle 11 # 21-11, Bogotá', '3104012345', 'est.mateo.rodríguez1@littlehorizons.edu.co', 'A+', NULL, NULL, '2022-01-20', 'Activo'),
('TI', '1034567802', 'Valentina', 'David', 'Rodríguez', 'Sánchez', '2011-02-05', 'F', 'Calle 12 # 22-12, Bogotá', '3104024690', 'est.valentina.rodríguez2@littlehorizons.edu.co', 'B+', NULL, NULL, '2023-01-20', 'Activo'),
('TI', '1034567803', 'Santiago', 'María', 'Martínez', 'Torres', '2012-03-07', 'M', 'Calle 13 # 23-13, Bogotá', '3104037035', 'est.santiago.martínez3@littlehorizons.edu.co', 'O-', NULL, NULL, '2024-01-20', 'Activo'),
('TI', '1034567804', 'Isabella', 'Sofía', 'Martínez', 'Torres', '2013-04-09', 'F', 'Calle 14 # 24-14, Bogotá', '3104049380', 'est.isabella.martínez4@littlehorizons.edu.co', 'AB+', NULL, NULL, '2025-01-20', 'Activo'),
('TI', '1034567805', 'Sebastián', 'Lucía', 'González', 'Rivera', '2014-05-11', 'M', 'Calle 15 # 25-15, Bogotá', '3104061725', 'est.sebastian.gonzalez5@littlehorizons.edu.co', 'A-', NULL, NULL, '2021-01-20', 'Activo'),
('TI', '1034567806', 'Lucía', NULL, 'González', 'Rivera', '2015-06-13', 'F', 'Calle 16 # 26-16, Bogotá', '3104074070', 'est.lucia.gonzalez6@littlehorizons.edu.co', 'O+', NULL, NULL, '2022-01-20', 'Activo'),
('TI', '1034567807', 'Alejandro', 'Antonio', 'Pérez', 'Vargas', '2016-07-15', 'M', 'Calle 17 # 27-17, Bogotá', '3104086415', 'est.alejandro.pérez7@littlehorizons.edu.co', 'A+', 'Maní', NULL, '2023-01-20', 'Activo'),
('TI', '1034567808', 'Camila', 'Alejandra', 'Sánchez', 'Castro', '2017-08-17', 'F', 'Calle 18 # 28-18, Bogotá', '3104098760', 'est.camila.sanchez8@littlehorizons.edu.co', 'B+', NULL, NULL, '2024-01-20', 'Activo'),
('TI', '1034567809', 'Daniel', NULL, 'Ramírez', 'Morales', '2018-09-19', 'M', 'Calle 19 # 29-19, Bogotá', '3104111105', 'est.daniel.ramírez9@littlehorizons.edu.co', 'O-', NULL, 'Asma leve', '2025-01-20', 'Activo'),
('TI', '1034567810', 'Mariana', 'Carlos', 'Torres', 'Rojas', '2009-10-21', 'F', 'Calle 20 # 30-20, Bogotá', '3104123450', 'est.mariana.torres10@littlehorizons.edu.co', 'AB+', NULL, NULL, '2021-01-20', 'Activo'),
('TI', '1034567811', 'Nicolás', 'Elena', 'Flores', 'Ortiz', '2010-11-23', 'M', 'Calle 21 # 31-21, Bogotá', '3104135795', 'est.nicolas.flores11@littlehorizons.edu.co', 'A-', 'Polen', NULL, '2022-01-20', 'Activo'),
('TI', '1034567812', 'Gabriela', NULL, 'Rivera', 'Silva', '2011-12-25', 'F', 'Calle 22 # 32-22, Bogotá', '3104148140', 'est.gabriela.rivera12@littlehorizons.edu.co', 'O+', NULL, NULL, '2023-01-20', 'Activo'),
('TI', '1034567813', 'Samuel', 'Esteban', 'Díaz', 'Mendoza', '2012-01-27', 'M', 'Calle 23 # 33-23, Bogotá', '3104160485', 'est.samuel.díaz13@littlehorizons.edu.co', 'A+', NULL, NULL, '2024-01-20', 'Activo'),
('TI', '1034567814', 'Sara', 'Isabel', 'Vargas', 'Reyes', '2013-02-02', 'F', 'Calle 24 # 34-24, Bogotá', '3104172830', 'est.sara.vargas14@littlehorizons.edu.co', 'B+', 'Maní', NULL, '2025-01-20', 'Activo'),
('TI', '1034567815', 'Tomás', NULL, 'Castro', 'Jiménez', '2014-03-04', 'M', 'Calle 25 # 35-25, Bogotá', '3104185175', 'est.tomas.castro15@littlehorizons.edu.co', 'O-', NULL, NULL, '2021-01-20', 'Activo'),
('TI', '1034567816', 'Valeria', 'José', 'Morales', 'Moreno', '2015-04-06', 'F', 'Calle 26 # 36-26, Bogotá', '3104197520', 'est.valeria.morales16@littlehorizons.edu.co', 'AB+', NULL, NULL, '2022-01-20', 'Activo'),
('TI', '1034567817', 'Lucas', 'Andrés', 'Rojas', 'Álvarez', '2016-05-08', 'M', 'Calle 27 # 37-27, Bogotá', '3104209865', 'est.lucas.rojas17@littlehorizons.edu.co', 'A-', NULL, NULL, '2023-01-20', 'Activo'),
('TI', '1034567818', 'Daniela', 'David', 'Ortiz', 'Romero', '2017-06-10', 'F', 'Calle 28 # 38-28, Bogotá', '3104222210', 'est.daniela.ortiz18@littlehorizons.edu.co', 'O+', NULL, 'Asma leve', '2024-01-20', 'Activo'),
('TI', '1034567819', 'Emiliano', 'María', 'Silva', 'Ruiz', '2018-07-12', 'M', 'Calle 29 # 39-29, Bogotá', '3104234555', 'est.emiliano.silva19@littlehorizons.edu.co', 'A+', NULL, NULL, '2025-01-20', 'Activo'),
('TI', '1034567820', 'Paula', 'Sofía', 'Mendoza', 'Muñoz', '2009-08-14', 'F', 'Calle 30 # 40-30, Bogotá', '3104246900', 'est.paula.mendoza20@littlehorizons.edu.co', 'B+', NULL, NULL, '2021-01-20', 'Activo'),
('TI', '1034567821', 'Gabriel', 'Lucía', 'Reyes', 'Guerrero', '2010-09-16', 'M', 'Calle 31 # 41-31, Bogotá', '3104259245', 'est.gabriel.reyes21@littlehorizons.edu.co', 'O-', 'Maní', NULL, '2022-01-20', 'Activo'),
('TI', '1034567822', 'Victoria', NULL, 'Jiménez', 'Cárdenas', '2011-10-18', 'F', 'Calle 32 # 42-32, Bogotá', '3104271590', 'est.victoria.jiménez22@littlehorizons.edu.co', 'AB+', 'Polen', NULL, '2023-01-20', 'Activo'),
('TI', '1034567823', 'Martín', 'Antonio', 'Moreno', 'Navarro', '2012-11-20', 'M', 'Calle 33 # 43-33, Bogotá', '3104283935', 'est.martin.moreno23@littlehorizons.edu.co', 'A-', NULL, NULL, '2024-01-20', 'Activo'),
('TI', '1034567824', 'Natalia', 'Alejandra', 'Álvarez', 'Pardo', '2013-12-22', 'F', 'Calle 34 # 44-34, Bogotá', '3104296280', 'est.natalia.alvarez24@littlehorizons.edu.co', 'O+', NULL, NULL, '2025-01-20', 'Activo'),
('TI', '1034567825', 'David', NULL, 'Romero', 'Salazar', '2014-01-24', 'M', 'Calle 35 # 45-35, Bogotá', '3104308625', 'est.david.romero25@littlehorizons.edu.co', 'A+', NULL, NULL, '2021-01-20', 'Activo'),
('TI', '1034567826', 'Laura', 'Carlos', 'Ruiz', 'Rodríguez', '2015-02-26', 'F', 'Calle 36 # 46-36, Bogotá', '3104320970', 'est.laura.ruiz26@littlehorizons.edu.co', 'B+', NULL, NULL, '2022-01-20', 'Activo'),
('TI', '1034567827', 'Juan', 'Elena', 'Muñoz', 'Gómez', '2016-03-01', 'M', 'Calle 37 # 47-37, Bogotá', '3104333315', 'est.juan.muñoz27@littlehorizons.edu.co', 'O-', NULL, 'Asma leve', '2023-01-20', 'Activo'),
('CC', '1034567828', 'Andrea', NULL, 'Guerrero', 'Martínez', '2017-04-03', 'F', 'Calle 38 # 48-38, Bogotá', '3104345660', 'est.andrea.guerrero28@littlehorizons.edu.co', 'AB+', 'Maní', NULL, '2024-01-20', 'Activo'),
('CC', '1034567829', 'Andrés', 'Esteban', 'Cárdenas', 'López', '2018-05-05', 'M', 'Calle 39 # 49-39, Bogotá', '3104358005', 'est.andres.cardenas29@littlehorizons.edu.co', 'A-', NULL, NULL, '2025-01-20', 'Activo'),
('CC', '1034567830', 'Juliana', 'Isabel', 'Navarro', 'González', '2009-06-07', 'F', 'Calle 40 # 50-40, Bogotá', '3104370350', 'est.juliana.navarro30@littlehorizons.edu.co', 'O+', NULL, NULL, '2021-01-20', 'Graduado'),
('CC', '1034567831', 'Felipe', NULL, 'Pardo', 'Hernández', '2010-07-09', 'M', 'Calle 41 # 51-41, Bogotá', '3104382695', 'est.felipe.pardo31@littlehorizons.edu.co', 'A+', NULL, NULL, '2022-01-20', 'Retirado'),
('CC', '1034567832', 'Sofía', 'José', 'Salazar', 'Pérez', '2011-08-11', 'F', 'Calle 42 # 52-42, Bogotá', '3104395040', 'est.sofia.salazar32@littlehorizons.edu.co', 'B+', NULL, NULL, '2023-01-20', 'Suspendido');

-- 2. Acudientes (30 registros)
INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, ocupacion, es_responsable_financiero) VALUES
('CC', '80123401', 'Daniel', 'David', 'Rodríguez', 'Rivera', 'Padre', '3155023456', NULL, 'acudiente1.rodríguez@correo.com', 'Carrera 16 # 31-16, Bogotá', 'Médica Pediatra', TRUE),
('CC', '80123402', 'Mariana', 'María', 'Gómez', 'Díaz', 'Madre', '3155046912', '3006022222', 'acudiente2.gomez@correo.com', 'Carrera 17 # 32-17, Bogotá', 'Contadora Pública', TRUE),
('CC', '80123403', 'Samuel', 'Sofía', 'Martínez', 'Vargas', 'Padre', '3155070368', NULL, 'acudiente3.martínez@correo.com', 'Carrera 18 # 33-18, Bogotá', 'Arquitecto', TRUE),
('CC', '80123404', 'Sara', 'Lucía', 'López', 'Castro', 'Madre', '3155093824', '3006044444', 'acudiente4.lopez@correo.com', 'Carrera 19 # 34-19, Bogotá', 'Abogada', TRUE),
('CC', '80123405', 'Lucas', NULL, 'González', 'Morales', 'Padre', '3155117280', NULL, 'acudiente5.gonzalez@correo.com', 'Carrera 20 # 35-20, Bogotá', 'Administrador', TRUE),
('CC', '80123406', 'Daniela', 'Antonio', 'Hernández', 'Rojas', 'Madre', '3155140736', '3006066666', 'acudiente6.hernandez@correo.com', 'Carrera 21 # 36-21, Bogotá', 'Docente', TRUE),
('CC', '80123407', 'Gabriel', 'Alejandra', 'Pérez', 'Ortiz', 'Padre', '3155164192', NULL, 'acudiente7.pérez@correo.com', 'Carrera 22 # 37-22, Bogotá', 'Comerciante', TRUE),
('CC', '80123408', 'Victoria', NULL, 'Sánchez', 'Silva', 'Madre', '3155187648', '3006088888', 'acudiente8.sanchez@correo.com', 'Carrera 23 # 38-23, Bogotá', 'Enfermera', TRUE),
('CC', '80123409', 'David', 'Carlos', 'Ramírez', 'Mendoza', 'Tutor Legal', '3155211104', NULL, 'acudiente9.ramírez@correo.com', 'Carrera 24 # 39-24, Bogotá', 'Diseñador Gráfico', TRUE),
('CC', '80123410', 'Laura', 'Elena', 'Torres', 'Reyes', 'Madre', '3155234560', '3006111110', 'acudiente10.torres@correo.com', 'Carrera 25 # 40-25, Bogotá', 'Ingeniero Civil', TRUE),
('CC', '80123411', 'Andrés', NULL, 'Flores', 'Jiménez', 'Padre', '3155258016', NULL, 'acudiente11.flores@correo.com', 'Carrera 26 # 41-26, Bogotá', 'Médica Pediatra', TRUE),
('CC', '80123412', 'Juliana', 'Esteban', 'Rivera', 'Moreno', 'Madre', '3155281472', '3006133332', 'acudiente12.rivera@correo.com', 'Carrera 27 # 42-27, Bogotá', 'Contadora Pública', TRUE),
('CC', '80123413', 'Mateo', 'Isabel', 'Díaz', 'Álvarez', 'Padre', '3155304928', NULL, 'acudiente13.díaz@correo.com', 'Carrera 28 # 43-28, Bogotá', 'Arquitecto', TRUE),
('CC', '80123414', 'Valentina', NULL, 'Vargas', 'Romero', 'Madre', '3155328384', '3006155554', 'acudiente14.vargas@correo.com', 'Carrera 29 # 44-29, Bogotá', 'Abogada', TRUE),
('CC', '80123415', 'Sebastián', 'José', 'Castro', 'Ruiz', 'Padre', '3155351840', NULL, 'acudiente15.castro@correo.com', 'Carrera 30 # 45-30, Bogotá', 'Administrador', TRUE),
('CC', '80123416', 'Lucía', 'Andrés', 'Morales', 'Muñoz', 'Madre', '3155375296', '3006177776', 'acudiente16.morales@correo.com', 'Carrera 31 # 46-31, Bogotá', 'Docente', TRUE),
('CC', '80123417', 'Daniel', 'David', 'Rojas', 'Guerrero', 'Padre', '3155398752', NULL, 'acudiente17.rojas@correo.com', 'Carrera 32 # 47-32, Bogotá', 'Comerciante', TRUE),
('CC', '80123418', 'Mariana', 'María', 'Ortiz', 'Cárdenas', 'Tutor Legal', '3155422208', '3006199998', 'acudiente18.ortiz@correo.com', 'Carrera 33 # 48-33, Bogotá', 'Enfermera', TRUE),
('CC', '80123419', 'Samuel', 'Sofía', 'Silva', 'Navarro', 'Padre', '3155445664', NULL, 'acudiente19.silva@correo.com', 'Carrera 34 # 49-34, Bogotá', 'Diseñador Gráfico', TRUE),
('CC', '80123420', 'Sara', 'Lucía', 'Mendoza', 'Pardo', 'Madre', '3155469120', '3006222220', 'acudiente20.mendoza@correo.com', 'Carrera 35 # 50-35, Bogotá', 'Ingeniero Civil', TRUE),
('CC', '80123421', 'Lucas', NULL, 'Reyes', 'Salazar', 'Padre', '3155492576', NULL, 'acudiente21.reyes@correo.com', 'Carrera 36 # 51-36, Bogotá', 'Médica Pediatra', TRUE),
('CC', '80123422', 'Daniela', 'Antonio', 'Jiménez', 'Rodríguez', 'Madre', '3155516032', '3006244442', 'acudiente22.jiménez@correo.com', 'Carrera 37 # 52-37, Bogotá', 'Contadora Pública', TRUE),
('CC', '80123423', 'Gabriel', 'Alejandra', 'Moreno', 'Gómez', 'Padre', '3155539488', NULL, 'acudiente23.moreno@correo.com', 'Carrera 38 # 53-38, Bogotá', 'Arquitecto', TRUE),
('CC', '80123424', 'Victoria', NULL, 'Álvarez', 'Martínez', 'Madre', '3155562944', '3006266664', 'acudiente24.alvarez@correo.com', 'Carrera 39 # 54-39, Bogotá', 'Abogada', TRUE),
('CC', '80123425', 'David', 'Carlos', 'Romero', 'López', 'Padre', '3155586400', NULL, 'acudiente25.romero@correo.com', 'Carrera 40 # 55-40, Bogotá', 'Administrador', TRUE),
('CC', '80123426', 'Laura', 'Elena', 'Ruiz', 'González', 'Madre', '3155609856', '3006288886', 'acudiente26.ruiz@correo.com', 'Carrera 41 # 56-41, Bogotá', 'Docente', TRUE),
('CC', '80123427', 'Andrés', NULL, 'Muñoz', 'Hernández', 'Tutor Legal', '3155633312', NULL, 'acudiente27.muñoz@correo.com', 'Carrera 42 # 57-42, Bogotá', 'Comerciante', TRUE),
('CC', '80123428', 'Juliana', 'Esteban', 'Guerrero', 'Pérez', 'Madre', '3155656768', '3006311108', 'acudiente28.guerrero@correo.com', 'Carrera 43 # 58-43, Bogotá', 'Enfermera', TRUE),
('CC', '80123429', 'Mateo', 'Isabel', 'Cárdenas', 'Sánchez', 'Padre', '3155680224', NULL, 'acudiente29.cardenas@correo.com', 'Carrera 44 # 59-44, Bogotá', 'Diseñador Gráfico', TRUE),
('CC', '80123430', 'Valentina', NULL, 'Navarro', 'Ramírez', 'Madre', '3155703680', '3006333330', 'acudiente30.navarro@correo.com', 'Carrera 45 # 60-45, Bogotá', 'Ingeniero Civil', TRUE);

-- 3. Relación Estudiante - Acudiente (soporta hermanos bajo un mismo acudiente)
INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) VALUES
(1, 1, TRUE, TRUE),
(2, 1, TRUE, TRUE),
(3, 3, TRUE, TRUE),
(4, 3, TRUE, TRUE),
(5, 5, TRUE, TRUE),
(6, 5, TRUE, TRUE),
(7, 7, TRUE, TRUE),
(8, 8, TRUE, TRUE),
(9, 9, TRUE, TRUE),
(10, 10, TRUE, TRUE),
(11, 11, TRUE, TRUE),
(12, 12, TRUE, TRUE),
(13, 13, TRUE, TRUE),
(14, 14, TRUE, TRUE),
(15, 15, TRUE, TRUE),
(16, 16, TRUE, TRUE),
(17, 17, TRUE, TRUE),
(18, 18, TRUE, TRUE),
(19, 19, TRUE, TRUE),
(20, 20, TRUE, TRUE),
(21, 21, TRUE, TRUE),
(22, 22, TRUE, TRUE),
(23, 23, TRUE, TRUE),
(24, 24, TRUE, TRUE),
(25, 25, TRUE, TRUE),
(26, 26, TRUE, TRUE),
(27, 27, TRUE, TRUE),
(28, 28, TRUE, TRUE),
(29, 29, TRUE, TRUE),
(30, 30, TRUE, TRUE),
(31, 21, TRUE, TRUE),
(32, 22, TRUE, TRUE),
(1, 2, FALSE, TRUE),
(3, 4, FALSE, TRUE);

-- 4. Matrículas (42 registros)
INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula, fecha_retiro, observaciones) VALUES
(1, 1, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(2, 2, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(3, 3, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(4, 4, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(5, 5, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(6, 6, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(7, 7, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(8, 8, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(9, 9, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(10, 10, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(11, 11, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(12, 12, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(13, 1, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(14, 2, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(15, 3, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(16, 4, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(17, 5, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(18, 6, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(19, 7, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(20, 8, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(21, 9, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(22, 10, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(23, 11, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(24, 12, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(25, 1, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(26, 2, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(27, 3, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(28, 4, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(29, 5, 2026, 'Activa', '2026-01-15', NULL, 'Matrícula regular 2026'),
(30, 6, 2026, 'Completada', '2026-01-15', NULL, 'Matrícula regular 2026'),
(31, 7, 2026, 'Retirada', '2026-01-15', '2026-06-15', 'Matrícula regular 2026'),
(32, 8, 2026, 'Retirada', '2026-01-15', NULL, 'Matrícula regular 2026'),
(1, 12, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(2, 1, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(3, 2, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(4, 3, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(5, 4, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(6, 5, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(7, 6, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(8, 7, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(9, 8, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado'),
(10, 9, 2025, 'Completada', '2025-01-15', NULL, 'Año académico 2025 aprobado');

-- 5. Materias (30 registros)
INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) VALUES
('MAT-1-01', 'Matemáticas Grado Pre', 'Aritmética, álgebra y geometría', 1, 5, TRUE),
('ESP-2-02', 'Lengua Castellana Grado 1', 'Lectura crítica y producción textual', 2, 4, TRUE),
('ING-3-03', 'Inglés Grado 2', 'Comunicación bilingüe B1/B2', 3, 4, TRUE),
('CNAT-4-04', 'Ciencias Naturales Grado 3', 'Biología, física y química escolar', 4, 4, TRUE),
('CSOC-5-05', 'Ciencias Sociales Grado 4', 'Historia, geografía y ciudadanía', 5, 3, TRUE),
('INFO-6-06', 'Informática y Robótica Grado 5', 'Programación y pensamiento computacional', 6, 3, TRUE),
('ART-7-07', 'Educación Artística Grado 6', 'Artes plásticas, música y expresión', 7, 2, TRUE),
('EDFIS-8-08', 'Educación Física Grado 7', 'Deporte formativo y salud', 8, 2, TRUE),
('ETICA-9-09', 'Ética y Valores Grado 8', 'Convivencia y formación ciudadana', 9, 2, TRUE),
('FILO-10-10', 'Filosofía Grado 9', 'Lógica y argumentación', 10, 2, TRUE),
('MAT-11-11', 'Matemáticas Grado 10', 'Aritmética, álgebra y geometría', 11, 5, TRUE),
('ESP-12-12', 'Lengua Castellana Grado 11', 'Lectura crítica y producción textual', 12, 4, TRUE),
('ING-1-13', 'Inglés Grado Pre', 'Comunicación bilingüe B1/B2', 1, 4, TRUE),
('CNAT-2-14', 'Ciencias Naturales Grado 1', 'Biología, física y química escolar', 2, 4, TRUE),
('CSOC-3-15', 'Ciencias Sociales Grado 2', 'Historia, geografía y ciudadanía', 3, 3, TRUE),
('INFO-4-16', 'Informática y Robótica Grado 3', 'Programación y pensamiento computacional', 4, 3, TRUE),
('ART-5-17', 'Educación Artística Grado 4', 'Artes plásticas, música y expresión', 5, 2, TRUE),
('EDFIS-6-18', 'Educación Física Grado 5', 'Deporte formativo y salud', 6, 2, TRUE),
('ETICA-7-19', 'Ética y Valores Grado 6', 'Convivencia y formación ciudadana', 7, 2, TRUE),
('FILO-8-20', 'Filosofía Grado 7', 'Lógica y argumentación', 8, 2, TRUE),
('MAT-9-21', 'Matemáticas Grado 8', 'Aritmética, álgebra y geometría', 9, 5, TRUE),
('ESP-10-22', 'Lengua Castellana Grado 9', 'Lectura crítica y producción textual', 10, 4, TRUE),
('ING-11-23', 'Inglés Grado 10', 'Comunicación bilingüe B1/B2', 11, 4, TRUE),
('CNAT-12-24', 'Ciencias Naturales Grado 11', 'Biología, física y química escolar', 12, 4, TRUE),
('CSOC-1-25', 'Ciencias Sociales Grado Pre', 'Historia, geografía y ciudadanía', 1, 3, TRUE),
('INFO-2-26', 'Informática y Robótica Grado 1', 'Programación y pensamiento computacional', 2, 3, TRUE),
('ART-3-27', 'Educación Artística Grado 2', 'Artes plásticas, música y expresión', 3, 2, TRUE),
('EDFIS-4-28', 'Educación Física Grado 3', 'Deporte formativo y salud', 4, 2, TRUE),
('ETICA-5-29', 'Ética y Valores Grado 4', 'Convivencia y formación ciudadana', 5, 2, TRUE),
('FILO-6-30', 'Filosofía Grado 5', 'Lógica y argumentación', 6, 2, TRUE);

-- 6. Profesores (30 registros)
INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) VALUES
('CC', '79234501', 'Gabriela', 'María', 'Hernández', 'Vargas', 'prof.gabriela.hernandez1@littlehorizons.edu.co', '3187019283', 'Matemáticas', '2018-01-15', 'Activo'),
('CC', '79234502', 'Tomás', 'Sofía', 'Pérez', 'Castro', 'prof.tomas.pérez2@littlehorizons.edu.co', '3187038566', 'Lengua Castellana', '2019-02-15', 'Activo'),
('CC', '79234503', 'Valeria', 'Lucía', 'Sánchez', 'Morales', 'prof.valeria.sanchez3@littlehorizons.edu.co', '3187057849', 'Inglés', '2020-03-15', 'Activo'),
('CC', '79234504', 'Emiliano', NULL, 'Ramírez', 'Rojas', 'prof.emiliano.ramírez4@littlehorizons.edu.co', '3187077132', 'Biología y Química', '2021-04-15', 'Activo'),
('CC', '79234505', 'Paula', 'Antonio', 'Torres', 'Ortiz', 'prof.paula.torres5@littlehorizons.edu.co', '3187096415', 'Ciencias Sociales', '2022-05-15', 'Activo'),
('CC', '79234506', 'Martín', 'Alejandra', 'Flores', 'Silva', 'prof.martin.flores6@littlehorizons.edu.co', '3187115698', 'Ingeniería de Sistemas', '2023-06-15', 'Activo'),
('CC', '79234507', 'Natalia', NULL, 'Rivera', 'Mendoza', 'prof.natalia.rivera7@littlehorizons.edu.co', '3187134981', 'Artes Visuales', '2024-07-15', 'Activo'),
('CC', '79234508', 'Juan', 'Carlos', 'Díaz', 'Reyes', 'prof.juan.díaz8@littlehorizons.edu.co', '3187154264', 'Educación Física', '2017-08-15', 'Activo'),
('CC', '79234509', 'Andrea', 'Elena', 'Vargas', 'Jiménez', 'prof.andrea.vargas9@littlehorizons.edu.co', '3187173547', 'Física', '2018-09-15', 'Activo'),
('CC', '79234510', 'Felipe', NULL, 'Castro', 'Moreno', 'prof.felipe.castro10@littlehorizons.edu.co', '3187192830', 'Filosofía', '2019-10-15', 'Activo'),
('CC', '79234511', 'Sofía', 'Esteban', 'Morales', 'Álvarez', 'prof.sofia.morales11@littlehorizons.edu.co', '3187212113', 'Matemáticas', '2020-11-15', 'Activo'),
('CC', '79234512', 'Santiago', 'Isabel', 'Rojas', 'Romero', 'prof.santiago.rojas12@littlehorizons.edu.co', '3187231396', 'Lengua Castellana', '2021-12-15', 'Activo'),
('CC', '79234513', 'Isabella', NULL, 'Ortiz', 'Ruiz', 'prof.isabella.ortiz13@littlehorizons.edu.co', '3187250679', 'Inglés', '2022-01-15', 'Activo'),
('CC', '79234514', 'Alejandro', 'José', 'Silva', 'Muñoz', 'prof.alejandro.silva14@littlehorizons.edu.co', '3187269962', 'Biología y Química', '2023-02-15', 'Activo'),
('CC', '79234515', 'Camila', 'Andrés', 'Mendoza', 'Guerrero', 'prof.camila.mendoza15@littlehorizons.edu.co', '3187289245', 'Ciencias Sociales', '2024-03-15', 'Activo'),
('CC', '79234516', 'Nicolás', 'David', 'Reyes', 'Cárdenas', 'prof.nicolas.reyes16@littlehorizons.edu.co', '3187308528', 'Ingeniería de Sistemas', '2017-04-15', 'Activo'),
('CC', '79234517', 'Gabriela', 'María', 'Jiménez', 'Navarro', 'prof.gabriela.jiménez17@littlehorizons.edu.co', '3187327811', 'Artes Visuales', '2018-05-15', 'Activo'),
('CC', '79234518', 'Tomás', 'Sofía', 'Moreno', 'Pardo', 'prof.tomas.moreno18@littlehorizons.edu.co', '3187347094', 'Educación Física', '2019-06-15', 'Activo'),
('CC', '79234519', 'Valeria', 'Lucía', 'Álvarez', 'Salazar', 'prof.valeria.alvarez19@littlehorizons.edu.co', '3187366377', 'Física', '2020-07-15', 'Activo'),
('CC', '79234520', 'Emiliano', NULL, 'Romero', 'Rodríguez', 'prof.emiliano.romero20@littlehorizons.edu.co', '3187385660', 'Filosofía', '2021-08-15', 'Activo'),
('CC', '79234521', 'Paula', 'Antonio', 'Ruiz', 'Gómez', 'prof.paula.ruiz21@littlehorizons.edu.co', '3187404943', 'Matemáticas', '2022-09-15', 'Activo'),
('CC', '79234522', 'Martín', 'Alejandra', 'Muñoz', 'Martínez', 'prof.martin.muñoz22@littlehorizons.edu.co', '3187424226', 'Lengua Castellana', '2023-10-15', 'Activo'),
('CC', '79234523', 'Natalia', NULL, 'Guerrero', 'López', 'prof.natalia.guerrero23@littlehorizons.edu.co', '3187443509', 'Inglés', '2024-11-15', 'Activo'),
('CC', '79234524', 'Juan', 'Carlos', 'Cárdenas', 'González', 'prof.juan.cardenas24@littlehorizons.edu.co', '3187462792', 'Biología y Química', '2017-12-15', 'Activo'),
('CC', '79234525', 'Andrea', 'Elena', 'Navarro', 'Hernández', 'prof.andrea.navarro25@littlehorizons.edu.co', '3187482075', 'Ciencias Sociales', '2018-01-15', 'Activo'),
('CC', '79234526', 'Felipe', NULL, 'Pardo', 'Pérez', 'prof.felipe.pardo26@littlehorizons.edu.co', '3187501358', 'Ingeniería de Sistemas', '2019-02-15', 'Activo'),
('CC', '79234527', 'Sofía', 'Esteban', 'Salazar', 'Sánchez', 'prof.sofia.salazar27@littlehorizons.edu.co', '3187520641', 'Artes Visuales', '2020-03-15', 'Activo'),
('CC', '79234528', 'Santiago', 'Isabel', 'Rodríguez', 'Ramírez', 'prof.santiago.rodríguez28@littlehorizons.edu.co', '3187539924', 'Educación Física', '2021-04-15', 'Licencia'),
('CC', '79234529', 'Isabella', NULL, 'Gómez', 'Torres', 'prof.isabella.gomez29@littlehorizons.edu.co', '3187559207', 'Física', '2022-05-15', 'Inactivo'),
('CC', '79234530', 'Alejandro', 'José', 'Martínez', 'Flores', 'prof.alejandro.martínez30@littlehorizons.edu.co', '3187578490', 'Filosofía', '2023-06-15', 'Inactivo');

-- 7. Asignaciones de Profesor (30 registros con horarios sin conflicto)
INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion, dias_horario, hora_inicio, hora_fin) VALUES
(1, 1, 2026, 'A', 'Martes,Jueves', '07:00:00', '08:30:00'),
(2, 2, 2026, 'A', 'Miércoles,Viernes', '07:00:00', '08:30:00'),
(3, 3, 2026, 'A', 'Lunes,Viernes', '07:00:00', '08:30:00'),
(4, 4, 2026, 'A', 'Martes,Viernes', '07:00:00', '08:30:00'),
(5, 5, 2026, 'A', 'Lunes,Miércoles', '07:00:00', '08:30:00'),
(6, 6, 2026, 'A', 'Martes,Jueves', '07:00:00', '08:30:00'),
(7, 7, 2026, 'A', 'Miércoles,Viernes', '07:00:00', '08:30:00'),
(8, 8, 2026, 'A', 'Lunes,Viernes', '07:00:00', '08:30:00'),
(9, 9, 2026, 'A', 'Martes,Viernes', '07:00:00', '08:30:00'),
(10, 10, 2026, 'A', 'Lunes,Miércoles', '07:00:00', '08:30:00'),
(11, 11, 2026, 'A', 'Martes,Jueves', '07:00:00', '08:30:00'),
(12, 12, 2026, 'A', 'Miércoles,Viernes', '07:00:00', '08:30:00'),
(13, 13, 2026, 'A', 'Lunes,Viernes', '07:00:00', '08:30:00'),
(14, 14, 2026, 'A', 'Martes,Viernes', '07:00:00', '08:30:00'),
(15, 15, 2026, 'A', 'Lunes,Miércoles', '07:00:00', '08:30:00'),
(16, 16, 2026, 'A', 'Martes,Jueves', '07:00:00', '08:30:00'),
(17, 17, 2026, 'A', 'Miércoles,Viernes', '07:00:00', '08:30:00'),
(18, 18, 2026, 'A', 'Lunes,Viernes', '07:00:00', '08:30:00'),
(19, 19, 2026, 'A', 'Martes,Viernes', '07:00:00', '08:30:00'),
(20, 20, 2026, 'A', 'Lunes,Miércoles', '07:00:00', '08:30:00'),
(21, 21, 2026, 'A', 'Martes,Jueves', '07:00:00', '08:30:00'),
(22, 22, 2026, 'A', 'Miércoles,Viernes', '07:00:00', '08:30:00'),
(23, 23, 2026, 'A', 'Lunes,Viernes', '07:00:00', '08:30:00'),
(24, 24, 2026, 'A', 'Martes,Viernes', '07:00:00', '08:30:00'),
(25, 25, 2026, 'A', 'Lunes,Miércoles', '07:00:00', '08:30:00'),
(26, 26, 2026, 'A', 'Martes,Jueves', '07:00:00', '08:30:00'),
(27, 27, 2026, 'A', 'Miércoles,Viernes', '07:00:00', '08:30:00'),
(1, 28, 2026, 'B', 'Lunes,Miércoles', '10:30:00', '12:00:00'),
(2, 29, 2026, 'B', 'Martes,Jueves', '10:30:00', '12:00:00'),
(3, 30, 2026, 'B', 'Miércoles,Viernes', '10:30:00', '12:00:00');

-- 8. Inscripciones en Materias (45 registros)
INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) VALUES
(1, 1, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(2, 2, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(3, 3, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(4, 4, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(5, 5, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(6, 6, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(7, 7, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(8, 8, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(9, 9, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(10, 10, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(11, 11, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(12, 12, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(13, 13, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(14, 14, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(15, 15, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(16, 16, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(17, 17, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(18, 18, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(19, 19, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(20, 20, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(21, 21, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(22, 22, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(23, 23, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(24, 24, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(25, 25, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(26, 26, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(27, 27, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(28, 28, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(29, 29, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(1, 30, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(2, 1, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(3, 2, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(4, 3, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(5, 4, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(6, 5, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(7, 6, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(8, 7, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(9, 8, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(10, 9, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(11, 10, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(12, 11, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(13, 12, 2026, 'II', 'A', '2026-01-20', 'Activa'),
(14, 13, 2026, 'III', 'A', '2026-01-20', 'Activa'),
(15, 14, 2026, 'IV', 'A', '2026-01-20', 'Activa'),
(16, 15, 2026, 'I', 'A', '2026-01-20', 'Activa');

-- 9. Registros Académicos / Calificaciones (50 registros)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) VALUES
(1, 1, 1, 2026, 'I', 3.5, 'Buen rendimiento', '2026-03-11'),
(2, 2, 2, 2026, 'II', 4.2, 'Buen rendimiento', '2026-03-12'),
(3, 3, 3, 2026, 'III', 4.9, 'Excelente desempeño', '2026-03-13'),
(4, 4, 4, 2026, 'IV', 3.4, 'Requiere refuerzo', '2026-03-14'),
(5, 5, 5, 2026, 'I', 4.1, 'Buen rendimiento', '2026-03-15'),
(6, 6, 6, 2026, 'II', 4.8, 'Excelente desempeño', '2026-03-16'),
(7, 7, 7, 2026, 'III', 3.3, 'Requiere refuerzo', '2026-03-17'),
(8, 8, 8, 2026, 'IV', 4.0, 'Buen rendimiento', '2026-03-18'),
(9, 9, 9, 2026, 'I', 4.7, 'Excelente desempeño', '2026-03-19'),
(10, 10, 10, 2026, 'II', 3.2, 'Requiere refuerzo', '2026-03-20'),
(11, 11, 11, 2026, 'III', 3.9, 'Buen rendimiento', '2026-03-21'),
(12, 12, 12, 2026, 'IV', 4.6, 'Excelente desempeño', '2026-03-22'),
(13, 13, 13, 2026, 'I', 3.1, 'Requiere refuerzo', '2026-03-23'),
(14, 14, 14, 2026, 'II', 3.8, 'Buen rendimiento', '2026-03-24'),
(15, 15, 15, 2026, 'III', 4.5, 'Excelente desempeño', '2026-03-25'),
(16, 16, 16, 2026, 'IV', 3.0, 'Requiere refuerzo', '2026-03-26'),
(17, 17, 17, 2026, 'I', 3.7, 'Buen rendimiento', '2026-03-27'),
(18, 18, 18, 2026, 'II', 4.4, 'Buen rendimiento', '2026-03-10'),
(19, 19, 19, 2026, 'III', 2.9, 'Requiere refuerzo', '2026-03-11'),
(20, 20, 20, 2026, 'IV', 3.6, 'Buen rendimiento', '2026-03-12'),
(21, 21, 21, 2026, 'I', 4.3, 'Buen rendimiento', '2026-03-13'),
(22, 22, 22, 2026, 'II', 2.8, 'Requiere refuerzo', '2026-03-14'),
(23, 23, 23, 2026, 'III', 3.5, 'Buen rendimiento', '2026-03-15'),
(24, 24, 24, 2026, 'IV', 4.2, 'Buen rendimiento', '2026-03-16'),
(25, 25, 25, 2026, 'I', 4.9, 'Excelente desempeño', '2026-03-17'),
(26, 26, 26, 2026, 'II', 3.4, 'Requiere refuerzo', '2026-03-18'),
(27, 27, 27, 2026, 'III', 4.1, 'Buen rendimiento', '2026-03-19'),
(28, 1, 1, 2026, 'IV', 4.8, 'Excelente desempeño', '2026-03-20'),
(1, 2, 2, 2026, 'I', 3.3, 'Requiere refuerzo', '2026-03-21'),
(2, 3, 3, 2026, 'II', 4.0, 'Buen rendimiento', '2026-03-22'),
(3, 4, 4, 2026, 'III', 4.7, 'Excelente desempeño', '2026-03-23'),
(4, 5, 5, 2026, 'IV', 3.2, 'Requiere refuerzo', '2026-03-24'),
(5, 6, 6, 2026, 'I', 3.9, 'Buen rendimiento', '2026-03-25'),
(6, 7, 7, 2026, 'II', 4.6, 'Excelente desempeño', '2026-03-26'),
(7, 8, 8, 2026, 'III', 3.1, 'Requiere refuerzo', '2026-03-27'),
(8, 9, 9, 2026, 'IV', 3.8, 'Buen rendimiento', '2026-03-10'),
(9, 10, 10, 2026, 'I', 4.5, 'Excelente desempeño', '2026-03-11'),
(10, 11, 11, 2026, 'II', 3.0, 'Requiere refuerzo', '2026-03-12'),
(1, 12, 12, 2025, 'IV', 3.7, 'Buen rendimiento', '2025-03-13'),
(2, 13, 13, 2025, 'Final', 4.4, 'Buen rendimiento', '2025-03-14'),
(3, 14, 14, 2025, 'I', 2.9, 'Requiere refuerzo', '2025-03-15'),
(4, 15, 15, 2025, 'II', 3.6, 'Buen rendimiento', '2025-03-16'),
(5, 16, 16, 2025, 'III', 4.3, 'Buen rendimiento', '2025-03-17'),
(6, 17, 17, 2025, 'IV', 2.8, 'Requiere refuerzo', '2025-03-18'),
(7, 18, 18, 2025, 'Final', 3.5, 'Buen rendimiento', '2025-03-19'),
(8, 19, 19, 2025, 'I', 4.2, 'Buen rendimiento', '2025-03-20'),
(9, 20, 20, 2025, 'II', 4.9, 'Excelente desempeño', '2025-03-21'),
(10, 21, 21, 2025, 'III', 3.4, 'Requiere refuerzo', '2025-03-22'),
(11, 22, 22, 2025, 'IV', 4.1, 'Buen rendimiento', '2025-03-23'),
(12, 23, 23, 2025, 'Final', 4.8, 'Excelente desempeño', '2025-03-24');

-- 10. Asistencias (45 registros; incluye casos con >3 ausencias semanales)
INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) VALUES
(1, 1, '2026-11-02', 'Ausente', 'Inasistencia sin excusa', 1),
(1, 2, '2026-11-03', 'Ausente', 'Inasistencia sin excusa', 2),
(1, 3, '2026-11-04', 'Ausente', 'Inasistencia sin excusa', 3),
(1, 4, '2026-11-05', 'Ausente', 'Inasistencia sin excusa', 4),
(3, 1, '2026-11-02', 'Ausente', 'Ausencia reportada', 1),
(3, 2, '2026-11-03', 'Ausente', 'Ausencia reportada', 2),
(3, 3, '2026-11-04', 'Ausente', 'Ausencia reportada', 3),
(3, 5, '2026-11-05', 'Ausente', 'Ausencia reportada', 5),
(5, 1, '2026-10-02', 'Presente', 'Control diario', 1),
(6, 2, '2026-10-03', 'Presente', 'Control diario', 2),
(7, 3, '2026-10-04', 'Tardanza', 'Control diario', 3),
(8, 4, '2026-10-05', 'Excusa Médica', 'Control diario', 4),
(9, 5, '2026-10-06', 'Ausente', 'Control diario', 5),
(10, 6, '2026-10-07', 'Presente', 'Control diario', 6),
(11, 7, '2026-10-08', 'Presente', 'Control diario', 7),
(12, 8, '2026-10-09', 'Presente', 'Control diario', 8),
(13, 9, '2026-10-10', 'Tardanza', 'Control diario', 9),
(14, 10, '2026-10-11', 'Excusa Médica', 'Control diario', 10),
(15, 11, '2026-10-12', 'Ausente', 'Control diario', 11),
(16, 12, '2026-10-13', 'Presente', 'Control diario', 12),
(17, 13, '2026-10-14', 'Presente', 'Control diario', 13),
(18, 14, '2026-10-15', 'Presente', 'Control diario', 14),
(19, 15, '2026-10-16', 'Tardanza', 'Control diario', 15),
(20, 16, '2026-10-17', 'Excusa Médica', 'Control diario', 16),
(21, 17, '2026-10-18', 'Ausente', 'Control diario', 17),
(22, 18, '2026-10-19', 'Presente', 'Control diario', 18),
(23, 19, '2026-10-20', 'Presente', 'Control diario', 19),
(24, 20, '2026-10-21', 'Presente', 'Control diario', 20),
(25, 1, '2026-10-22', 'Tardanza', 'Control diario', 1),
(26, 2, '2026-10-23', 'Excusa Médica', 'Control diario', 2),
(27, 3, '2026-10-24', 'Ausente', 'Control diario', 3),
(28, 4, '2026-10-25', 'Presente', 'Control diario', 4),
(1, 5, '2026-10-01', 'Presente', 'Control diario', 5),
(2, 6, '2026-10-02', 'Presente', 'Control diario', 6),
(3, 7, '2026-10-03', 'Tardanza', 'Control diario', 7),
(4, 8, '2026-10-04', 'Excusa Médica', 'Control diario', 8),
(5, 9, '2026-10-05', 'Ausente', 'Control diario', 9),
(6, 10, '2026-10-06', 'Presente', 'Control diario', 10),
(7, 11, '2026-10-07', 'Presente', 'Control diario', 11),
(8, 12, '2026-10-08', 'Presente', 'Control diario', 12),
(9, 13, '2026-10-09', 'Tardanza', 'Control diario', 13),
(10, 14, '2026-10-10', 'Excusa Médica', 'Control diario', 14),
(11, 15, '2026-10-11', 'Ausente', 'Control diario', 15),
(12, 16, '2026-10-12', 'Presente', 'Control diario', 16),
(13, 17, '2026-10-13', 'Presente', 'Control diario', 17);

-- 11. Clubes Extracurriculares (30 registros)
INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) VALUES
('Club de Robótica', 'Actividades formativas y prácticas de robótica', 1, 21, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Ciencias', 'Actividades formativas y prácticas de ciencias', 2, 22, 'Viernes 2:00-4:00 PM', TRUE),
('Club de Debate', 'Actividades formativas y prácticas de debate', 3, 23, 'Miércoles 3:30-5:00 PM', TRUE),
('Club de Ajedrez', 'Actividades formativas y prácticas de ajedrez', 4, 24, 'Martes 3:00-5:00 PM', TRUE),
('Club de Teatro', 'Actividades formativas y prácticas de teatro', 5, 25, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Música', 'Actividades formativas y prácticas de música', 6, 26, 'Viernes 2:00-4:00 PM', TRUE),
('Club de Fútbol', 'Actividades formativas y prácticas de fútbol', 7, 27, 'Miércoles 3:30-5:00 PM', TRUE),
('Club de Baloncesto', 'Actividades formativas y prácticas de baloncesto', 8, 28, 'Martes 3:00-5:00 PM', TRUE),
('Club de Voleibol', 'Actividades formativas y prácticas de voleibol', 9, 29, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Programación', 'Actividades formativas y prácticas de programación', 10, 30, 'Viernes 2:00-4:00 PM', TRUE),
('Club de Astronomía', 'Actividades formativas y prácticas de astronomía', 11, 31, 'Miércoles 3:30-5:00 PM', TRUE),
('Club de Ecología', 'Actividades formativas y prácticas de ecología', 12, 32, 'Martes 3:00-5:00 PM', TRUE),
('Club de Fotografía', 'Actividades formativas y prácticas de fotografía', 13, 33, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Pintura', 'Actividades formativas y prácticas de pintura', 14, 34, 'Viernes 2:00-4:00 PM', TRUE),
('Club de Literatura', 'Actividades formativas y prácticas de literatura', 15, 20, 'Miércoles 3:30-5:00 PM', TRUE),
('Club de Natación', 'Actividades formativas y prácticas de natación', 16, 21, 'Martes 3:00-5:00 PM', TRUE),
('Club de Atletismo', 'Actividades formativas y prácticas de atletismo', 17, 22, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Danza', 'Actividades formativas y prácticas de danza', 18, 23, 'Viernes 2:00-4:00 PM', TRUE),
('Club de Oratoria', 'Actividades formativas y prácticas de oratoria', 19, 24, 'Miércoles 3:30-5:00 PM', TRUE),
('Club de Inglés Conversacional', 'Actividades formativas y prácticas de inglés conversacional', 20, 25, 'Martes 3:00-5:00 PM', TRUE),
('Club de Matemáticas Olímpicas', 'Actividades formativas y prácticas de matemáticas olímpicas', 21, 26, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Química Experimental', 'Actividades formativas y prácticas de química experimental', 22, 27, 'Viernes 2:00-4:00 PM', TRUE),
('Club de Cine Foro', 'Actividades formativas y prácticas de cine foro', 23, 28, 'Miércoles 3:30-5:00 PM', TRUE),
('Club de Periodismo Escolar', 'Actividades formativas y prácticas de periodismo escolar', 24, 29, 'Martes 3:00-5:00 PM', TRUE),
('Club de Cocina Saludable', 'Actividades formativas y prácticas de cocina saludable', 25, 30, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Jardinería', 'Actividades formativas y prácticas de jardinería', 26, 31, 'Viernes 2:00-4:00 PM', TRUE),
('Club de Ciclismo', 'Actividades formativas y prácticas de ciclismo', 27, 32, 'Miércoles 3:30-5:00 PM', TRUE),
('Club de Tenis de Mesa', 'Actividades formativas y prácticas de tenis de mesa', 1, 33, 'Martes 3:00-5:00 PM', TRUE),
('Club de Banda Sinfónica', 'Actividades formativas y prácticas de banda sinfónica', 2, 34, 'Jueves 3:00-5:00 PM', FALSE),
('Club de Voluntariado', 'Actividades formativas y prácticas de voluntariado', 3, 20, 'Viernes 2:00-4:00 PM', FALSE);

-- 12. Membresías en Clubes (35 registros)
INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia, fecha_retiro, motivo_retiro) VALUES
(1, 1, '2026-02-02', 'Activa', NULL, NULL),
(2, 2, '2026-02-03', 'Activa', NULL, NULL),
(3, 3, '2026-02-04', 'Activa', NULL, NULL),
(4, 4, '2026-02-05', 'Activa', NULL, NULL),
(5, 5, '2026-02-06', 'Activa', NULL, NULL),
(6, 6, '2026-02-07', 'Activa', NULL, NULL),
(7, 7, '2026-02-08', 'Activa', NULL, NULL),
(8, 8, '2026-02-09', 'Activa', NULL, NULL),
(9, 9, '2026-02-10', 'Activa', NULL, NULL),
(10, 10, '2026-02-11', 'Retirada', '2026-08-15', 'Cambio de horario'),
(11, 11, '2026-02-12', 'Activa', NULL, NULL),
(12, 12, '2026-02-13', 'Activa', NULL, NULL),
(13, 13, '2026-02-14', 'Activa', NULL, NULL),
(14, 14, '2026-02-15', 'Activa', NULL, NULL),
(15, 15, '2026-02-16', 'Activa', NULL, NULL),
(16, 16, '2026-02-17', 'Activa', NULL, NULL),
(17, 17, '2026-02-18', 'Activa', NULL, NULL),
(18, 18, '2026-02-19', 'Activa', NULL, NULL),
(19, 19, '2026-02-20', 'Activa', NULL, NULL),
(20, 20, '2026-02-01', 'Retirada', '2026-08-15', 'Cambio de horario'),
(21, 21, '2026-02-02', 'Activa', NULL, NULL),
(22, 22, '2026-02-03', 'Activa', NULL, NULL),
(23, 23, '2026-02-04', 'Activa', NULL, NULL),
(24, 24, '2026-02-05', 'Activa', NULL, NULL),
(25, 25, '2026-02-06', 'Activa', NULL, NULL),
(26, 1, '2026-02-07', 'Activa', NULL, NULL),
(27, 2, '2026-02-08', 'Activa', NULL, NULL),
(28, 3, '2026-02-09', 'Activa', NULL, NULL),
(29, 4, '2026-02-10', 'Activa', NULL, NULL),
(1, 5, '2026-02-11', 'Retirada', '2026-08-15', 'Cambio de horario'),
(2, 6, '2026-02-12', 'Activa', NULL, NULL),
(3, 7, '2026-02-13', 'Activa', NULL, NULL),
(4, 8, '2026-02-14', 'Activa', NULL, NULL),
(5, 9, '2026-02-15', 'Activa', NULL, NULL),
(6, 10, '2026-02-16', 'Activa', NULL, NULL);

-- 13. Eventos Institucionales (12 registros)
INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) VALUES
('Feria Anual de Ciencias', 'Exposición de proyectos científicos y tecnológicos', 'Científico', '2026-05-15', 'Auditorio Principal', TRUE, 1, TRUE),
('Olimpiadas Matemáticas', 'Competencia intercursos de resolución lógica', 'Académico', '2026-04-20', 'Coliseo Cubierto', TRUE, 2, TRUE),
('Festival Cultural de Primavera', 'Muestras de danza, teatro y música', 'Cultural', '2026-06-10', 'Teatro Escolar', FALSE, 7, TRUE),
('Copa Deportiva Little Horizons', 'Torneo intercolegiado de fútbol y baloncesto', 'Deportivo', '2026-08-22', 'Cancha Múltiple', TRUE, 8, TRUE),
('Día de la Familia', 'Integración de padres, acudientes y estudiantes', 'Social', '2026-09-12', 'Campus Central', FALSE, 5, TRUE),
('Hackathon Escolar', 'Desarrollo de aplicaciones educativas en equipo', 'Científico', '2026-10-08', 'Sala de Sistemas 1', TRUE, 6, TRUE),
('Concurso de Oratoria Bilingüe', 'Discursos en español e inglés', 'Académico', '2026-07-18', 'Auditorio Menor', TRUE, 3, TRUE),
('Exhibición de Arte Joven', 'Galería de pintura y fotografía estudiantil', 'Cultural', '2026-10-25', 'Hall Cultural', FALSE, 7, TRUE),
('Carrera Atlética 5K', 'Jornada deportiva de integración', 'Deportivo', '2026-11-05', 'Pista Atlética', TRUE, 8, TRUE),
('Encuentro de Egresados', 'Convivencia anual de comunidad educativa', 'Social', '2026-11-20', 'Salón Múltiple', FALSE, 10, TRUE),
('Simposio Ambiental', 'Charlas sobre sostenibilidad escolar', 'Científico', '2026-09-28', 'Biblioteca Central', FALSE, 4, TRUE),
('Torneo de Ajedrez', 'Competencia suiza de ajedrez escolar', 'Deportivo', '2026-08-05', 'Sala de Juegos', TRUE, 9, TRUE);

-- 14. Participantes en Eventos (30 registros)
INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) VALUES
(1, 1, 'Competidor', '2do Lugar', 'Participación registrada'),
(2, 2, 'Competidor', '3er Lugar', 'Participación registrada'),
(3, 3, 'Participante', 'Participante', 'Participación registrada'),
(4, 4, 'Competidor', '1er Lugar', 'Participación registrada'),
(5, 5, 'Participante', 'Participante', 'Participación registrada'),
(6, 6, 'Competidor', '3er Lugar', 'Participación registrada'),
(7, 7, 'Competidor', 'Participante', 'Participación registrada'),
(8, 8, 'Participante', 'Participante', 'Participación registrada'),
(9, 9, 'Competidor', '2do Lugar', 'Participación registrada'),
(10, 10, 'Participante', 'Participante', 'Participación registrada'),
(11, 11, 'Participante', 'Participante', 'Participación registrada'),
(12, 12, 'Competidor', '1er Lugar', 'Participación registrada'),
(1, 13, 'Competidor', '2do Lugar', 'Participación registrada'),
(2, 14, 'Competidor', '3er Lugar', 'Participación registrada'),
(3, 15, 'Participante', 'Participante', 'Participación registrada'),
(4, 16, 'Competidor', '1er Lugar', 'Participación registrada'),
(5, 17, 'Participante', 'Participante', 'Participación registrada'),
(6, 18, 'Competidor', '3er Lugar', 'Participación registrada'),
(7, 19, 'Competidor', 'Participante', 'Participación registrada'),
(8, 20, 'Participante', 'Participante', 'Participación registrada'),
(9, 21, 'Competidor', '2do Lugar', 'Participación registrada'),
(10, 22, 'Participante', 'Participante', 'Participación registrada'),
(11, 23, 'Participante', 'Participante', 'Participación registrada'),
(12, 24, 'Competidor', '1er Lugar', 'Participación registrada'),
(1, 25, 'Competidor', '2do Lugar', 'Participación registrada'),
(2, 26, 'Competidor', '3er Lugar', 'Participación registrada'),
(3, 27, 'Participante', 'Participante', 'Participación registrada'),
(4, 28, 'Competidor', '1er Lugar', 'Participación registrada'),
(5, 1, 'Participante', 'Participante', 'Participación registrada'),
(6, 2, 'Competidor', '3er Lugar', 'Participación registrada');

-- 15. Pagos (35 registros: pagados y pendientes > 30 días para reporte de mora)
INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, descuento_aplicado, estado_pago, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) VALUES
(1, 1, 1, 500000.00, 0.00, 'Pagado', '2026-02-06', 'Transferencia', 'REC-2026-0001', 2026, 'II', 'Pago confirmado en tesorería'),
(2, 1, 2, 315000.00, 35000.00, 'Pagado', '2026-03-07', 'Tarjeta Débito', 'REC-2026-0002', 2026, 'III', 'Pago confirmado en tesorería'),
(3, 3, 3, 250000.00, 0.00, 'Pagado', '2026-04-08', 'Tarjeta Crédito', 'REC-2026-0003', 2026, 'IV', 'Pago confirmado en tesorería'),
(4, 3, 4, 150000.00, 0.00, 'Pagado', '2026-05-09', 'Cheque', 'REC-2026-0004', 2026, 'Anual', 'Pago confirmado en tesorería'),
(5, 5, 5, 120000.00, 0.00, 'Pagado', '2026-06-10', 'Efectivo', 'REC-2026-0005', 2026, 'I', 'Pago confirmado en tesorería'),
(6, 5, 6, 50000.00, 0.00, 'Pagado', '2026-07-11', 'Transferencia', 'REC-2026-0006', 2026, 'II', 'Pago confirmado en tesorería'),
(7, 7, 7, 15000.00, 0.00, 'Pagado', '2026-08-12', 'Tarjeta Débito', 'REC-2026-0007', 2026, 'III', 'Pago confirmado en tesorería'),
(8, 8, 1, 500000.00, 0.00, 'Pagado', '2026-09-13', 'Tarjeta Crédito', 'REC-2026-0008', 2026, 'IV', 'Pago confirmado en tesorería'),
(9, 9, 2, 350000.00, 0.00, 'Pagado', '2026-01-14', 'Cheque', 'REC-2026-0009', 2026, 'Anual', 'Pago confirmado en tesorería'),
(10, 10, 3, 250000.00, 0.00, 'Pagado', '2026-02-15', 'Efectivo', 'REC-2026-0010', 2026, 'I', 'Pago confirmado en tesorería'),
(11, 11, 4, 150000.00, 0.00, 'Pagado', '2026-03-16', 'Transferencia', 'REC-2026-0011', 2026, 'II', 'Pago confirmado en tesorería'),
(12, 12, 5, 120000.00, 0.00, 'Pagado', '2026-04-17', 'Tarjeta Débito', 'REC-2026-0012', 2026, 'III', 'Pago confirmado en tesorería'),
(13, 13, 6, 50000.00, 0.00, 'Pagado', '2026-05-18', 'Tarjeta Crédito', 'REC-2026-0013', 2026, 'IV', 'Pago confirmado en tesorería'),
(14, 14, 7, 15000.00, 0.00, 'Pagado', '2026-06-19', 'Cheque', 'REC-2026-0014', 2026, 'Anual', 'Pago confirmado en tesorería'),
(15, 15, 1, 500000.00, 0.00, 'Pagado', '2026-07-20', 'Efectivo', 'REC-2026-0015', 2026, 'I', 'Pago confirmado en tesorería'),
(16, 16, 2, 350000.00, 0.00, 'Pagado', '2026-08-21', 'Transferencia', 'REC-2026-0016', 2026, 'II', 'Pago confirmado en tesorería'),
(17, 17, 3, 250000.00, 0.00, 'Pagado', '2026-09-22', 'Tarjeta Débito', 'REC-2026-0017', 2026, 'III', 'Pago confirmado en tesorería'),
(18, 18, 4, 150000.00, 0.00, 'Pagado', '2026-01-23', 'Tarjeta Crédito', 'REC-2026-0018', 2026, 'IV', 'Pago confirmado en tesorería'),
(19, 19, 5, 120000.00, 0.00, 'Pagado', '2026-02-24', 'Cheque', 'REC-2026-0019', 2026, 'Anual', 'Pago confirmado en tesorería'),
(20, 20, 6, 50000.00, 0.00, 'Pagado', '2026-03-05', 'Efectivo', 'REC-2026-0020', 2026, 'I', 'Pago confirmado en tesorería'),
(21, 21, 7, 15000.00, 0.00, 'Pagado', '2026-04-06', 'Transferencia', 'REC-2026-0021', 2026, 'II', 'Pago confirmado en tesorería'),
(22, 22, 1, 500000.00, 0.00, 'Pagado', '2026-05-07', 'Tarjeta Débito', 'REC-2026-0022', 2026, 'III', 'Pago confirmado en tesorería'),
(23, 23, 2, 350000.00, 0.00, 'Pagado', '2026-06-08', 'Tarjeta Crédito', 'REC-2026-0023', 2026, 'IV', 'Pago confirmado en tesorería'),
(24, 24, 3, 250000.00, 0.00, 'Pagado', '2026-07-09', 'Cheque', 'REC-2026-0024', 2026, 'Anual', 'Pago confirmado en tesorería'),
(25, 25, 4, 150000.00, 0.00, 'Pagado', '2026-08-10', 'Efectivo', 'REC-2026-0025', 2026, 'I', 'Pago confirmado en tesorería'),
(26, 26, 5, 120000.00, 0.00, 'Pagado', '2026-09-11', 'Transferencia', 'REC-2026-0026', 2026, 'II', 'Pago confirmado en tesorería'),
(27, 27, 6, 50000.00, 0.00, 'Pagado', '2026-01-12', 'Tarjeta Débito', 'REC-2026-0027', 2026, 'III', 'Pago confirmado en tesorería'),
(28, 28, 7, 15000.00, 0.00, 'Pagado', '2026-02-13', 'Tarjeta Crédito', 'REC-2026-0028', 2026, 'IV', 'Pago confirmado en tesorería'),
(29, 29, 1, 500000.00, 0.00, 'Pagado', '2026-03-14', 'Cheque', 'REC-2026-0029', 2026, 'Anual', 'Pago confirmado en tesorería'),
(1, 1, 2, 350000.00, 0.00, 'Pagado', '2026-04-15', 'Efectivo', 'REC-2026-0030', 2026, 'I', 'Pago confirmado en tesorería'),
(2, 1, 3, 250000.00, 0.00, 'Pendiente', '2026-08-11', 'Transferencia', NULL, 2026, 'II', 'Pendiente por mora superior a 30 días'),
(3, 3, 4, 150000.00, 0.00, 'Pendiente', '2026-08-12', 'Tarjeta Débito', NULL, 2026, 'III', 'Pendiente por mora superior a 30 días'),
(4, 3, 5, 120000.00, 0.00, 'Pendiente', '2026-08-13', 'Tarjeta Crédito', NULL, 2026, 'IV', 'Pendiente por mora superior a 30 días'),
(5, 5, 6, 50000.00, 0.00, 'Pendiente', '2026-08-14', 'Cheque', NULL, 2026, 'Anual', 'Pendiente por mora superior a 30 días'),
(6, 5, 7, 15000.00, 0.00, 'Pendiente', '2026-08-15', 'Efectivo', NULL, 2026, 'I', 'Pendiente por mora superior a 30 días');

-- 16. Facturas Electrónicas (30 registros vinculados a pagos confirmados)
INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, ruta_archivo) VALUES
('FE-2026-000001', 1, '2026-02-06', '9a4afc81405109ae859ce18b67684cd5d0b7ddd6f525466120a0684e118fc17b', 'factura_000001.txt', 'invoices/factura_000001.txt'),
('FE-2026-000002', 2, '2026-03-07', 'cc40c65fddfafa1937a9a7e5f6f313ccba0b8932bbab87add273f0de8e996175', 'factura_000002.txt', 'invoices/factura_000002.txt'),
('FE-2026-000003', 3, '2026-04-08', 'c6c94b96e90142f7a9a88429c032c2315ce0d3dd2e0cdf4b0be8ebbbf9647bfd', 'factura_000003.txt', 'invoices/factura_000003.txt'),
('FE-2026-000004', 4, '2026-05-09', '8fddce1bd5035a3d46816318b33a6d4c43fa6083ca16b13d9b2857714c5c184f', 'factura_000004.txt', 'invoices/factura_000004.txt'),
('FE-2026-000005', 5, '2026-06-10', 'ef55dd6b4fe0a230733346118a405d650be12cd4a9c5c6e426c48dfa7219411c', 'factura_000005.txt', 'invoices/factura_000005.txt'),
('FE-2026-000006', 6, '2026-07-11', '083dec86b53ec662c9782e8100f167d024be703d1349547da3ef9e4ba6e47714', 'factura_000006.txt', 'invoices/factura_000006.txt'),
('FE-2026-000007', 7, '2026-08-12', 'e72057ac84c846fe3827ae594952a995dfd1f62273788c55e73bac8c048c1afc', 'factura_000007.txt', 'invoices/factura_000007.txt'),
('FE-2026-000008', 8, '2026-09-13', '4d029ea643a70f38a57210afd5eb3fc44a7a06fb4954c6b992e7103656feaab2', 'factura_000008.txt', 'invoices/factura_000008.txt'),
('FE-2026-000009', 9, '2026-01-14', 'c596768d98c8a711b0fe56390fe74e923d02aa8e893ea775b9ed07a5dc9b4201', 'factura_000009.txt', 'invoices/factura_000009.txt'),
('FE-2026-000010', 10, '2026-02-15', '014551730e79fec08bd36b740625c6f92502fc71694da6978e3f569f3da36318', 'factura_000010.txt', 'invoices/factura_000010.txt'),
('FE-2026-000011', 11, '2026-03-16', '50e564c66825925af191772898aeb17dec72d34b29dc607894a60b1e82300503', 'factura_000011.txt', 'invoices/factura_000011.txt'),
('FE-2026-000012', 12, '2026-04-17', '17435fa17744867068644bbee5981af6a37c6046475295173c9f870db06eefed', 'factura_000012.txt', 'invoices/factura_000012.txt'),
('FE-2026-000013', 13, '2026-05-18', '8a49e253db769dcb7cd80098f5e69837a4d7eeed73ddcab3e9e8056dcccb45b7', 'factura_000013.txt', 'invoices/factura_000013.txt'),
('FE-2026-000014', 14, '2026-06-19', '33fd84476e769252bd075f7c5b19a06a34fc79ba58ede02187b6cadd18893368', 'factura_000014.txt', 'invoices/factura_000014.txt'),
('FE-2026-000015', 15, '2026-07-20', 'a8e71c27d98c58522a57dd8994545795d4ddb9fc33fe66ff843c8943f814053d', 'factura_000015.txt', 'invoices/factura_000015.txt'),
('FE-2026-000016', 16, '2026-08-21', '590cd690cae60a2fcf477ce16fd168c46b5bf9b6577bb348fa1e36ab2307bd8e', 'factura_000016.txt', 'invoices/factura_000016.txt'),
('FE-2026-000017', 17, '2026-09-22', '05507f531f8e5dfc7ae57e3d0b372017df9449f1633097ffeceb00ce752c342f', 'factura_000017.txt', 'invoices/factura_000017.txt'),
('FE-2026-000018', 18, '2026-01-23', '3288ccfdd7f6d70cf6b9bf4c8f41922a03677139f88403d0e5f9173b8efbe6b1', 'factura_000018.txt', 'invoices/factura_000018.txt'),
('FE-2026-000019', 19, '2026-02-24', 'f6641166a4d4bd2c4d66d92d4d66c12223e74cc81e3807f99dd526a679dbfbeb', 'factura_000019.txt', 'invoices/factura_000019.txt'),
('FE-2026-000020', 20, '2026-03-05', '801c78df8cfa08eb2afdc32ea6dd48f2158f630ba778617e01f663bb75304dd1', 'factura_000020.txt', 'invoices/factura_000020.txt'),
('FE-2026-000021', 21, '2026-04-06', 'c48f9cc87941ad2cfc238f9cc94800b8ae4b79c253b66091f0d2b308016bd4d4', 'factura_000021.txt', 'invoices/factura_000021.txt'),
('FE-2026-000022', 22, '2026-05-07', '6fec0b145abf7123e3c97067c3094867599af4b3be3373c56ef8e06fcfdb4ce1', 'factura_000022.txt', 'invoices/factura_000022.txt'),
('FE-2026-000023', 23, '2026-06-08', 'cfcc89c7bd70474ffe000b33c343d0bfa8bf564dbca55aae99ff34ebba1674af', 'factura_000023.txt', 'invoices/factura_000023.txt'),
('FE-2026-000024', 24, '2026-07-09', '53f2c035a56199a0d2a53eea086891b418b4f866edf75186191c0eb70d72fd39', 'factura_000024.txt', 'invoices/factura_000024.txt'),
('FE-2026-000025', 25, '2026-08-10', 'e5e25abd2a3ca1b414087ed05b23087eb42a7e0dd9ba821fb9b8749fe87a9c3b', 'factura_000025.txt', 'invoices/factura_000025.txt'),
('FE-2026-000026', 26, '2026-09-11', '3d79bd0e4762ae92ff583413c3ab76bba2360d006c77b3a2ee4bb04f6e10ec30', 'factura_000026.txt', 'invoices/factura_000026.txt'),
('FE-2026-000027', 27, '2026-01-12', 'bdfa0f51332b85eb741ba00b66dd3c94439ee1606d530a88850ed9538d049404', 'factura_000027.txt', 'invoices/factura_000027.txt'),
('FE-2026-000028', 28, '2026-02-13', '4e36c4472735ff40150bb058f1259fbbdba8fa8d089de49d95d5a3f7bc396965', 'factura_000028.txt', 'invoices/factura_000028.txt'),
('FE-2026-000029', 29, '2026-03-14', 'b546af09bb1decfd525dc75dddcd363551a3e66a79c3d06701bf48b9b1338b62', 'factura_000029.txt', 'invoices/factura_000029.txt'),
('FE-2026-000030', 30, '2026-04-15', 'c19861cb5cbb7eb6dc44328e5446cf42ea740d787fcc737e8a406505ad2cb6dc', 'factura_000030.txt', 'invoices/factura_000030.txt');

-- 17. Recursos de Biblioteca y Laboratorios (32 registros)
INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES
('REC-0001', 'Cálculo de Stewart 8a Ed. #1', 1, 'Recurso institucional inventariado', '2024-03-02', 'Biblioteca Central - Estante A1', 'Disponible'),
('REC-0002', 'Microscopio Óptico Binocular #1', 2, 'Recurso institucional inventariado', '2024-03-03', 'Laboratorio de Biología', 'Disponible'),
('REC-0003', 'Balón de Fútbol Profesional #1', 3, 'Recurso institucional inventariado', '2024-03-04', 'Bodega Deportiva', 'Disponible'),
('REC-0004', 'Tablet Samsung Galaxy Tab A8 #1', 4, 'Recurso institucional inventariado', '2024-03-05', 'Sala de Tecnología', 'Disponible'),
('REC-0005', 'Globo Terráqueo Político #1', 5, 'Recurso institucional inventariado', '2024-03-06', 'Salón de Sociales 201', 'Disponible'),
('REC-0006', 'Cien Años de Soledad #1', 1, 'Recurso institucional inventariado', '2024-03-07', 'Biblioteca Central - Literatura', 'Disponible'),
('REC-0007', 'Balanza Analítica Digital #1', 2, 'Recurso institucional inventariado', '2024-03-08', 'Laboratorio de Química', 'Disponible'),
('REC-0008', 'Calculadora Científica Casio fx-991 #1', 4, 'Recurso institucional inventariado', '2024-03-09', 'Coordinación Académica', 'Disponible'),
('REC-0009', 'Kit de Robótica Arduino Uno #1', 4, 'Recurso institucional inventariado', '2024-03-10', 'Laboratorio de Robótica', 'Disponible'),
('REC-0010', 'Esqueleto Anatómico Escala 1:2 #1', 5, 'Recurso institucional inventariado', '2024-03-11', 'Laboratorio de Biología', 'Disponible'),
('REC-0011', 'Física Universitaria Sears-Zemansky #1', 1, 'Recurso institucional inventariado', '2024-03-12', 'Biblioteca Central - Estante B3', 'Disponible'),
('REC-0012', 'Osciloscopio Digital 100MHz #1', 2, 'Recurso institucional inventariado', '2024-03-13', 'Laboratorio de Física', 'Disponible'),
('REC-0013', 'Set de Balones de Baloncesto #1', 3, 'Recurso institucional inventariado', '2024-03-14', 'Coliseo Cubierto', 'Disponible'),
('REC-0014', 'Portátil Lenovo ThinkPad E14 #1', 4, 'Recurso institucional inventariado', '2024-03-15', 'Sala de Docentes', 'Disponible'),
('REC-0015', 'Mapa Físico de Colombia #1', 5, 'Recurso institucional inventariado', '2024-03-16', 'Salón de Geografía', 'Disponible'),
('REC-0016', 'Química General Raymond Chang #1', 1, 'Recurso institucional inventariado', '2024-03-17', 'Biblioteca Central - Estante B4', 'Disponible'),
('REC-0017', 'Cálculo de Stewart 8a Ed. #2', 1, 'Recurso institucional inventariado', '2024-03-18', 'Biblioteca Central - Estante A1', 'Disponible'),
('REC-0018', 'Microscopio Óptico Binocular #2', 2, 'Recurso institucional inventariado', '2024-03-19', 'Laboratorio de Biología', 'Disponible'),
('REC-0019', 'Balón de Fútbol Profesional #2', 3, 'Recurso institucional inventariado', '2024-03-20', 'Bodega Deportiva', 'Disponible'),
('REC-0020', 'Tablet Samsung Galaxy Tab A8 #2', 4, 'Recurso institucional inventariado', '2024-03-21', 'Sala de Tecnología', 'Disponible'),
('REC-0021', 'Globo Terráqueo Político #2', 5, 'Recurso institucional inventariado', '2024-03-22', 'Salón de Sociales 201', 'Disponible'),
('REC-0022', 'Cien Años de Soledad #2', 1, 'Recurso institucional inventariado', '2024-03-23', 'Biblioteca Central - Literatura', 'Disponible'),
('REC-0023', 'Balanza Analítica Digital #2', 2, 'Recurso institucional inventariado', '2024-03-24', 'Laboratorio de Química', 'Disponible'),
('REC-0024', 'Calculadora Científica Casio fx-991 #2', 4, 'Recurso institucional inventariado', '2024-03-25', 'Coordinación Académica', 'Disponible'),
('REC-0025', 'Kit de Robótica Arduino Uno #2', 4, 'Recurso institucional inventariado', '2024-03-01', 'Laboratorio de Robótica', 'Prestado'),
('REC-0026', 'Esqueleto Anatómico Escala 1:2 #2', 5, 'Recurso institucional inventariado', '2024-03-02', 'Laboratorio de Biología', 'Prestado'),
('REC-0027', 'Física Universitaria Sears-Zemansky #2', 1, 'Recurso institucional inventariado', '2024-03-03', 'Biblioteca Central - Estante B3', 'Prestado'),
('REC-0028', 'Osciloscopio Digital 100MHz #2', 2, 'Recurso institucional inventariado', '2024-03-04', 'Laboratorio de Física', 'Prestado'),
('REC-0029', 'Set de Balones de Baloncesto #2', 3, 'Recurso institucional inventariado', '2024-03-05', 'Coliseo Cubierto', 'En Mantenimiento'),
('REC-0030', 'Portátil Lenovo ThinkPad E14 #2', 4, 'Recurso institucional inventariado', '2024-03-06', 'Sala de Docentes', 'En Mantenimiento'),
('REC-0031', 'Mapa Físico de Colombia #2', 5, 'Recurso institucional inventariado', '2024-03-07', 'Salón de Geografía', 'Dado de Baja'),
('REC-0032', 'Química General Raymond Chang #2', 1, 'Recurso institucional inventariado', '2024-03-08', 'Biblioteca Central - Estante B4', 'Dado de Baja');

-- 18. Préstamos a Estudiantes (12 registros)
INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(25, 1, '2026-10-01', '2026-10-15', NULL, 'Vencido', 'Libro con fecha de devolución vencida'),
(26, 2, '2026-11-10', '2026-11-24', NULL, 'Activo', 'Préstamo vigente para laboratorio'),
(1, 3, '2026-08-02', '2026-08-09', '2026-08-08', 'Devuelto', 'Devuelto en buen estado'),
(2, 4, '2026-08-03', '2026-08-10', '2026-08-09', 'Devuelto', 'Devuelto en buen estado'),
(3, 5, '2026-08-04', '2026-08-11', '2026-08-10', 'Devuelto', 'Devuelto en buen estado'),
(4, 6, '2026-08-05', '2026-08-12', '2026-08-11', 'Devuelto', 'Devuelto en buen estado'),
(5, 7, '2026-08-06', '2026-08-13', '2026-08-12', 'Devuelto', 'Devuelto en buen estado'),
(6, 8, '2026-08-07', '2026-08-14', '2026-08-13', 'Devuelto', 'Devuelto en buen estado'),
(7, 9, '2026-08-08', '2026-08-15', '2026-08-14', 'Devuelto', 'Devuelto en buen estado'),
(8, 10, '2026-08-09', '2026-08-16', '2026-08-15', 'Devuelto', 'Devuelto en buen estado'),
(9, 11, '2026-08-10', '2026-08-17', '2026-08-16', 'Devuelto', 'Devuelto en buen estado'),
(10, 12, '2026-08-11', '2026-08-18', '2026-08-17', 'Devuelto', 'Devuelto en buen estado');

-- 19. Préstamos a Profesores (10 registros)
INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(27, 1, '2026-10-05', '2026-10-20', NULL, 'Vencido', 'Equipo pendiente por devolver'),
(28, 2, '2026-11-12', '2026-11-26', NULL, 'Activo', 'Uso en clase de ciencias'),
(11, 3, '2026-09-12', '2026-09-21', '2026-09-20', 'Devuelto', 'Devuelto por docente sin novedad'),
(12, 4, '2026-09-13', '2026-09-22', '2026-09-21', 'Devuelto', 'Devuelto por docente sin novedad'),
(13, 5, '2026-09-14', '2026-09-23', '2026-09-22', 'Devuelto', 'Devuelto por docente sin novedad'),
(14, 6, '2026-09-15', '2026-09-24', '2026-09-23', 'Devuelto', 'Devuelto por docente sin novedad'),
(15, 7, '2026-09-01', '2026-09-10', '2026-09-09', 'Devuelto', 'Devuelto por docente sin novedad'),
(16, 8, '2026-09-02', '2026-09-11', '2026-09-10', 'Devuelto', 'Devuelto por docente sin novedad'),
(17, 9, '2026-09-03', '2026-09-12', '2026-09-11', 'Devuelto', 'Devuelto por docente sin novedad'),
(18, 10, '2026-09-04', '2026-09-13', '2026-09-12', 'Devuelto', 'Devuelto por docente sin novedad');

-- Verificación final de tablas creadas
SELECT TABLE_NAME AS 'Tabla Creada', TABLE_ROWS AS 'Filas Estimadas' FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'erp_little_horizons' ORDER BY TABLE_NAME;
