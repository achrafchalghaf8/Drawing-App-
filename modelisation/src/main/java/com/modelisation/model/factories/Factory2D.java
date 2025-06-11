package com.modelisation.model.factories;

import com.modelisation.model.shapes.Circle;
import com.modelisation.model.shapes.Rectangle;
import com.modelisation.model.shapes.Shape;
import javafx.scene.paint.Color;

/**
 * Concrete factory for creating 2D shapes.
 */
public class Factory2D implements AbstractShapeFactory {

    @Override
    public Shape createCircle(double x, double y, double radius, Color color, double strokeWidth) {
        return new Circle(x, y, radius, color, strokeWidth);
    }

    @Override
    public Shape createRectangle(double x, double y, double width, double height, Color color, double strokeWidth) {
        return new Rectangle(x, y, width, height, color, strokeWidth);
    }
}
