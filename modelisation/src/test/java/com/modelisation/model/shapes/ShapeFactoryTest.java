package com.modelisation.model.shapes;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la ShapeFactory
 */
public class ShapeFactoryTest {
    
    @Test
    public void testCreateRectangle() {
        ShapeFactory.ShapeParameters params = new ShapeFactory.ShapeParameters(10, 20)
            .width(100)
            .height(50)
            .color(Color.RED)
            .strokeWidth(2.0);
        
        Shape shape = ShapeFactory.createShape(ShapeFactory.ShapeType.RECTANGLE, params);
        
        assertNotNull(shape);
        assertTrue(shape instanceof Rectangle);
        assertEquals(10, shape.getX());
        assertEquals(20, shape.getY());
        assertEquals(Color.RED, shape.getColor());
        assertEquals(2.0, shape.getStrokeWidth());
        
        Rectangle rectangle = (Rectangle) shape;
        assertEquals(100, rectangle.getWidth());
        assertEquals(50, rectangle.getHeight());
    }
    
    @Test
    public void testCreateCircle() {
        ShapeFactory.ShapeParameters params = new ShapeFactory.ShapeParameters(50, 60)
            .radius(25)
            .color(Color.BLUE)
            .strokeWidth(1.5);
        
        Shape shape = ShapeFactory.createShape(ShapeFactory.ShapeType.CIRCLE, params);
        
        assertNotNull(shape);
        assertTrue(shape instanceof Circle);
        assertEquals(50, shape.getX());
        assertEquals(60, shape.getY());
        assertEquals(Color.BLUE, shape.getColor());
        assertEquals(1.5, shape.getStrokeWidth());
        
        Circle circle = (Circle) shape;
        assertEquals(25, circle.getRadius());
    }
    
    @Test
    public void testCreateLine() {
        ShapeFactory.ShapeParameters params = new ShapeFactory.ShapeParameters(0, 0)
            .endPoint(100, 100)
            .color(Color.GREEN)
            .strokeWidth(3.0);
        
        Shape shape = ShapeFactory.createShape(ShapeFactory.ShapeType.LINE, params);
        
        assertNotNull(shape);
        assertTrue(shape instanceof Line);
        assertEquals(0, shape.getX());
        assertEquals(0, shape.getY());
        assertEquals(Color.GREEN, shape.getColor());
        assertEquals(3.0, shape.getStrokeWidth());
        
        Line line = (Line) shape;
        assertEquals(100, line.getEndX());
        assertEquals(100, line.getEndY());
    }
    
    @Test
    public void testShapeCalculations() {
        // Test Rectangle
        Rectangle rect = new Rectangle(0, 0, 10, 5, Color.BLACK, 1.0);
        assertEquals(50, rect.getArea());
        assertEquals(30, rect.getPerimeter());
        assertTrue(rect.contains(5, 2));
        assertFalse(rect.contains(15, 2));
        
        // Test Circle
        Circle circle = new Circle(0, 0, 5, Color.BLACK, 1.0);
        assertEquals(Math.PI * 25, circle.getArea(), 0.001);
        assertEquals(Math.PI * 10, circle.getPerimeter(), 0.001);
        assertTrue(circle.contains(3, 0));
        assertFalse(circle.contains(6, 0));
        
        // Test Line
        Line line = new Line(0, 0, 10, 0, Color.BLACK, 1.0);
        assertEquals(0, line.getArea());
        assertEquals(10, line.getPerimeter());
        assertEquals(10, line.getLength());
    }
}
