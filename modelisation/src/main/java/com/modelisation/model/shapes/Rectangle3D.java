package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Rectangle3D extends Shape implements AbstractRectangle {
    private double width;
    private double height;
    private double depthFactor = 0.4; // Determines how "deep" it looks

    public Rectangle3D(double x, double y, double width, double height, Color color, double strokeWidth) {
        super(x, y, color, strokeWidth); // x, y are top-left for Rectangle3D front face
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(getColor());
        gc.setLineWidth(getStrokeWidth());
        gc.setFill(getColor().deriveColor(1, 1, 1, 0.3)); // Semi-transparent fill

        double x = getX();
        double y = getY();

        // Front face
        gc.strokeRect(x, y, width, height);
        gc.fillRect(x, y, width, height);

        // Perspective lines for depth
        double offsetX = width * depthFactor * 0.707; // cos(45 deg)
        double offsetY = height * depthFactor * 0.707; // sin(45 deg) but using height for a consistent look

        // Top-right back point
        double xTR_back = x + width + offsetX;
        double yTR_back = y - offsetY;
        // Bottom-right back point
        double xBR_back = x + width + offsetX;
        double yBR_back = y + height - offsetY;
        // Top-left back point (only needed if drawing back face explicitly)
        // double xTL_back = x + offsetX;
        // double yTL_back = y - offsetY;

        // Draw side and top faces (simplified)
        // Top face
        gc.strokePolygon(new double[]{x, x + width, xTR_back, x + offsetX},
                         new double[]{y, y, yTR_back, y - offsetY}, 4);
        gc.fillPolygon(new double[]{x, x + width, xTR_back, x + offsetX},
                       new double[]{y, y, yTR_back, y - offsetY}, 4);

        // Right face
        gc.strokePolygon(new double[]{x + width, x + width, xBR_back, xTR_back},
                         new double[]{y, y + height, yBR_back, yTR_back}, 4);
        gc.fillPolygon(new double[]{x + width, x + width, xBR_back, xTR_back},
                       new double[]{y, y + height, yBR_back, yTR_back}, 4);
    }

    @Override
    public boolean contains(double pointX, double pointY) {
        // Simplified: check if within the front face for now
        return pointX >= getX() && pointX <= getX() + width &&
               pointY >= getY() && pointY <= getY() + height;
    }

    @Override
    public double getArea() {
        // Surface area of a cuboid: 2*(lw + lh + wh)
        // For simplicity, returning area of the front face, as 'depth' is visual
        return width * height; 
    }

    @Override
    public double getPerimeter() {
        // Perimeter of the front face
        return 2 * (width + height);
    }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}
