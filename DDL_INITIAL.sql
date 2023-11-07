-- Crear la tabla TCLIENTE
CREATE TABLE TCLIENTE (
    id serial PRIMARY KEY,
    nombre varchar(255),
    apellido varchar(255),
    Telefono varchar(255),
    direccion varchar(255)
);





-- Crear la tabla TPEDIDOCABECERA
CREATE TABLE TPEDIDOCABECERA (
    ID serial PRIMARY KEY,
    FECHA date,
    TOTAL numeric(8, 2),
    ESTADO varchar(255),
    ccliente integer
);





-- Crear la tabla TPEDIDODETALLE
CREATE TABLE TPEDIDODETALLE (
    ID serial PRIMARY KEY,
    FECHA date,
    UNIDADES integer,
    DESCRIPCION varchar(255),
    VUNITARIO numeric(8, 2),
    TOTAL numeric(8, 2),
    ccabecera integer
);




ALTER TABLE TPEDIDOCABECERA
ADD CONSTRAINT fk_ccliente
FOREIGN KEY (ccliente) REFERENCES TCLIENTE (id);


ALTER TABLE TPEDIDODETALLE
ADD CONSTRAINT fk_ccabecera
FOREIGN KEY (ccabecera) REFERENCES TPEDIDOCABECERA (ID);
