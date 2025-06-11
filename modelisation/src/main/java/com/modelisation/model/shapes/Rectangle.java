package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant un rectangle
 * Implémentation concrète de Shape
 */
public class Rectangle extends Shape implements AbstractRectangle {
    private double width;
    private double height;

    public Rectangle(double x, double y, double width, double height, Color color, double strokeWidth) {
        super(x, y, color, strokeWidth);
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(strokeWidth);
        gc.strokeRect(x, y, width, height);
    }

    @Override
    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width && 
               pointY >= y && pointY <= y + height;
    }

    @Override
    public double getArea() {
        return width * height;
    }

    @Override
    public double getPerimeter() {
        return 2 * (width + height);
    }

    // Getters et Setters
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    @Override
    public String toString() {
        return String.format("Rectangle[x=%.2f, y=%.2f, width=%.2f, height=%.2f, color=%s]", 
                           x, y, width, height, color);
    }
}
