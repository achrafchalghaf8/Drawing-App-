package com.modelisation.model;

import com.modelisation.model.shapes.Shape;
import com.modelisation.model.logging.LoggingStrategy;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Classe représentant un dessin contenant une collection de formes
 * Utilise le pattern Observer pour notifier les changements
 */
@SuppressWarnings("deprecation") // Observable est deprecated mais toujours fonctionnel
public class Drawing extends Observable {
    private List<Shape> shapes;
    private String name;
    private String description;
    @JsonIgnore
    private LoggingStrategy logger;
    
    public Drawing() {
        this.shapes = new ArrayList<>();
        this.name = "Nouveau dessin";
        this.description = "";
    }
    
    public Drawing(String name, String description) {
        this.shapes = new ArrayList<>();
        this.name = name;
        this.description = description;
    }
    
    /**
     * Ajoute une forme au dessin
     * @param shape Forme à ajouter
     */
    public void addShape(Shape shape) {
        shapes.add(shape);
        setChanged();
        notifyObservers("SHAPE_ADDED");
        
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, 
                      "Forme ajoutée: " + shape.toString());
        }
    }
    
    /**
     * Supprime une forme du dessin
     * @param shape Forme à supprimer
     * @return true si la forme a été supprimée
     */
    public boolean removeShape(Shape shape) {
        boolean removed = shapes.remove(shape);
        if (removed) {
            setChanged();
            notifyObservers("SHAPE_REMOVED");
            
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.INFO, 
                          "Forme supprimée: " + shape.toString());
            }
        }
        return removed;
    }
    
    /**
     * Supprime une forme par son index
     * @param index Index de la forme à supprimer
     * @return true si la forme a été supprimée
     */
    public boolean removeShape(int index) {
        if (index >= 0 && index < shapes.size()) {
            Shape removedShape = shapes.remove(index);
            setChanged();
            notifyObservers("SHAPE_REMOVED");
            
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.INFO, 
                          "Forme supprimée à l'index " + index + ": " + removedShape.toString());
            }
            return true;
        }
        return false;
    }
    
    /**
     * Efface toutes les formes du dessin
     */
    public void clear() {
        int shapeCount = shapes.size();
        shapes.clear();
        setChanged();
        notifyObservers("DRAWING_CLEARED");
        
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, 
                      "Dessin effacé - " + shapeCount + " formes supprimées");
        }
    }
    
    /**
     * Trouve une forme à une position donnée
     * @param x Coordonnée X
     * @param y Coordonnée Y
     * @return La forme trouvée ou null
     */
    public Shape findShapeAt(double x, double y) {
        // Parcourir les formes en ordre inverse pour sélectionner celle du dessus
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape.contains(x, y)) {
                return shape;
            }
        }
        return null;
    }
    
    /**
     * Calcule l'aire totale de toutes les formes
     * @return Aire totale
     */
    public double getTotalArea() {
        return shapes.stream()
                    .mapToDouble(Shape::getArea)
                    .sum();
    }
    
    /**
     * Calcule le périmètre total de toutes les formes
     * @return Périmètre total
     */
    public double getTotalPerimeter() {
        return shapes.stream()
                    .mapToDouble(Shape::getPerimeter)
                    .sum();
    }
    
    /**
     * Obtient le nombre de formes dans le dessin
     * @return Nombre de formes
     */
    public int getShapeCount() {
        return shapes.size();
    }
    
    // Getters et Setters
    public List<Shape> getShapes() {
        return new ArrayList<>(shapes); // Retourner une copie pour éviter les modifications externes
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        setChanged();
        notifyObservers("NAME_CHANGED");
        
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, 
                      "Nom du dessin changé: " + name);
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        setChanged();
        notifyObservers("DESCRIPTION_CHANGED");
    }
    
    public void setLogger(LoggingStrategy logger) {
        this.logger = logger;
    }
    
    public LoggingStrategy getLogger() {
        return logger;
    }
    
    @Override
    public String toString() {
        return String.format("Drawing[name='%s', shapes=%d, area=%.2f]", 
                           name, shapes.size(), getTotalArea());
    }
}
