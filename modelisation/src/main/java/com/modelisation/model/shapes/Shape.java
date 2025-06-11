package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe abstraite représentant une forme géométrique
 * Utilise le pattern Template Method pour définir la structure commune
 */
public abstract class Shape {
    protected double x, y;
    protected Color color;
    protected double strokeWidth;
    protected String id;

    public Shape(double x, double y, Color color, double strokeWidth) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.id = generateId();
    }

    /**
     * Méthode abstraite pour dessiner la forme
     * @param gc Contexte graphique pour le dessin
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Méthode abstraite pour vérifier si un point est dans la forme
     * @param pointX coordonnée X du point
     * @param pointY coordonnée Y du point
     * @return true si le point est dans la forme
     */
    public abstract boolean contains(double pointX, double pointY);

    /**
     * Méthode abstraite pour obtenir l'aire de la forme
     * @return l'aire de la forme
     */
    public abstract double getArea();

    /**
     * Méthode abstraite pour obtenir le périmètre de la forme
     * @return le périmètre de la forme
     */
    public abstract double getPerimeter();

    /**
     * Génère un ID unique pour la forme
     * @return ID unique
     */
    private String generateId() {
        return getClass().getSimpleName() + "_" + System.currentTimeMillis() + "_" + hashCode();
    }

    // Getters et Setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public double getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(double strokeWidth) { this.strokeWidth = strokeWidth; }

    public String getId() { return id; }

    @Override
    public String toString() {
        return String.format("%s[x=%.2f, y=%.2f, color=%s]", 
                           getClass().getSimpleName(), x, y, color);
    }
}
