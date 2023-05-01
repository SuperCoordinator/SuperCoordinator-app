/*******************************************************************************
   Create Tables
********************************************************************************/

CREATE TABLE IF NOT EXISTS sf_distribution (
    name VARCHAR(500) NOT NULL,
    time_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS inbound_orders (
    id MEDIUMINT NOT NULL AUTO_INCREMENT,
    order_date TIMESTAMP NOT NULL,
    metal_qty INT NOT NULL,
    green_qty INT NOT NULL,
    blue_qty INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS outbound_orders (
    id INT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS part(
    id INT NOT NULL,
    status VARCHAR(500) NOT NULL,
    fk_sf_distribution VARCHAR(500) NOT NULL,
    fk_inbound_orders INT NOT NULL,
    fk_outbound_orders INT,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS production_history(
    time_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    material VARCHAR(500) NOT NULL,
    form VARCHAR(500) NOT NULL,
    fk_part_id INT NOT NULL,
    fk_sensor_name VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS sfem (
    name VARCHAR(500) NOT NULL,
    fk_sf_distribution VARCHAR(500) NOT NULL,
    PRIMARY KEY(name)
);

CREATE TABLE IF NOT EXISTS sfee (
    name VARCHAR(500) NOT NULL,
    fk_sfem VARCHAR(500) NOT NULL,
    PRIMARY KEY(name)
);

CREATE TABLE IF NOT EXISTS sfei (
    name VARCHAR(500) NOT NULL,
    fk_sfee VARCHAR(500) NOT NULL,
    fk_in_sensor VARCHAR (500) NOT NULL,
    fk_out_sensor VARCHAR (500) NOT NULL,
    PRIMARY KEY(name)
);

CREATE TABLE IF NOT EXISTS sensor(
    name VARCHAR(500) NOT NULL,
    PRIMARY KEY (name)
);

/*******************************************************************************
   Create Foreign Keys
********************************************************************************/
/* part(*) -- (1) sf_distribution */

ALTER TABLE part
ADD FOREIGN KEY (fk_sf_distribution) REFERENCES sf_distribution(name) ON DELETE CASCADE ON UPDATE NO ACTION,
ADD FOREIGN KEY (fk_inbound_orders) REFERENCES inbound_orders(id) ON DELETE CASCADE ON UPDATE NO ACTION,
ADD FOREIGN KEY (fk_outbound_orders) REFERENCES outbound_orders(id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE production_history
ADD FOREIGN KEY (fk_part_id) REFERENCES part(id) ON DELETE CASCADE ON UPDATE NO ACTION,
ADD FOREIGN KEY (fk_sensor_name) REFERENCES sensor(name) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE sfem
ADD FOREIGN KEY (fk_sf_distribution) REFERENCES sf_distribution(name) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE sfee
ADD FOREIGN KEY (fk_sfem) REFERENCES sfem(name) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE sfei
ADD FOREIGN KEY (fk_sfee) REFERENCES sfee(name) ON DELETE CASCADE ON UPDATE NO ACTION,
ADD FOREIGN KEY (fk_in_sensor) REFERENCES sensor(name) ON DELETE CASCADE ON UPDATE NO ACTION,
ADD FOREIGN KEY (fk_out_sensor) REFERENCES sensor(name) ON DELETE CASCADE ON UPDATE NO ACTION;