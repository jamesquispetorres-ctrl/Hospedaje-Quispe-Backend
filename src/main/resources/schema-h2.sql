-- Schema DDL para base de datos H2 (Modo Demo)
CREATE TABLE IF NOT EXISTS habitaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    piso INT NOT NULL,
    precio_mensual DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS huespedes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS contratos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    huesped_id BIGINT NOT NULL,
    habitacion_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    dia_pago_mensual INT NOT NULL,
    monto_mensual_pactado DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(500),
    fecha_creacion TIMESTAMP NOT NULL,
    FOREIGN KEY (huesped_id) REFERENCES huespedes(id),
    FOREIGN KEY (habitacion_id) REFERENCES habitaciones(id)
);

CREATE TABLE IF NOT EXISTS servicios_adicionales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    tarifa_fija DECIMAL(10,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contrato_id BIGINT NOT NULL,
    periodo_mes_anio VARCHAR(20) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago DATE,
    monto_habitacion DECIMAL(10,2) NOT NULL,
    monto_servicios DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    monto_total DECIMAL(10,2) NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    saldo_pendiente DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    metodo_pago VARCHAR(30),
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    comprobante_url VARCHAR(300),
    observaciones VARCHAR(500),
    fecha_registro TIMESTAMP NOT NULL,
    FOREIGN KEY (contrato_id) REFERENCES contratos(id)
);

CREATE TABLE IF NOT EXISTS pago_servicios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pago_id BIGINT NOT NULL,
    servicio_adicional_id BIGINT,
    nombre_servicio VARCHAR(100) NOT NULL,
    costo_cobrado DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pago_id) REFERENCES pagos(id),
    FOREIGN KEY (servicio_adicional_id) REFERENCES servicios_adicionales(id)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
