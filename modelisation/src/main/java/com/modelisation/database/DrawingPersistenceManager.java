package com.modelisation.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.modelisation.model.Drawing;
import com.modelisation.model.shapes.Shape;
import com.modelisation.model.shapes.Circle;
import com.modelisation.model.shapes.Rectangle;
import com.modelisation.model.shapes.Line;
import com.modelisation.model.logging.LoggingStrategy;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de persistance des dessins dans la base de données
 */
public class DrawingPersistenceManager {
    
    private final DatabaseManager databaseManager;
    private final ObjectMapper objectMapper;
    private LoggingStrategy logger;
    
    public DrawingPersistenceManager() {
        this.databaseManager = DatabaseManager.getInstance();
        this.objectMapper = new ObjectMapper();
    }
    
    public void setLogger(LoggingStrategy logger) {
        this.logger = logger;
    }
    
    /**
     * Sauvegarde un dessin dans la base de données
     */
    public int saveDrawing(Drawing drawing, String name, String description) {
        try {
            // Convertir le dessin en JSON
            String jsonData = convertDrawingToJson(drawing);
            
            // Sauvegarder dans la base de données
            int drawingId = databaseManager.saveDrawing(name, description, jsonData, drawing.getShapeCount());
            
            if (drawingId > 0) {
                // Sauvegarder aussi les formes individuelles pour l'analyse
                saveShapesDetails(drawingId, drawing);
                
                if (logger != null) {
                    logger.log(LoggingStrategy.LogLevel.INFO, 
                        String.format("Dessin '%s' sauvegardé avec succès (ID: %d, %d formes)", 
                            name, drawingId, drawing.getShapeCount()));
                }
            }
            
            return drawingId;
            
        } catch (Exception e) {
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.ERROR, 
                    "Erreur lors de la sauvegarde du dessin: " + name, e);
            }
            return -1;
        }
    }
    
    /**
     * Convertit un dessin en JSON
     */
    public String convertDrawingToJson(Drawing drawing) throws Exception {
        ObjectNode rootNode = objectMapper.createObjectNode();
        
        // Métadonnées du dessin
        rootNode.put("name", drawing.getName());
        rootNode.put("description", drawing.getDescription());
        rootNode.put("shapeCount", drawing.getShapeCount());
        rootNode.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Array des formes
        ArrayNode shapesArray = objectMapper.createArrayNode();
        
        int circleCounter = 0;
        int lineCounter = 1;
        int rectangleCounter = 0;
        
        for (Shape shape : drawing.getShapes()) {
            ObjectNode shapeNode = objectMapper.createObjectNode();
            
            // Propriétés communes
            shapeNode.put("id", shape.getId());
            shapeNode.put("type", shape.getClass().getSimpleName());
            shapeNode.put("x", shape.getX());
            shapeNode.put("y", shape.getY());
            shapeNode.put("color", shape.getColor().toString());
            shapeNode.put("strokeWidth", shape.getStrokeWidth());
            
            // Label personnalisé selon le type
            String label;
            if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                shapeNode.put("radius", circle.getRadius());
                label = String.valueOf((char)('A' + circleCounter));
                circleCounter++;
            } else if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                shapeNode.put("width", rect.getWidth());
                shapeNode.put("height", rect.getHeight());
                label = String.valueOf((char)('A' + rectangleCounter));
                rectangleCounter++;
            } else if (shape instanceof Line) {
                Line line = (Line) shape;
                shapeNode.put("endX", line.getEndX());
                shapeNode.put("endY", line.getEndY());
                label = String.valueOf(lineCounter);
                lineCounter++;
            } else {
                label = shape.getClass().getSimpleName();
            }
            
            shapeNode.put("label", label);
            shapesArray.add(shapeNode);
        }
        
        rootNode.set("shapes", shapesArray);
        
        return objectMapper.writeValueAsString(rootNode);
    }
    
    /**
     * Sauvegarde les détails des formes individuelles
     */
    private void saveShapesDetails(int drawingId, Drawing drawing) {
        String sql = "INSERT INTO shapes (drawing_id, shape_type, shape_label, position_x, position_y, param1, param2, color, stroke_width) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            
            int circleCounter = 0;
            int lineCounter = 1;
            int rectangleCounter = 0;
            
            for (Shape shape : drawing.getShapes()) {
                stmt.setInt(1, drawingId);
                stmt.setString(2, shape.getClass().getSimpleName().toUpperCase());
                stmt.setDouble(4, shape.getX());
                stmt.setDouble(5, shape.getY());
                stmt.setString(8, shape.getColor().toString());
                stmt.setDouble(9, shape.getStrokeWidth());
                
                // Paramètres spécifiques selon le type et label
                String label;
                if (shape instanceof Circle) {
                    Circle circle = (Circle) shape;
                    label = String.valueOf((char)('A' + circleCounter));
                    stmt.setDouble(6, circle.getRadius()); // param1 = radius
                    stmt.setNull(7, Types.DOUBLE); // param2 = null
                    circleCounter++;
                } else if (shape instanceof Rectangle) {
                    Rectangle rect = (Rectangle) shape;
                    label = String.valueOf((char)('A' + rectangleCounter));
                    stmt.setDouble(6, rect.getWidth()); // param1 = width
                    stmt.setDouble(7, rect.getHeight()); // param2 = height
                    rectangleCounter++;
                } else if (shape instanceof Line) {
                    Line line = (Line) shape;
                    label = String.valueOf(lineCounter);
                    stmt.setDouble(6, line.getEndX()); // param1 = endX
                    stmt.setDouble(7, line.getEndY()); // param2 = endY
                    lineCounter++;
                } else {
                    label = shape.getClass().getSimpleName();
                    stmt.setNull(6, Types.DOUBLE);
                    stmt.setNull(7, Types.DOUBLE);
                }
                
                stmt.setString(3, label);
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            
        } catch (SQLException e) {
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.ERROR, 
                    "Erreur lors de la sauvegarde des détails des formes", e);
            }
        }
    }
    
    /**
     * Charge la liste des dessins sauvegardés
     */
    public List<DrawingInfo> getDrawingsList() {
        List<DrawingInfo> drawings = new ArrayList<>();
        String sql = "SELECT id, name, description, shape_count, created_at, updated_at FROM drawings ORDER BY updated_at DESC";
        
        try (Statement stmt = databaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                DrawingInfo info = new DrawingInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("shape_count"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
                );
                drawings.add(info);
            }
            
        } catch (SQLException e) {
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.ERROR, 
                    "Erreur lors du chargement de la liste des dessins", e);
            }
        }
        
        return drawings;
    }
    
    /**
     * Charge un dessin depuis la base de données
     */
    public String getDrawingJson(int drawingId) {
        String sql = "SELECT json_data FROM drawings WHERE id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, drawingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("json_data");
                }
            }
            
        } catch (SQLException e) {
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.ERROR, 
                    "Erreur lors du chargement du dessin ID: " + drawingId, e);
            }
        }
        
        return null;
    }
    
    /**
     * Classe pour les informations de dessin
     */
    public static class DrawingInfo {
        public final int id;
        public final String name;
        public final String description;
        public final int shapeCount;
        public final Timestamp createdAt;
        public final Timestamp updatedAt;
        
        public DrawingInfo(int id, String name, String description, int shapeCount, 
                          Timestamp createdAt, Timestamp updatedAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.shapeCount = shapeCount;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%d formes) - %s", name, shapeCount, 
                createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }
}
