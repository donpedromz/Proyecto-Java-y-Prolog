-- Reset y creación de base de datos
DROP DATABASE IF EXISTS health_db;
CREATE DATABASE health_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE health_db;

-- Esquema principal
CREATE TABLE category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disease (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    category_id INT NULL,
    CONSTRAINT fk_disease_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE symptom (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recommendation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disease_symptom (
    disease_id INT NOT NULL,
    symptom_id INT NOT NULL,
    PRIMARY KEY (disease_id, symptom_id),
    CONSTRAINT fk_ds_disease FOREIGN KEY (disease_id) REFERENCES disease(id) ON DELETE CASCADE,
    CONSTRAINT fk_ds_symptom FOREIGN KEY (symptom_id) REFERENCES symptom(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disease_recommendation (
    disease_id INT NOT NULL,
    recommendation_id INT NOT NULL,
    PRIMARY KEY (disease_id, recommendation_id),
    CONSTRAINT fk_dr_disease FOREIGN KEY (disease_id) REFERENCES disease(id) ON DELETE CASCADE,
    CONSTRAINT fk_dr_rec FOREIGN KEY (recommendation_id) REFERENCES recommendation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    age INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diagnostic (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(150) NOT NULL,
    patient_age INT NOT NULL,
    disease_id INT NOT NULL,
    disease_name VARCHAR(200) NOT NULL,
    category VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_diagnostic_disease FOREIGN KEY (disease_id) REFERENCES disease(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diagnostic_symptom (
    id INT AUTO_INCREMENT PRIMARY KEY,
    diagnostic_id INT NOT NULL,
    symptom_description VARCHAR(255) NOT NULL,
    CONSTRAINT fk_diag_sym_diagnostic FOREIGN KEY (diagnostic_id) REFERENCES diagnostic(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diagnostic_recomendation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    diagnostic_id INT NOT NULL,
    recomendation_description VARCHAR(255) NOT NULL,
    CONSTRAINT fk_diag_rec_diagnostic FOREIGN KEY (diagnostic_id) REFERENCES diagnostic(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Categorías
INSERT INTO category (name) VALUES
    ('viral'),
    ('digestiva'),
    ('cronica')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Enfermedades
INSERT INTO disease (name, category_id)
SELECT 'gripa', c.id FROM category c WHERE c.name = 'viral'
ON DUPLICATE KEY UPDATE category_id = category_id;

INSERT INTO disease (name, category_id)
SELECT 'covid19', c.id FROM category c WHERE c.name = 'viral'
ON DUPLICATE KEY UPDATE category_id = category_id;

INSERT INTO disease (name, category_id)
SELECT 'gastritis', c.id FROM category c WHERE c.name = 'digestiva'
ON DUPLICATE KEY UPDATE category_id = category_id;

INSERT INTO disease (name, category_id)
SELECT 'diabetes', c.id FROM category c WHERE c.name = 'cronica'
ON DUPLICATE KEY UPDATE category_id = category_id;

INSERT INTO disease (name, category_id)
SELECT 'hipertension', c.id FROM category c WHERE c.name = 'cronica'
ON DUPLICATE KEY UPDATE category_id = category_id;

-- Síntomas
INSERT INTO symptom (code, description) VALUES
    ('fiebre', 'Fiebre'),
    ('tos', 'Tos'),
    ('dolor_cabeza', 'Dolor de cabeza'),
    ('dolor_muscular', 'Dolor muscular'),
    ('dificultad_respirar', 'Dificultad para respirar'),
    ('perdida_olfato', 'Perdida del olfato'),
    ('perdida_gusto', 'Perdida del gusto'),
    ('dolor_abdominal', 'Dolor abdominal'),
    ('acidez', 'Acidez'),
    ('nausea', 'Nausea'),
    ('vomito', 'Vomito'),
    ('sed_excesiva', 'Sed excesiva'),
    ('hambre_constante', 'Hambre constante'),
    ('perdida_peso', 'Perdida de peso'),
    ('fatiga', 'Fatiga'),
    ('mareo', 'Mareo'),
    ('vision_borrosa', 'Vision borrosa'),
    ('palpitaciones', 'Palpitaciones')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Recomendaciones
INSERT INTO recommendation (code, description) VALUES
    ('descansar', 'Descansar'),
    ('hidratar', 'Hidratar'),
    ('consultar_medico', 'Consultar al medico'),
    ('aislamiento', 'Aislamiento'),
    ('dieta_blanda', 'Dieta blanda'),
    ('antiacidos', 'Antiacidos'),
    ('control_glucosa', 'Control de glucosa'),
    ('dieta_equilibrada', 'Dieta equilibrada'),
    ('actividad_fisica', 'Actividad fisica moderada'),
    ('reducir_sal', 'Reducir consumo de sal'),
    ('ejercicio_moderado', 'Ejercicio moderado'),
    ('controlar_peso', 'Controlar peso')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Relaciones enfermedad-síntoma
INSERT INTO disease_symptom (disease_id, symptom_id)
SELECT d.id, s.id FROM disease d
JOIN symptom s ON s.code IN ('fiebre','tos','dolor_cabeza','dolor_muscular')
WHERE d.name = 'gripa'
ON DUPLICATE KEY UPDATE symptom_id = symptom_id;

INSERT INTO disease_symptom (disease_id, symptom_id)
SELECT d.id, s.id FROM disease d
JOIN symptom s ON s.code IN ('fiebre','tos','dificultad_respirar','perdida_olfato','perdida_gusto')
WHERE d.name = 'covid19'
ON DUPLICATE KEY UPDATE symptom_id = symptom_id;

INSERT INTO disease_symptom (disease_id, symptom_id)
SELECT d.id, s.id FROM disease d
JOIN symptom s ON s.code IN ('dolor_abdominal','acidez','nausea','vomito')
WHERE d.name = 'gastritis'
ON DUPLICATE KEY UPDATE symptom_id = symptom_id;

INSERT INTO disease_symptom (disease_id, symptom_id)
SELECT d.id, s.id FROM disease d
JOIN symptom s ON s.code IN ('sed_excesiva','hambre_constante','perdida_peso','fatiga')
WHERE d.name = 'diabetes'
ON DUPLICATE KEY UPDATE symptom_id = symptom_id;

INSERT INTO disease_symptom (disease_id, symptom_id)
SELECT d.id, s.id FROM disease d
JOIN symptom s ON s.code IN ('dolor_cabeza','mareo','vision_borrosa','palpitaciones')
WHERE d.name = 'hipertension'
ON DUPLICATE KEY UPDATE symptom_id = symptom_id;

-- Relaciones enfermedad-recomendación
INSERT INTO disease_recommendation (disease_id, recommendation_id)
SELECT d.id, r.id FROM disease d
JOIN recommendation r ON r.code IN ('descansar','hidratar','consultar_medico')
WHERE d.name = 'gripa'
ON DUPLICATE KEY UPDATE recommendation_id = recommendation_id;

INSERT INTO disease_recommendation (disease_id, recommendation_id)
SELECT d.id, r.id FROM disease d
JOIN recommendation r ON r.code IN ('aislamiento','hidratar','consultar_medico')
WHERE d.name = 'covid19'
ON DUPLICATE KEY UPDATE recommendation_id = recommendation_id;

INSERT INTO disease_recommendation (disease_id, recommendation_id)
SELECT d.id, r.id FROM disease d
JOIN recommendation r ON r.code IN ('dieta_blanda','antiacidos','consultar_medico')
WHERE d.name = 'gastritis'
ON DUPLICATE KEY UPDATE recommendation_id = recommendation_id;

INSERT INTO disease_recommendation (disease_id, recommendation_id)
SELECT d.id, r.id FROM disease d
JOIN recommendation r ON r.code IN ('control_glucosa','dieta_equilibrada','actividad_fisica','consultar_medico')
WHERE d.name = 'diabetes'
ON DUPLICATE KEY UPDATE recommendation_id = recommendation_id;

INSERT INTO disease_recommendation (disease_id, recommendation_id)
SELECT d.id, r.id FROM disease d
JOIN recommendation r ON r.code IN ('reducir_sal','ejercicio_moderado','controlar_peso','consultar_medico')
WHERE d.name = 'hipertension'
ON DUPLICATE KEY UPDATE recommendation_id = recommendation_id;
