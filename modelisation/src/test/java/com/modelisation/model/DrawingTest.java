package com.modelisation.model;

import com.modelisation.model.shapes.*;
import com.modelisation.model.logging.ConsoleLogger;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Drawing
 */
public class DrawingTest {
    
    private Drawing drawing;
    
    @BeforeEach
    public void setUp() {
        drawing = new Drawing("Test Drawing", "Description de test");
        drawing.setLogger(new ConsoleLogger());
    }
    
    @Test
    public void testAddShape() {
        assertEquals(0, drawing.getShapeCount());
        
        Rectangle rect = new Rectangle(10, 10, 50, 30, Color.RED, 2.0);
        drawing.addShape(rect);
        
        assertEquals(1, drawing.getShapeCount());
        assertTrue(drawing.getShapes().contains(rect));
    }
    
    @Test
    public void testRemoveShape() {
        Rectangle rect = new Rectangle(10, 10, 50, 30, Color.RED, 2.0);
        Circle circle = new Circle(100, 100, 25, Color.BLUE, 1.0);
        
        drawing.addShape(rect);
        drawing.addShape(circle);
        assertEquals(2, drawing.getShapeCount());
        
        boolean removed = drawing.removeShape(rect);
        assertTrue(removed);
        assertEquals(1, drawing.getShapeCount());
        assertFalse(drawing.getShapes().contains(rect));
        assertTrue(drawing.getShapes().contains(circle));
    }
    
    @Test
    public void testRemoveShapeByIndex() {
        Rectangle rect = new Rectangle(10, 10, 50, 30, Color.RED, 2.0);
        Circle circle = new Circle(100, 100, 25, Color.BLUE, 1.0);
        
        drawing.addShape(rect);
        drawing.addShape(circle);
        
        boolean removed = drawing.removeShape(0);
        assertTrue(removed);
        assertEquals(1, drawing.getShapeCount());
        
        // Vérifier que c'est bien le premier élément qui a été supprimé
        assertEquals(circle, drawing.getShapes().get(0));
    }
    
    @Test
    public void testClear() {
        drawing.addShape(new Rectangle(0, 0, 10, 10, Color.BLACK, 1.0));
        drawing.addShape(new Circle(0, 0, 5, Color.BLACK, 1.0));
        drawing.addShape(new Line(0, 0, 10, 10, Color.BLACK, 1.0));
        
        assertEquals(3, drawing.getShapeCount());
        
        drawing.clear();
        assertEquals(0, drawing.getShapeCount());
        assertTrue(drawing.getShapes().isEmpty());
    }
    
    @Test
    public void testFindShapeAt() {
        Rectangle rect = new Rectangle(10, 10, 50, 30, Color.RED, 2.0);
        Circle circle = new Circle(100, 100, 25, Color.BLUE, 1.0);
        
        drawing.addShape(rect);
        drawing.addShape(circle);
        
        // Test de recherche dans le rectangle
        Shape found = drawing.findShapeAt(30, 20);
        assertEquals(rect, found);
        
        // Test de recherche dans le cercle
        found = drawing.findShapeAt(100, 100);
        assertEquals(circle, found);
        
        // Test de recherche dans une zone vide
        found = drawing.findShapeAt(200, 200);
        assertNull(found);
    }
    
    @Test
    public void testCalculations() {
        Rectangle rect = new Rectangle(0, 0, 10, 5, Color.BLACK, 1.0); // Aire: 50
        Circle circle = new Circle(0, 0, 5, Color.BLACK, 1.0); // Aire: π*25
        Line line = new Line(0, 0, 10, 0, Color.BLACK, 1.0); // Aire: 0
        
        drawing.addShape(rect);
        drawing.addShape(circle);
        drawing.addShape(line);
        
        double expectedArea = 50 + Math.PI * 25;
        assertEquals(expectedArea, drawing.getTotalArea(), 0.001);
        
        double expectedPerimeter = 30 + Math.PI * 10 + 10;
        assertEquals(expectedPerimeter, drawing.getTotalPerimeter(), 0.001);
    }
    
    @Test
    public void testDrawingProperties() {
        assertEquals("Test Drawing", drawing.getName());
        assertEquals("Description de test", drawing.getDescription());
        
        drawing.setName("Nouveau nom");
        assertEquals("Nouveau nom", drawing.getName());
        
        drawing.setDescription("Nouvelle description");
        assertEquals("Nouvelle description", drawing.getDescription());
    }
}
