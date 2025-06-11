package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract interface for Rectangle shapes.
 */
public interface AbstractRectangle {
    void draw(GraphicsContext gc);
    // Add any rectangle-specific methods if needed, e.g.:
    // double getWidth();
    // void setWidth(double width);
    // double getHeight();
    // void setHeight(double height);
}
