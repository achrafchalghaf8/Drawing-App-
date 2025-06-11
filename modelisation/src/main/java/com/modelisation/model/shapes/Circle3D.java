package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Circle3D extends Shape implements AbstractCircle {
    private double radius;

    public Circle3D(double x, double y, double radius, Color color, double strokeWidth) {
        super(x, y, color, strokeWidth); // x, y are center for Circle3D
        this.radius = radius;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(getColor());
        gc.setLineWidth(getStrokeWidth());
        gc.setFill(getColor().deriveColor(1, 1, 1, 0.3)); // Semi-transparent fill

        // Draw an ellipse to represent a 3D circle (e.g., a disc viewed from an angle)
        // Main ellipse (face of the cylinder/sphere)
        gc.strokeOval(getX() - radius, getY() - radius / 2, 2 * radius, radius);
        gc.fillOval(getX() - radius, getY() - radius / 2, 2 * radius, radius);

        // Suggesting depth (optional, simple representation)
        // You could draw another ellipse slightly offset or lines to suggest a cylinder
        // For simplicity, we'll keep it as a single ellipse for now.
    }

    @Override
    public boolean contains(double pointX, double pointY) {
        // For an ellipse: ((x-h)^2 / a^2) + ((y-k)^2 / b^2) <= 1
        // Here, h = getX(), k = getY(), a = radius, b = radius / 2
        double dx = pointX - getX();
        double dy = pointY - getY();
        return (dx * dx) / (radius * radius) + (dy * dy) / ((radius / 2) * (radius / 2)) <= 1;
    }

    @Override
    public double getArea() {
        return Math.PI * radius * (radius / 2); // Area of the projected ellipse
    }

    @Override
    public double getPerimeter() {
        // Approximate perimeter of an ellipse (Ramanujan's approximation)
        double a = radius;
        double b = radius / 2;
        double h = Math.pow(a - b, 2) / Math.pow(a + b, 2);
        return Math.PI * (a + b) * (1 + (3 * h) / (10 + Math.sqrt(4 - 3 * h)));
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
