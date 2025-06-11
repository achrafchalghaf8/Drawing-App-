package com.modelisation.model.shapes;

import javafx.scene.paint.Color;

/**
 * Factory Pattern pour créer différents types de formes géométriques
 * Centralise la création des objets Shape
 */
public class ShapeFactory {
    
    /**
     * Énumération des types de formes supportées
     */
    public enum ShapeType {
        RECTANGLE,
        CIRCLE,
        LINE
    }

    /**
     * Crée une forme selon le type spécifié
     * @param type Type de forme à créer
     * @param params Paramètres pour la création de la forme
     * @return Instance de la forme créée
     */
    public static Shape createShape(ShapeType type, ShapeParameters params) {
        switch (type) {
            case RECTANGLE:
                return new Rectangle(
                    params.getX(), 
                    params.getY(), 
                    params.getWidth(), 
                    params.getHeight(), 
                    params.getColor(), 
                    params.getStrokeWidth()
                );
            
            case CIRCLE:
                return new Circle(
                    params.getX(), 
                    params.getY(), 
                    params.getRadius(), 
                    params.getColor(), 
                    params.getStrokeWidth()
                );
            
            case LINE:
                return new Line(
                    params.getX(), 
                    params.getY(), 
                    params.getEndX(), 
                    params.getEndY(), 
                    params.getColor(), 
                    params.getStrokeWidth()
                );
            
            default:
                throw new IllegalArgumentException("Type de forme non supporté: " + type);
        }
    }

    /**
     * Classe pour encapsuler les paramètres de création d'une forme
     * Utilise le pattern Builder pour une construction flexible
     */
    public static class ShapeParameters {
        private double x, y;
        private double width, height;
        private double radius;
        private double endX, endY;
        private Color color = Color.BLACK;
        private double strokeWidth = 1.0;

        public ShapeParameters(double x, double y) {
            this.x = x;
            this.y = y;
        }

        // Builder methods
        public ShapeParameters width(double width) {
            this.width = width;
            return this;
        }

        public ShapeParameters height(double height) {
            this.height = height;
            return this;
        }

        public ShapeParameters radius(double radius) {
            this.radius = radius;
            return this;
        }

        public ShapeParameters endPoint(double endX, double endY) {
            this.endX = endX;
            this.endY = endY;
            return this;
        }

        public ShapeParameters color(Color color) {
            this.color = color;
            return this;
        }

        public ShapeParameters strokeWidth(double strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }

        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public double getRadius() { return radius; }
        public double getEndX() { return endX; }
        public double getEndY() { return endY; }
        public Color getColor() { return color; }
        public double getStrokeWidth() { return strokeWidth; }
    }
}
