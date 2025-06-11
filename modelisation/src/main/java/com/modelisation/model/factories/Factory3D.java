package com.modelisation.model.factories;

import com.modelisation.model.shapes.Circle3D; // Placeholder for actual 3D circle
import com.modelisation.model.shapes.Rectangle3D; // Placeholder for actual 3D rectangle
import com.modelisation.model.shapes.Shape;
import javafx.scene.paint.Color;

/**
 * Concrete factory for creating 3D shapes (represented as 2D projections).
 */
public class Factory3D implements AbstractShapeFactory {

    @Override
    public Shape createCircle(double x, double y, double radius, Color color, double strokeWidth) {
        // For now, we'll create a Circle3D which will have its own drawing logic.
        // Parameters might need adjustment for 3D (e.g., depth, perspective)
        return new Circle3D(x, y, radius, color, strokeWidth); 
    }

    @Override
    public Shape createRectangle(double x, double y, double width, double height, Color color, double strokeWidth) {
        // Similar to Circle3D, Rectangle3D will handle its 3D-like rendering.
        return new Rectangle3D(x, y, width, height, color, strokeWidth);
    }
}
