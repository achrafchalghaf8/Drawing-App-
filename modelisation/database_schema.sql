-- Script de création de la base de données pour l'application de modélisation
-- À exécuter dans phpMyAdmin

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS modelisation_app 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE modelisation_app;

-- Table pour les logs de l'application
CREATE TABLE IF NOT EXISTS application_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    level ENUM('DEBUG', 'INFO', 'WARNING', 'ERROR') NOT NULL,
    message TEXT NOT NULL,
    exception_details TEXT NULL,
    session_id VARCHAR(255) NULL,
    user_action VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table pour les dessins
CREATE TABLE IF NOT EXISTS drawings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    json_data LONGTEXT NOT NULL,
    shape_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    session_id VARCHAR(255) NULL
);

-- Table pour les formes individuelles (pour analyse détaillée)
CREATE TABLE IF NOT EXISTS shapes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    drawing_id INT NOT NULL,
    shape_type ENUM('CIRCLE', 'RECTANGLE', 'LINE') NOT NULL,
    shape_label VARCHAR(10) NOT NULL,
    position_x DOUBLE NOT NULL,
    position_y DOUBLE NOT NULL,
    param1 DOUBLE NULL, -- radius pour cercle, width pour rectangle, endX pour ligne
    param2 DOUBLE NULL, -- height pour rectangle, endY pour ligne
    color VARCHAR(20) NOT NULL,
    stroke_width DOUBLE NOT NULL DEFAULT 2.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
);

-- Table pour les sessions de plus court chemin
CREATE TABLE IF NOT EXISTS shortest_path_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    drawing_id INT NOT NULL,
    algorithm_used ENUM('DIJKSTRA', 'BFS') NOT NULL,
    start_shape_label VARCHAR(10) NOT NULL,
    end_shape_label VARCHAR(10) NOT NULL,
    path_length INT NOT NULL,
    total_distance DOUBLE NOT NULL,
    path_nodes TEXT NOT NULL, -- JSON array des nœuds du chemin
    execution_time_ms BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_logs_timestamp ON application_logs(timestamp);
CREATE INDEX idx_logs_level ON application_logs(level);
CREATE INDEX idx_drawings_created ON drawings(created_at);
CREATE INDEX idx_shapes_drawing ON shapes(drawing_id);
CREATE INDEX idx_shapes_type ON shapes(shape_type);
CREATE INDEX idx_shortest_path_drawing ON shortest_path_sessions(drawing_id);

-- Vues utiles pour les rapports
CREATE VIEW drawing_statistics AS
SELECT 
    d.id,
    d.name,
    d.shape_count,
    d.created_at,
    COUNT(sps.id) as shortest_path_calculations,
    AVG(sps.execution_time_ms) as avg_execution_time
FROM drawings d
LEFT JOIN shortest_path_sessions sps ON d.id = sps.drawing_id
GROUP BY d.id, d.name, d.shape_count, d.created_at;

CREATE VIEW recent_activity AS
SELECT 
    'LOG' as activity_type,
    al.timestamp as activity_time,
    al.level as severity,
    al.message as description,
    al.session_id
FROM application_logs al
UNION ALL
SELECT 
    'DRAWING' as activity_type,
    d.created_at as activity_time,
    'INFO' as severity,
    CONCAT('Drawing created: ', d.name) as description,
    d.session_id
FROM drawings d
UNION ALL
SELECT 
    'SHORTEST_PATH' as activity_type,
    sps.created_at as activity_time,
    'INFO' as severity,
    CONCAT('Shortest path calculated: ', sps.start_shape_label, ' → ', sps.end_shape_label) as description,
    NULL as session_id
FROM shortest_path_sessions sps
ORDER BY activity_time DESC
LIMIT 100;
