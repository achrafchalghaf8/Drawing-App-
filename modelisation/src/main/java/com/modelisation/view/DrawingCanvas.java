package com.modelisation.view;

import com.modelisation.model.Drawing;
import com.modelisation.model.shapes.Shape;
import com.modelisation.model.shapes.ShapeFactory;
import com.modelisation.model.logging.LoggingStrategy;
import com.modelisation.controller.DrawingController;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import com.modelisation.model.graph.Graph;
import com.modelisation.model.graph.Node;
import com.modelisation.model.graph.Edge;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Canvas personnalisé pour le dessin des formes géométriques
 * Implémente Observer pour réagir aux changements du modèle Drawing
 */
@SuppressWarnings("deprecation") // Observer est deprecated mais toujours fonctionnel
public class DrawingCanvas extends Canvas implements Observer {
    
    private Drawing drawing;
    private ShapeFactory.ShapeType currentShapeType;
    private Color currentColor;
    private double currentStrokeWidth;
    private LoggingStrategy logger;
    private DrawingController drawingController; // Reference to the controller
    
    // Variables pour le dessin en cours
    private double startX, startY;
    private String interactionMode;
    private DimensionType currentDimensionType;

    private boolean isDrawing = false;
    private Shape previewShape;
    
    public DrawingCanvas(double width, double height) {
        super(width, height);
        
        // Valeurs par défaut
        this.currentShapeType = ShapeFactory.ShapeType.RECTANGLE;
        this.currentDimensionType = DimensionType.D2; // Default to 2D
        this.currentColor = Color.BLACK;
        this.currentStrokeWidth = 2.0;
        
        // Initialiser le dessin
        this.drawing = new Drawing();
        this.drawing.addObserver(this);
        
        // Configurer les événements de souris
        setupMouseEvents();
        
        // Rendre le canvas focusable pour recevoir les événements clavier
        setFocusTraversable(true);
        this.interactionMode = "NORMAL"; // Default interaction mode
    }
    
    /**
     * Configure les gestionnaires d'événements de souris
     */
    private void setupMouseEvents() {
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseReleased(this::handleMouseReleased);
        setOnMouseClicked(this::handleMouseClicked);
    }
    
