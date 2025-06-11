package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract interface for Circle shapes.
 */
public interface AbstractCircle {
    void draw(GraphicsContext gc);
    // Add any circle-specific methods if needed, e.g.:
    // double getRadius();
    // void setRadius(double radius);
}
