-- Seed data for branches table.
-- Uses INSERT ... ON CONFLICT DO NOTHING to be idempotent (requires PostgreSQL 9.5+).
-- Activate with: spring.sql.init.mode=always in application.properties.

INSERT INTO branches (name, address, specialty, phone, capacity, active, latitude, longitude)
VALUES
  ('Sede Norte',     'Avenida Autopista Norte 150',  'General',   '1234567', 100, true, 4.7110, -74.0721),
  ('Sede Chapinero', 'Carrera 15 # 72',              'Pediatría', '7654321',  80, true, 4.6589, -74.0621),
  ('Sede Sur',       'Avenida Caracas # 40 sur',     'Urgencias', '150',       60, true, 4.5789, -74.1221)
ON CONFLICT DO NOTHING;