    /**
     * Gère l'événement de pression de la souris
     */
    private void handleMousePressed(MouseEvent event) {
        // Ne pas commencer le dessin en mode PATH_SELECTION
        if ("PATH_SELECTION".equals(interactionMode)) {
            return;
        }

        startX = event.getX();
        startY = event.getY();
        isDrawing = true;

        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.DEBUG,
                      String.format("Début du dessin à (%.2f, %.2f)", startX, startY));
        }

        requestFocus(); // Pour recevoir les événements clavier
    }
    
    /**
     * Gère l'événement de glissement de la souris (preview du dessin)
     */
    private void handleMouseDragged(MouseEvent event) {
        // Ne pas dessiner en mode PATH_SELECTION
        if ("PATH_SELECTION".equals(interactionMode) || !isDrawing) return;

        double currentX = event.getX();
        double currentY = event.getY();

        // Créer une forme de prévisualisation
        createPreviewShape(startX, startY, currentX, currentY);

        // Redessiner le canvas avec la prévisualisation
        redraw();
    }
    
    /**
     * Gère l'événement de relâchement de la souris
     */
    private void handleMouseReleased(MouseEvent event) {
        // Ne pas créer de forme en mode PATH_SELECTION
        if ("PATH_SELECTION".equals(interactionMode) || !isDrawing) return;
        
        double endX = event.getX();
        double endY = event.getY();

        if (drawingController != null && (currentShapeType == ShapeFactory.ShapeType.CIRCLE || currentShapeType == ShapeFactory.ShapeType.RECTANGLE)) {
            double param1, param2 = 0;
            double shapeX, shapeY;

            if (currentShapeType == ShapeFactory.ShapeType.RECTANGLE) {
                shapeX = Math.min(startX, endX);
                shapeY = Math.min(startY, endY);
                param1 = Math.abs(startX - endX); // width
                param2 = Math.abs(startY - endY); // height
                drawingController.addShape(currentShapeType, currentDimensionType, shapeX, shapeY, param1, param2, currentColor, currentStrokeWidth);
            } else if (currentShapeType == ShapeFactory.ShapeType.CIRCLE) {
                shapeX = startX; // center X
                shapeY = startY; // center Y
                param1 = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)); // radius
                drawingController.addShape(currentShapeType, currentDimensionType, shapeX, shapeY, param1, param2, currentColor, currentStrokeWidth);
            }
        } else if (currentShapeType == ShapeFactory.ShapeType.LINE) {
            // Handle LINE shape creation locally as before, if Abstract Factory doesn't support it
            Shape finalShape = createShape(startX, startY, endX, endY);
            if (finalShape != null) {
                drawing.addShape(finalShape);
                if (logger != null) {
                    logger.log(LoggingStrategy.LogLevel.INFO, "Forme créée: " + finalShape.toString());
                }
            }
        } else {
             if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.WARNING, "Shape creation skipped for type: " + currentShapeType);
            }
        }
        
        // Réinitialiser l'état de dessin
        isDrawing = false;
        previewShape = null;
        redraw();
    }
    
    /**
     * Gère l'événement de clic de souris (pour sélection)
     */
    private void handleMouseClicked(MouseEvent event) {
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.DEBUG,
                       String.format("Clic de souris à (%.2f, %.2f) - Mode: %s, Click Count: %d", 
                                     event.getX(), event.getY(), interactionMode, event.getClickCount()));
        }

        if ("PATH_SELECTION".equals(interactionMode)) {
            if (drawingController != null) {
                // In path selection mode, any click (single or double) is for path selection
                drawingController.handleCanvasClickForShortestPath(event.getX(), event.getY());
            } else {
                if (logger != null) {
                    logger.log(LoggingStrategy.LogLevel.ERROR, "DrawingController not set in DrawingCanvas for PATH_SELECTION mode.");
                }
            }
        } else {
            // Normal interaction mode: existing double-click to delete logic
            if (event.getClickCount() == 2) { // Double-clic pour sélection/suppression
                Shape selectedShape = drawing.findShapeAt(event.getX(), event.getY());
                if (selectedShape != null) {
                    drawing.removeShape(selectedShape);
                    
                    if (logger != null) {
                        logger.log(LoggingStrategy.LogLevel.INFO, 
                                  "Forme supprimée par double-clic: " + selectedShape.toString());
                    }
                }
            }
            // Single-click logic for normal mode (e.g., selecting a shape) could be added here if needed.
        }
    }
    
    /**
     * Crée une forme de prévisualisation
     */
    private void createPreviewShape(double startX, double startY, double currentX, double currentY) {
        previewShape = createShape(startX, startY, currentX, currentY);
        if (previewShape != null) {
            // Utiliser une couleur plus claire pour la prévisualisation
            previewShape.setColor(currentColor.deriveColor(0, 1, 1, 0.5));
        }
    }
    
    /**
     * Crée une forme selon le type sélectionné
     */
    private Shape createShape(double startX, double startY, double endX, double endY) {
        try {
            ShapeFactory.ShapeParameters params = new ShapeFactory.ShapeParameters(startX, startY)
                .color(currentColor)
                .strokeWidth(currentStrokeWidth);
            
            switch (currentShapeType) {
                case RECTANGLE:
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    // Ajuster les coordonnées pour que le rectangle soit toujours dessiné correctement
                    double rectX = Math.min(startX, endX);
                    double rectY = Math.min(startY, endY);
                    params = new ShapeFactory.ShapeParameters(rectX, rectY)
                        .width(width)
                        .height(height)
                        .color(currentColor)
                        .strokeWidth(currentStrokeWidth);
                    break;
                    
                case CIRCLE:
                    double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                    params.radius(radius);
                    break;
                    
                case LINE:
                    params.endPoint(endX, endY);
                    break;
            }
            
            return ShapeFactory.createShape(currentShapeType, params);
            
        } catch (Exception e) {
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.ERROR, 
                          "Erreur lors de la création de la forme", e);
            }
            return null;
        }
    }
    
    /**
     * Redessine tout le canvas
     */
    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        
        // Effacer le canvas
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        // Dessiner le fond
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        // Dessiner toutes les formes du dessin avec leurs labels personnalisés
        List<Shape> shapes = drawing.getShapes();
        int circleCounter = 0;
        int lineCounter = 1;
        int rectangleCounter = 0;

        for (Shape shape : shapes) {
            shape.draw(gc);

            // Générer le label personnalisé selon le type de forme
            String customLabel;
            if (shape instanceof com.modelisation.model.shapes.Circle) {
                customLabel = String.valueOf((char)('A' + circleCounter));
                circleCounter++;
            } else if (shape instanceof com.modelisation.model.shapes.Line) {
                customLabel = String.valueOf(lineCounter);
                lineCounter++;
            } else if (shape instanceof com.modelisation.model.shapes.Rectangle) {
                customLabel = String.valueOf((char)('A' + rectangleCounter));
                rectangleCounter++;
            } else {
                customLabel = shape.getId();
            }

            // Afficher le label personnalisé de la forme
            drawShapeLabel(gc, shape, customLabel);
        }
        
        // Dessiner la forme de prévisualisation si elle existe
        if (previewShape != null) {
            previewShape.draw(gc);
        }
    }
    
    @Override
    public void update(Observable o, Object arg) {
        // Redessiner quand le modèle change
        redraw();
    }
    
    // Getters et Setters
    public Drawing getDrawing() {
        return drawing;
    }
    
    public void setDrawing(Drawing drawing) {
        if (this.drawing != null) {
            this.drawing.deleteObserver(this);
        }
        
        this.drawing = drawing;
        this.drawing.addObserver(this);
        redraw();
    }
    
    public ShapeFactory.ShapeType getCurrentShapeType() {
        return currentShapeType;
    }
    
    public void setCurrentShapeType(ShapeFactory.ShapeType currentShapeType) {
        this.currentShapeType = currentShapeType;
        
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, 
                      "Type de forme sélectionné: " + currentShapeType);
        }
    }
    
    public Color getCurrentColor() {
        return currentColor;
    }
    
    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }
    
    public double getCurrentStrokeWidth() {
        return currentStrokeWidth;
    }
    
    public void setCurrentStrokeWidth(double currentStrokeWidth) {
        this.currentStrokeWidth = currentStrokeWidth;
    }

    public void setCurrentDimensionType(DimensionType dimensionType) {
        this.currentDimensionType = dimensionType;
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, "Dimension type set to: " + dimensionType);
        }
    }
    
    public void setLogger(LoggingStrategy logger) {
        this.logger = logger;
        drawing.setLogger(logger);
    }

    public void setDrawingController(DrawingController controller) {
        this.drawingController = controller;
    }

    public void setInteractionMode(String mode) {
        this.interactionMode = mode;
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, "Interaction mode set to: " + mode);
        }
        // Potentially change cursor or other UI elements based on mode here
        // For example:
        // if ("PATH_SELECTION".equals(mode)) {
        //     setCursor(Cursor.CROSSHAIR);
        // } else {
        //     setCursor(Cursor.DEFAULT);
        // }
    }

    public String getInteractionMode() {
        return interactionMode;
    }

    /**
     * Dessine le label personnalisé d'une forme géométrique
     * @param gc Le contexte graphique
     * @param shape La forme dont on veut afficher le label
     * @param customLabel Le label personnalisé à afficher
     */
    private void drawShapeLabel(GraphicsContext gc, Shape shape, String customLabel) {
        if (shape == null) return;

        // Sauvegarder les paramètres actuels
        Color originalFill = (Color) gc.getFill();
        double originalLineWidth = gc.getLineWidth();

        // Configurer le style pour le texte
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1.0);

        // Calculer la position du label selon le type de forme
        double labelX, labelY;
        String labelText = customLabel;

        if (shape instanceof com.modelisation.model.shapes.Circle) {
            // Pour les cercles, centrer le label
            labelX = shape.getX() - 15;
            labelY = shape.getY() + 5;
        } else if (shape instanceof com.modelisation.model.shapes.Rectangle) {
            // Pour les rectangles, placer le label en haut à gauche
            labelX = shape.getX() + 5;
            labelY = shape.getY() + 15;
        } else if (shape instanceof com.modelisation.model.shapes.Line) {
            // Pour les lignes, placer le label au milieu
            com.modelisation.model.shapes.Line line = (com.modelisation.model.shapes.Line) shape;
            labelX = (shape.getX() + line.getEndX()) / 2;
            labelY = (shape.getY() + line.getEndY()) / 2 - 5;
        } else {
            // Position par défaut
            labelX = shape.getX();
            labelY = shape.getY() - 5;
        }

        // Dessiner un fond blanc semi-transparent pour le texte
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.8);
        double textWidth = labelText.length() * 6; // Estimation approximative
        gc.fillRect(labelX - 2, labelY - 12, textWidth + 4, 14);

        // Dessiner le texte
        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.BLACK);
        gc.fillText(labelText, labelX, labelY);

        // Restaurer les paramètres originaux
        gc.setFill(originalFill);
        gc.setLineWidth(originalLineWidth);
    }

    /**
     * Dessine un graphe sur le canvas.
     * Cette méthode superpose le graphe (uniquement les arêtes surlignées du chemin le plus court) 
     * sur le dessin existant.
     * @param graphToDraw Le graphe à dessiner.
     */
    public void drawGraph(Graph graphToDraw) {
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.DEBUG, "drawGraph called.");
        }

        if (graphToDraw == null) {
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.DEBUG, "DrawingCanvas.drawGraph: Graph is null, nothing to draw.");
            }
            return;
        }
        
        if (graphToDraw.getEdges().isEmpty()) {
            if (logger != null) {
                logger.log(LoggingStrategy.LogLevel.DEBUG, "DrawingCanvas.drawGraph: Graph has no edges, nothing to draw.");
            }
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();

        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, "DrawingCanvas.drawGraph: Processing graph with " + graphToDraw.getNodes().size() + " nodes and " + graphToDraw.getEdges().size() + " edges.");
            int highlightedEdgeCount = 0;
            for (Edge edge : graphToDraw.getEdges()) {
                if (edge.isHighlighted()) {
                    highlightedEdgeCount++;
                    logger.log(LoggingStrategy.LogLevel.INFO, "DrawingCanvas.drawGraph: Found HIGHLIGHTED Edge from '" + (edge.getSource() != null ? edge.getSource().getLabel() : "null") + "' to '" + (edge.getTarget() != null ? edge.getTarget().getLabel() : "null") + "'");
                } else {
                    // Optional: log non-highlighted edges for debugging if needed
                    // logger.log(LoggingStrategy.LogLevel.DEBUG, "DrawingCanvas.drawGraph: Non-highlighted Edge from '" + (edge.getSource() != null ? edge.getSource().getLabel() : "null") + "' to '" + (edge.getTarget() != null ? edge.getTarget().getLabel() : "null") + "'");
                }
            }
            logger.log(LoggingStrategy.LogLevel.INFO, "DrawingCanvas.drawGraph: Total highlighted edges to draw: " + highlightedEdgeCount);
        }

        // Dessiner d'abord toutes les arêtes en gris clair
        for (Edge edge : graphToDraw.getEdges()) {
            Node source = edge.getSource();
            Node target = edge.getTarget();

            if (source == null || target == null) {
                if (logger != null) {
                    logger.log(LoggingStrategy.LogLevel.WARNING, "DrawingCanvas.drawGraph: Skipping edge with null source or target.");
                }
                continue;
            }

            if (!edge.isHighlighted()) {
                gc.setStroke(Color.LIGHTGRAY); // Arêtes normales en gris clair
                gc.setLineWidth(1.0);
                gc.strokeLine(source.getX(), source.getY(), target.getX(), target.getY());
            }
        }

        // Puis dessiner les arêtes du chemin le plus court en ROUGE avec effet de brillance
        for (Edge edge : graphToDraw.getEdges()) {
            if (edge.isHighlighted()) {
                Node source = edge.getSource();
                Node target = edge.getTarget();

                if (source == null || target == null) {
                    if (logger != null) {
                        logger.log(LoggingStrategy.LogLevel.WARNING, "DrawingCanvas.drawGraph: Skipping highlighted edge with null source or target.");
                    }
                    continue;
                }

                // Dessiner le chemin avec effet de brillance (glow effect)
                drawHighlightedPath(gc, source, target);

                if (logger != null) {
                    logger.log(LoggingStrategy.LogLevel.INFO, "DrawingCanvas.drawGraph: Drew highlighted path from '" + source.getLabel() + "' to '" + target.getLabel() + "'.");
                }
            }
        }

        // Dessiner les nœuds par-dessus les arêtes
        for (Node node : graphToDraw.getNodes()) {
            if (node != null) {
                // Vérifier si le nœud fait partie du chemin mis en évidence
                boolean isOnPath = isNodeOnHighlightedPath(node, graphToDraw);

                if (node.isSelected()) {
                    // Nœuds sélectionnés avec effet spécial
                    drawSelectedNode(gc, node);
                } else if (isOnPath) {
                    // Nœuds sur le chemin mis en évidence
                    drawPathNode(gc, node);
                } else {
                    // Nœuds normaux
                    drawNormalNode(gc, node);
                }

                // Afficher le label du nœud avec fond blanc pour meilleure lisibilité
                gc.setFill(Color.WHITE);
                gc.setGlobalAlpha(0.8);
                double textWidth = node.getLabel().length() * 6;
                gc.fillRect(node.getX() - 15, node.getY() - 24, textWidth + 4, 14);

                gc.setGlobalAlpha(1.0);
                gc.setFill(Color.BLACK);
                gc.fillText(node.getLabel(), node.getX() - 13, node.getY() - 12);
            }
        }
        
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.DEBUG, "DrawingCanvas.drawGraph: Finished drawing graph elements.");
        }
    }

    /**
     * Dessine un chemin mis en évidence avec un effet visuel impressionnant
     * @param gc Le contexte graphique
     * @param source Le nœud source
     * @param target Le nœud cible
     */
    private void drawHighlightedPath(GraphicsContext gc, Node source, Node target) {
        double startX = source.getX();
        double startY = source.getY();
        double endX = target.getX();
        double endY = target.getY();

        // Sauvegarder l'état actuel
        gc.save();

        // 1. Dessiner l'effet de brillance (glow) - plusieurs couches
        // Couche externe (la plus large et la plus transparente)
        gc.setGlobalAlpha(0.3);
        gc.setStroke(Color.ORANGERED);
        gc.setLineWidth(12.0);
        gc.strokeLine(startX, startY, endX, endY);

        // Couche intermédiaire
        gc.setGlobalAlpha(0.5);
        gc.setStroke(Color.RED);
        gc.setLineWidth(8.0);
        gc.strokeLine(startX, startY, endX, endY);

        // Couche principale (la plus visible)
        gc.setGlobalAlpha(1.0);
        gc.setStroke(Color.CRIMSON);
        gc.setLineWidth(5.0);
        gc.strokeLine(startX, startY, endX, endY);

        // Ligne centrale brillante
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1.5);
        gc.strokeLine(startX, startY, endX, endY);

        // 2. Dessiner une flèche directionnelle au milieu du segment
        drawDirectionArrow(gc, startX, startY, endX, endY);

        // Restaurer l'état
        gc.restore();
    }

    /**
     * Dessine une flèche directionnelle sur le chemin
     * @param gc Le contexte graphique
     * @param startX Coordonnée X de départ
     * @param startY Coordonnée Y de départ
     * @param endX Coordonnée X d'arrivée
     * @param endY Coordonnée Y d'arrivée
     */
    private void drawDirectionArrow(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        // Calculer le point milieu
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;

        // Calculer l'angle de la ligne
        double angle = Math.atan2(endY - startY, endX - startX);

        // Taille de la flèche
        double arrowLength = 15;
        double arrowAngle = Math.PI / 6; // 30 degrés

        // Calculer les points de la flèche
        double arrowX1 = midX - arrowLength * Math.cos(angle - arrowAngle);
        double arrowY1 = midY - arrowLength * Math.sin(angle - arrowAngle);
        double arrowX2 = midX - arrowLength * Math.cos(angle + arrowAngle);
        double arrowY2 = midY - arrowLength * Math.sin(angle + arrowAngle);

        // Dessiner la flèche
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3.0);
        gc.strokeLine(midX, midY, arrowX1, arrowY1);
        gc.strokeLine(midX, midY, arrowX2, arrowY2);

        // Contour noir pour meilleure visibilité
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.strokeLine(midX, midY, arrowX1, arrowY1);
        gc.strokeLine(midX, midY, arrowX2, arrowY2);
    }

    /**
     * Vérifie si un nœud fait partie du chemin mis en évidence
     * @param node Le nœud à vérifier
     * @param graph Le graphe contenant les arêtes
     * @return true si le nœud est sur le chemin mis en évidence
     */
    private boolean isNodeOnHighlightedPath(Node node, Graph graph) {
        for (Edge edge : graph.getEdges()) {
            if (edge.isHighlighted()) {
                if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Dessine un nœud sélectionné avec effet spécial
     * @param gc Le contexte graphique
     * @param node Le nœud à dessiner
     */
    private void drawSelectedNode(GraphicsContext gc, Node node) {
        gc.save();

        // Effet de pulsation avec plusieurs cercles
        gc.setGlobalAlpha(0.4);
        gc.setFill(Color.YELLOW);
        gc.fillOval(node.getX() - 15, node.getY() - 15, 30, 30);

        gc.setGlobalAlpha(0.7);
        gc.setFill(Color.ORANGE);
        gc.fillOval(node.getX() - 12, node.getY() - 12, 24, 24);

        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.DARKRED);
        gc.fillOval(node.getX() - 10, node.getY() - 10, 20, 20);

        // Bordure brillante
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3.0);
        gc.strokeOval(node.getX() - 10, node.getY() - 10, 20, 20);

        gc.restore();
    }

    /**
     * Dessine un nœud sur le chemin mis en évidence
     * @param gc Le contexte graphique
     * @param node Le nœud à dessiner
     */
    private void drawPathNode(GraphicsContext gc, Node node) {
        gc.save();

        // Effet de brillance pour les nœuds du chemin
        gc.setGlobalAlpha(0.5);
        gc.setFill(Color.ORANGERED);
        gc.fillOval(node.getX() - 12, node.getY() - 12, 24, 24);

        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.CRIMSON);
        gc.fillOval(node.getX() - 9, node.getY() - 9, 18, 18);

        // Bordure dorée
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2.5);
        gc.strokeOval(node.getX() - 9, node.getY() - 9, 18, 18);

        // Point central brillant
        gc.setFill(Color.WHITE);
        gc.fillOval(node.getX() - 2, node.getY() - 2, 4, 4);

        gc.restore();
    }

    /**
     * Dessine un nœud normal
     * @param gc Le contexte graphique
     * @param node Le nœud à dessiner
     */
    private void drawNormalNode(GraphicsContext gc, Node node) {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillOval(node.getX() - 8, node.getY() - 8, 16, 16);
        gc.setStroke(Color.DARKBLUE);
        gc.setLineWidth(2.0);
        gc.strokeOval(node.getX() - 8, node.getY() - 8, 16, 16);
    }
}
