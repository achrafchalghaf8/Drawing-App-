package com.modelisation.model.factories;

import com.modelisation.model.shapes.Shape;
import javafx.scene.paint.Color;

/**
 * Abstract Factory interface for creating shapes.
 */
public interface AbstractShapeFactory {
    Shape createCircle(double x, double y, double radius, Color color, double strokeWidth);
    Shape createRectangle(double x, double y, double width, double height, Color color, double strokeWidth);
}
