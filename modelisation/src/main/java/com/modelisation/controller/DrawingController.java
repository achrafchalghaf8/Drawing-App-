package com.modelisation.controller;

import com.modelisation.model.Drawing;
import com.modelisation.database.DatabaseManager;
import com.modelisation.database.DrawingPersistenceManager;
import com.modelisation.model.logging.LoggingStrategy;
import com.modelisation.model.logging.DatabaseLoggingStrategy;
import com.modelisation.view.MainView;
import com.modelisation.view.DimensionType;
import com.modelisation.model.factories.AbstractShapeFactory;
import com.modelisation.model.factories.Factory2D;
import com.modelisation.model.factories.Factory3D;
import com.modelisation.model.shapes.ShapeFactory; // Assuming this enum exists for shape types
import javafx.scene.paint.Color; // For Color parameter in addShape
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import com.modelisation.model.graph.Graph;
import com.modelisation.model.graph.Node;
import com.modelisation.model.graph.Edge;
import com.modelisation.model.graph.algorithms.DijkstraStrategy;
import com.modelisation.model.graph.algorithms.BFSAlgorithm;
import com.modelisation.model.graph.algorithms.ShortestPathStrategy;
import com.modelisation.model.shapes.Shape;
import com.modelisation.model.shapes.Rectangle; // Assuming Rectangle is a primary shape type
import com.modelisation.model.shapes.Circle;     // For getShapeCenter
import com.modelisation.model.shapes.Line;     // For getShapeCenter
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID; // For potentially more robust node IDs
import javafx.application.Platform;

/**
 * Contrôleur principal de l'application
 * Gère la logique métier et fait le lien entre la vue et le modèle
 * Implémente le pattern MVC
 */
public class DrawingController {
    
    private Drawing currentDrawing;
    private MainView mainView;
    private DatabaseManager databaseManager;
    private DrawingPersistenceManager persistenceManager;
    private ObjectMapper jsonMapper;
    private File currentFile;
    private int currentDrawingId = -1; // ID du dessin en base de données
    private String currentDrawingLogFile; // Fichier de log spécifique au dessin actuel

    // Fields for Shortest Path functionality
    private boolean shortestPathMode = false;
    private Shape selectedStartShapeForPath = null;
    private Graph currentGraphForPathfinding = null;
    private Map<Shape, Node> shapeToNodeMapForPathfinding = new HashMap<>();

    // Algorithm selection
    private ShortestPathStrategy currentAlgorithm = new DijkstraStrategy();
    private String currentAlgorithmName = "Dijkstra";

    private AbstractShapeFactory factory2D = new Factory2D();
    private AbstractShapeFactory factory3D = new Factory3D();
    
    public DrawingController(Drawing drawing, MainView mainView) {
        this.currentDrawing = drawing;
        this.mainView = mainView;
        this.databaseManager = DatabaseManager.getInstance();
        this.persistenceManager = new DrawingPersistenceManager();
        this.jsonMapper = new ObjectMapper();

        // Configurer le logger pour le gestionnaire de persistance
        LoggingStrategy logger = mainView.getCurrentLogger();
        if (logger != null) {
            this.persistenceManager.setLogger(logger);
        }

        // Tester la connexion à la base de données
        if (databaseManager.testConnection()) {
            logAction("Connexion à la base de données MySQL établie avec succès");
        } else {
            logError("Impossible de se connecter à la base de données MySQL", null);
        }
    }

    /**
     * Adds a shape to the current drawing using the Abstract Factory pattern.
     *
     * @param shapeType     The type of shape to create (e.g., CIRCLE, RECTANGLE).
     * @param dimensionType The dimension of the shape (D2 or D3).
     * @param x             The x-coordinate (center for circle, top-left for rectangle).
     * @param y             The y-coordinate (center for circle, top-left for rectangle).
     * @param param1        Radius for circle, width for rectangle.
     * @param param2        Unused for circle, height for rectangle.
     * @param color         The color of the shape.
     * @param strokeWidth   The stroke width of the shape.
     */
    public void addShape(ShapeFactory.ShapeType shapeType, DimensionType dimensionType, 
                         double x, double y, double param1, double param2, 
                         Color color, double strokeWidth) {
        
        AbstractShapeFactory factory;
        if (dimensionType == DimensionType.D3) {
            factory = factory3D;
        } else {
            factory = factory2D; // Default to 2D
        }

        Shape newShape = null;
        String shapeNameForLog = "Unknown";

        switch (shapeType) {
            case CIRCLE:
                newShape = factory.createCircle(x, y, param1, color, strokeWidth);
                shapeNameForLog = (dimensionType == DimensionType.D3 ? "3D " : "2D ") + "Circle";
                break;
            case RECTANGLE:
                newShape = factory.createRectangle(x, y, param1, param2, color, strokeWidth);
                shapeNameForLog = (dimensionType == DimensionType.D3 ? "3D " : "2D ") + "Rectangle";
                break;
            // Add cases for other shapes like LINE if they also support 2D/3D
            default:
                logError("Unsupported shape type for abstract factory: " + shapeType, null);
                return;
        }

        if (newShape != null) {
            currentDrawing.addShape(newShape);
            mainView.getDrawingCanvas().redraw(); // Redraw the canvas
            logAction(shapeNameForLog + " added at (" + String.format("%.2f", x) + ", " + String.format("%.2f", y) + ")");
            mainView.getStatusBar().setMessage(shapeNameForLog + " added.");
        } else {
            logError("Failed to create shape: " + shapeNameForLog, null);
            mainView.getStatusBar().setMessage("Error creating " + shapeNameForLog + ".");
        }
    }

    
    /**
     * Crée un nouveau dessin
     */
    public void newDrawing() {
        // Demander confirmation si le dessin actuel n'est pas sauvegardé
        if (hasUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Nouveau dessin");
            alert.setHeaderText("Le dessin actuel n'est pas sauvegardé");
            alert.setContentText("Voulez-vous continuer sans sauvegarder ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        // Créer un nouveau dessin
        Drawing newDrawing = new Drawing("Nouveau dessin", "");
        newDrawing.setLogger(currentDrawing.getLogger());

        // Mettre à jour la vue
        mainView.getDrawingCanvas().setDrawing(newDrawing);
        currentDrawing = newDrawing;
        currentFile = null;

        // Logger l'action
        logAction("Nouveau dessin créé");
        mainView.getStatusBar().setMessage("Nouveau dessin créé");
    }
    
    /**
     * Ouvre un dessin existant
     */
    public void openDrawing() {
        // Choix entre fichier et base de données
        Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        choiceAlert.setTitle("Ouvrir un dessin");
        choiceAlert.setHeaderText("Choisissez la source");
        choiceAlert.setContentText("Voulez-vous ouvrir depuis un fichier ou la base de données ?");
        
        ButtonType fileButton = new ButtonType("Fichier");
        ButtonType databaseButton = new ButtonType("Base de données");
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        choiceAlert.getButtonTypes().setAll(fileButton, databaseButton, cancelButton);
        
        Optional<ButtonType> choice = choiceAlert.showAndWait();
        
        if (choice.isPresent()) {
            if (choice.get() == fileButton) {
                openFromFile();
            } else if (choice.get() == databaseButton) {
                openFromDatabase();
            }
        }
    }
    
    /**
     * Ouvre un dessin depuis un fichier
     */
    private void openFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir un dessin");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
        );
        
        File file = fileChooser.showOpenDialog(mainView.getPrimaryStage());
        if (file != null) {
            try {
                String jsonContent = Files.readString(file.toPath());

                // Extraire le nom du fichier sans extension
                String fileName = file.getName();
                if (fileName.endsWith(".json")) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }

                // Désérialiser le dessin depuis JSON
                Drawing newDrawing = deserializeDrawingFromJson(jsonContent, fileName, "Chargé depuis fichier");
                newDrawing.setLogger(currentDrawing.getLogger());

                // Mettre à jour la vue
                mainView.getDrawingCanvas().setDrawing(newDrawing);
                currentDrawing = newDrawing;
                currentFile = file;
                currentDrawingId = -1; // Pas d'ID de base de données

                logAction("Dessin ouvert depuis le fichier: " + file.getName() + " (" + newDrawing.getShapeCount() + " formes)");
                mainView.getStatusBar().setMessage("Dessin ouvert: " + file.getName() + " (" + newDrawing.getShapeCount() + " formes)");

                // Redessiner le canvas
                mainView.getDrawingCanvas().redraw();

            } catch (IOException e) {
                showError("Erreur lors de l'ouverture du fichier", e.getMessage());
                logError("Erreur lors de l'ouverture du fichier", e);
            } catch (Exception e) {
                showError("Erreur de format", "Le fichier ne semble pas être un dessin valide: " + e.getMessage());
                logError("Erreur lors de la désérialisation du fichier", e);
            }
        }
    }
    
    /**
     * Ouvre un dessin depuis la base de données
     */
    private void openFromDatabase() {
        // Charger la liste des dessins disponibles
        List<DrawingPersistenceManager.DrawingInfo> drawings = persistenceManager.getDrawingsList();

        if (drawings.isEmpty()) {
            showError("Aucun dessin", "Aucun dessin trouvé dans la base de données");
            return;
        }

        // Créer une boîte de dialogue de sélection
        ChoiceDialog<DrawingPersistenceManager.DrawingInfo> dialog = new ChoiceDialog<>(drawings.get(0), drawings);
        dialog.setTitle("Ouvrir depuis la base de données");
        dialog.setHeaderText("Sélectionnez un dessin à ouvrir");
        dialog.setContentText("Dessin:");

        Optional<DrawingPersistenceManager.DrawingInfo> result = dialog.showAndWait();
        if (result.isPresent()) {
            DrawingPersistenceManager.DrawingInfo selectedDrawing = result.get();

            String jsonData = persistenceManager.getDrawingJson(selectedDrawing.id);
            if (jsonData != null) {
                try {
                    // Désérialiser le dessin depuis JSON
                    Drawing newDrawing = deserializeDrawingFromJson(jsonData, selectedDrawing.name, selectedDrawing.description);
                    newDrawing.setLogger(currentDrawing.getLogger());

                    // Mettre à jour la vue
                    mainView.getDrawingCanvas().setDrawing(newDrawing);
                    currentDrawing = newDrawing;
                    currentDrawingId = selectedDrawing.id;
                    currentFile = null;

                    logAction("Dessin ouvert depuis la base de données: " + selectedDrawing.name + " (ID: " + selectedDrawing.id + ", " + newDrawing.getShapeCount() + " formes)");
                    mainView.getStatusBar().setMessage("Dessin ouvert: " + selectedDrawing.name + " (" + newDrawing.getShapeCount() + " formes)");

                    // Redessiner le canvas
                    mainView.getDrawingCanvas().redraw();

                } catch (Exception e) {
                    showError("Erreur de désérialisation", "Impossible de charger le dessin: " + e.getMessage());
                    logError("Erreur lors de la désérialisation du dessin", e);
                }
            } else {
                showError("Erreur", "Impossible de charger les données du dessin");
            }
        }
    }
    
    /**
     * Sauvegarde le dessin actuel
     */
    public void saveDrawing() {
        if (currentFile != null) {
            saveToFile(currentFile);
        } else {
            saveDrawingAs();
        }
    }
    
    /**
     * Sauvegarde le dessin avec un nouveau nom
     */
    public void saveDrawingAs() {
        // Choix entre fichier et base de données
        Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        choiceAlert.setTitle("Enregistrer le dessin");
        choiceAlert.setHeaderText("Choisissez la destination");
        choiceAlert.setContentText("Voulez-vous enregistrer dans un fichier ou la base de données ?");
        
        ButtonType fileButton = new ButtonType("Fichier");
        ButtonType databaseButton = new ButtonType("Base de données");
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        choiceAlert.getButtonTypes().setAll(fileButton, databaseButton, cancelButton);
        
        Optional<ButtonType> choice = choiceAlert.showAndWait();
        
        if (choice.isPresent()) {
            if (choice.get() == fileButton) {
                saveAsFile();
            } else if (choice.get() == databaseButton) {
                saveToDatabase();
            }
        }
    }
    
    /**
     * Sauvegarde dans un fichier
     */
    private void saveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le dessin");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(mainView.getPrimaryStage());
        if (file != null) {
            saveToFile(file);
        }
    }
    
    /**
     * Sauvegarde dans un fichier spécifique
     */
    private void saveToFile(File file) {
        try {
            // Extraire le nom du fichier sans extension pour le nom du dessin
            String fileName = file.getName();
            if (fileName.endsWith(".json")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            }

            // Mettre à jour le nom du dessin
            currentDrawing.setName(fileName);
            currentDrawing.setDescription("Sauvegardé dans fichier");

            // Utiliser le gestionnaire de persistance pour générer le JSON
            String jsonContent = persistenceManager.convertDrawingToJson(currentDrawing);

            Files.writeString(file.toPath(), jsonContent);
            currentFile = file;

            logAction("Dessin sauvegardé dans le fichier: " + file.getName() + " (" + currentDrawing.getShapeCount() + " formes)");
            mainView.getStatusBar().setMessage("Dessin sauvegardé: " + file.getName());

        } catch (IOException e) {
            showError("Erreur lors de la sauvegarde", e.getMessage());
            logError("Erreur lors de la sauvegarde du fichier", e);
        } catch (Exception e) {
            showError("Erreur de sérialisation", "Impossible de sauvegarder le dessin: " + e.getMessage());
            logError("Erreur lors de la sérialisation du fichier", e);
        }
    }
    
    /**
     */
    private void saveToDatabase() {
        // Demander le nom et la description
        TextInputDialog nameDialog = new TextInputDialog(currentDrawing.getName());
        nameDialog.setTitle("Enregistrer en base de données");
        nameDialog.setHeaderText("Nom du dessin");
        nameDialog.setContentText("Nom:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isPresent()) {
            String name = nameResult.get();

            TextInputDialog descDialog = new TextInputDialog(currentDrawing.getDescription());
            descDialog.setTitle("Enregistrer en base de données");
            descDialog.setHeaderText("Description du dessin");
            descDialog.setContentText("Description:");

            Optional<String> descResult = descDialog.showAndWait();
            String description = descResult.orElse("");

            // Utiliser le nouveau gestionnaire de persistance
            int drawingId = persistenceManager.saveDrawing(currentDrawing, name, description);

            if (drawingId > 0) {
                currentDrawing.setName(name);
                currentDrawing.setDescription(description);
                currentDrawingId = drawingId;

                logAction("Dessin sauvegardé en base de données, ID: " + drawingId + " avec " + currentDrawing.getShapeCount() + " formes");
                mainView.getStatusBar().setMessage("Dessin sauvegardé en base de données (ID: " + drawingId + ")");
            } else {
                showError("Erreur de sauvegarde", "Impossible de sauvegarder le dessin en base de données");
            }
        }
    }
    
    /**
     * Efface le dessin actuel
     */
    public void clearDrawing() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Effacer le dessin");
        alert.setHeaderText("Confirmer l'effacement");
        alert.setContentText("Êtes-vous sûr de vouloir effacer tout le dessin ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentDrawing.clear();
            logAction("Dessin effacé");
            mainView.getStatusBar().setMessage("Dessin effacé");
        }
    }
    
    /**
     * Vérifie s'il y a des changements non sauvegardés
     */
    private boolean hasUnsavedChanges() {
        // TODO: Implémenter la détection des changements non sauvegardés
        return currentDrawing.getShapeCount() > 0 && currentFile == null;
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Enregistre une action dans le log
     */
    private void logAction(String message) {
        LoggingStrategy logger = mainView.getCurrentLogger();
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.INFO, message);
        }
    }
    
    /**
     * Enregistre une erreur dans le log
     */
    private void logError(String message, Exception e) {
        LoggingStrategy logger = mainView.getCurrentLogger();
        if (logger != null) {
            logger.log(LoggingStrategy.LogLevel.ERROR, message, e);
        }
    }
    
    // Getters
    public Drawing getCurrentDrawing() {
        return currentDrawing;
    }
    
    public File getCurrentFile() {
        return currentFile;
    }

    // --- Shortest Path Functionality ---

    public boolean isShortestPathModeActive() {
        return shortestPathMode;
    }

    public void toggleShortestPathMode() {
        shortestPathMode = !shortestPathMode;
        selectedStartShapeForPath = null; // Reset manual selection

        if (shortestPathMode) {
            logAction("Shortest path mode activated. Waiting for manual node selection.");
            mainView.getStatusBar().setMessage("Mode Plus Court Chemin: Cliquez sur le nœud de DÉBUT");

            // Activer le mode de sélection de chemin dans le canvas
            mainView.getDrawingCanvas().setInteractionMode("PATH_SELECTION");

            if (currentDrawing == null || currentDrawing.getShapes().size() < 2) {
                logAction("Shortest Path: Not enough shapes to find a path (<2).");
                mainView.getStatusBar().setMessage("Veuillez dessiner au moins deux formes pour utiliser le mode plus court chemin.");
                currentGraphForPathfinding = new Graph(false); // Ensure it's an empty graph if no shapes
                if (currentDrawing != null && !currentDrawing.getShapes().isEmpty()) { // if 1 shape, build graph for it
                    currentGraphForPathfinding = buildGraphFromDrawing(currentDrawing);
                }
                mainView.getDrawingCanvas().drawGraph(currentGraphForPathfinding);
                return; // Exit if not enough shapes
            }

            // Construire le graphe mais ne pas calculer automatiquement le chemin
            currentGraphForPathfinding = buildGraphFromDrawing(currentDrawing);
            currentGraphForPathfinding.resetHighlights(); // Clear previous highlights

            if (currentGraphForPathfinding.getNodes().size() >= 2) {
                logAction("Shortest Path: Graph built with " + currentGraphForPathfinding.getNodes().size() + " nodes. Ready for manual selection.");
                mainView.getStatusBar().setMessage("Mode Plus Court Chemin: Cliquez sur le nœud de DÉBUT");
            } else {
                logAction("Shortest Path: Graph built, but has < 2 nodes. Cannot find path.");
                mainView.getStatusBar().setMessage("Moins de deux nœuds dans le graphe après construction.");
            }
            mainView.getDrawingCanvas().drawGraph(currentGraphForPathfinding); // Redraw to show all nodes
        } else {
            logAction("Shortest path mode deactivated.");
            mainView.getStatusBar().setMessage("Mode normal activé.");

            // Désactiver le mode de sélection de chemin dans le canvas
            mainView.getDrawingCanvas().setInteractionMode("NORMAL");

            if (currentGraphForPathfinding != null) {
                currentGraphForPathfinding.resetHighlights();
                // Redessiner le dessin normal sans le graphe
                mainView.getDrawingCanvas().redraw();
            }
            shapeToNodeMapForPathfinding.clear(); // Clear map when mode is off
        }
        // Ensure canvas reflects mode change (e.g., cursor, visual cues if any)
    }

    public void handleCanvasClickForShortestPath(double x, double y) {
        if (!shortestPathMode || currentDrawing == null || currentDrawing.getShapes().isEmpty()) {
            return;
        }
    
        Shape clickedShape = currentDrawing.findShapeAt(x, y);
        if (clickedShape == null) {
            logAction("Shortest Path: Clicked on empty area.");
            return;
        }
    
        // Ne reconstruire le graphe que si vraiment nécessaire
        boolean needsRebuild = currentGraphForPathfinding == null ||
                              currentDrawing.getShapes().size() != currentGraphForPathfinding.getNodes().size() ||
                              shapeToNodeMapForPathfinding.size() != currentDrawing.getShapes().size();

        if (needsRebuild) {
            logAction("Shortest Path: Rebuilding graph due to changes in drawing");
            currentGraphForPathfinding = buildGraphFromDrawing(currentDrawing);
        } else {
            logAction("Shortest Path: Using existing graph (no rebuild needed)");
        }
    
        Node clickedGraphNode = shapeToNodeMapForPathfinding.get(clickedShape);
    
        logAction(String.format("CanvasClick: Clicked Shape Hash: %s, Mapped to Node ID: %s, Label: %s, Hash: %s",
                                System.identityHashCode(clickedShape),
                                (clickedGraphNode != null ? clickedGraphNode.getId() : "null"),
                                (clickedGraphNode != null ? clickedGraphNode.getLabel() : "null"),
                                System.identityHashCode(clickedGraphNode)));
    
        if (clickedGraphNode == null) {
            logError("Shortest Path: Clicked shape not found in internal graph mapping. Graph might be out of sync.", null);
            showError("Erreur interne", "La forme cliquée n'a pas pu être mappée à un nœud du graphe. Essayez de désactiver et réactiver le mode chemin le plus court.");
            return;
        }
    
        if (selectedStartShapeForPath == null) {
            // This is the first click, selecting the start shape
            selectedStartShapeForPath = clickedShape;
            clickedGraphNode.setSelected(true); // Visually mark as selected
            logAction("Shortest Path: Start shape selected - " + clickedGraphNode.getLabel());
            mainView.getStatusBar().setMessage("Nœud de DÉBUT sélectionné (" + clickedGraphNode.getLabel() + "). Cliquez sur le nœud de FIN.");
            mainView.getDrawingCanvas().drawGraph(currentGraphForPathfinding); // Redraw to show selection
        } else {
            // This is the second click, selecting the end shape
            Node actualStartNode = shapeToNodeMapForPathfinding.get(selectedStartShapeForPath);
            Node actualEndNode = clickedGraphNode; // Node for the currently clicked shape
    
            if (actualStartNode == null) {
                logError("Shortest Path: Previously selected start shape (ID: " + (selectedStartShapeForPath != null ? selectedStartShapeForPath.getClass().getSimpleName() + selectedStartShapeForPath.hashCode() : "null") + ") could not be re-mapped to a graph node. Graph might have changed.", null);
                showError("Erreur interne", "La forme de départ sélectionnée est devenue invalide. Veuillez recommencer.");
                selectedStartShapeForPath = null; // Reset
                if (currentGraphForPathfinding != null) {
                    currentGraphForPathfinding.resetHighlights(); // Clear any visual selection
                     // Attempt to get the node for the previously selected shape to deselect it visually
                    Node prevSelectedNode = shapeToNodeMapForPathfinding.get(selectedStartShapeForPath); // This will be null if selectedStartShapeForPath is null
                    if (prevSelectedNode != null) {
                        prevSelectedNode.setSelected(false);
                    }
                }
                mainView.getStatusBar().setMessage("Shortest Path Mode: Select start shape.");
                if (currentGraphForPathfinding != null) mainView.getDrawingCanvas().drawGraph(currentGraphForPathfinding);
                return;
            }
    
            if (actualEndNode.equals(actualStartNode)) {
                logAction("Shortest Path: Clicked on the same shape/node again. End node selection cancelled.");
                mainView.getStatusBar().setMessage("Shortest Path Mode: End node cannot be same as start. Select a different end shape.");
                return; // Do not proceed if start and end are the same
            }
    
            logAction("Shortest Path: End shape selected - " + actualEndNode.getLabel() + ". Calculating path.");
            mainView.getStatusBar().setMessage("Calculating shortest path from " + actualStartNode.getLabel() + " to " + actualEndNode.getLabel());

            logAction(String.format("CanvasClick: Calling %s. Graph Hash: %s, StartNode ID: %s, Hash: %s, EndNode ID: %s, Hash: %s",
                                    currentAlgorithmName,
                                    System.identityHashCode(currentGraphForPathfinding),
                                    actualStartNode.getId(), System.identityHashCode(actualStartNode),
                                    actualEndNode.getId(), System.identityHashCode(actualEndNode)));

            // Mesurer le temps d'exécution
            long startTime = System.currentTimeMillis();
            List<Node> path = currentAlgorithm.findShortestPath(currentGraphForPathfinding, actualStartNode, actualEndNode);
            long executionTime = System.currentTimeMillis() - startTime;

            // DEBUG: Afficher des informations détaillées sur le chemin
            logAction("DEBUG: Path calculation result:");
            logAction("  - Path is null: " + (path == null));
            logAction("  - Path size: " + (path != null ? path.size() : "N/A"));
            if (path != null && !path.isEmpty()) {
                StringBuilder pathStr = new StringBuilder("  - Path nodes: ");
                for (int i = 0; i < path.size(); i++) {
                    pathStr.append(path.get(i).getLabel());
                    if (i < path.size() - 1) pathStr.append(" -> ");
                }
                logAction(pathStr.toString());
            }

            // DEBUG: Vérifier les arêtes du graphe
            logAction("DEBUG: Graph edges count: " + currentGraphForPathfinding.getEdges().size());
            for (int i = 0; i < Math.min(5, currentGraphForPathfinding.getEdges().size()); i++) {
                var edge = currentGraphForPathfinding.getEdges().get(i);
                logAction("  - Edge " + i + ": " + edge.getSource().getLabel() + " -> " + edge.getTarget().getLabel() + " (weight: " + edge.getWeight() + ")");
            }

            currentGraphForPathfinding.resetHighlights(); // Clear previous highlights (like start node selection)
            actualStartNode.setSelected(false); // Deselect the visual start node

            if (path == null || path.isEmpty()) {
                logAction("Shortest Path: No path found.");
                mainView.getStatusBar().setMessage("Aucun chemin trouvé entre " + actualStartNode.getLabel() + " et " + actualEndNode.getLabel());
                showError("Chemin non trouvé", "Aucun chemin n'a pu être trouvé entre les formes sélectionnées.");

                // Enregistrer l'échec en base de données
                if (currentDrawingId > 0) {
                    saveShortestPathSession(actualStartNode.getLabel(), actualEndNode.getLabel(),
                                          0, 0.0, "[]", executionTime);
                }
            } else {
                // Calculer la distance totale du chemin
                double totalDistance = calculatePathDistance(path);

                logAction("Shortest Path: Path found with " + path.size() + " nodes, total distance: " + String.format("%.2f", totalDistance));
                currentGraphForPathfinding.highlightPath(path);
                currentGraphForPathfinding.setDeemphasizeNonHighlightedEdges(true); // De-emphasize other edges
                mainView.getStatusBar().setMessage("✅ Plus court chemin trouvé: " + actualStartNode.getLabel() + " → " + actualEndNode.getLabel() + " (" + (path.size() - 1) + " segments, " + String.format("%.2f", totalDistance) + " unités)");

                // Enregistrer le succès en base de données
                if (currentDrawingId > 0) {
                    String pathNodesJson = convertPathToJson(path);
                    saveShortestPathSession(actualStartNode.getLabel(), actualEndNode.getLabel(),
                                          path.size() - 1, totalDistance, pathNodesJson, executionTime);
                }
            }
            mainView.getDrawingCanvas().drawGraph(currentGraphForPathfinding); // Redraw to show path/cleared selection
            selectedStartShapeForPath = null; // Reset for next selection

            // Attendre 3 secondes puis remettre le message de sélection
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        if (shortestPathMode) { // Vérifier que le mode est toujours actif
                            mainView.getStatusBar().setMessage("Mode Plus Court Chemin: Cliquez sur le nœud de DÉBUT");
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private Graph buildGraphFromDrawing(Drawing drawing) {
        Graph graph = new Graph(false); // Assuming undirected graph
        if (drawing != null && drawing.getLogger() != null) {
            graph.setLogger(drawing.getLogger());
        }
        logAction("BuildGraph: Initializing. shapeToNodeMap size before clear: " + shapeToNodeMapForPathfinding.size());
        shapeToNodeMapForPathfinding.clear(); // Clear previous mapping
        logAction("BuildGraph: shapeToNodeMap size after clear: " + shapeToNodeMapForPathfinding.size());
    
        if (drawing == null) { // Guard against null drawing
            logError("BuildGraph: Drawing object is null.", null);
            return graph; // Return empty graph
        }
    
        List<Shape> shapes = drawing.getShapes();
        logAction("BuildGraph: Number of shapes retrieved from drawing: " + (shapes != null ? shapes.size() : "null list"));
    
        if (shapes == null || shapes.isEmpty()) {
            logAction("BuildGraph: No shapes in drawing (or shapes list is null) to build graph from.");
            return graph; // Return empty graph if no shapes
        }
        logAction("BuildGraph: Starting node creation loop. Initial graph.getNodesMapSize(): " + graph.getNodesMapSize());
    
        // 1. Create nodes from shapes with alphabetic labels for circles and numeric for lines
        int circleCounter = 0;
        int lineCounter = 1; // Start lines at 1
        int rectangleCounter = 0;

        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            Point2D center = getShapeCenter(shape);
            // Using a more robust ID combining class name and a short UUID part
            String nodeId = shape.getClass().getSimpleName() + "_" + UUID.randomUUID().toString().substring(0, 8);
            Node node = new Node(nodeId, center.getX(), center.getY());

            // Créer des labels personnalisés selon le type de forme
            String label;
            if (shape instanceof com.modelisation.model.shapes.Circle) {
                // Cercles avec lettres : A, B, C, D, ...
                label = String.valueOf((char)('A' + circleCounter));
                circleCounter++;
            } else if (shape instanceof com.modelisation.model.shapes.Line) {
                // Lignes avec nombres : 1, 2, 3, 4, ...
                label = String.valueOf(lineCounter);
                lineCounter++;
            } else if (shape instanceof com.modelisation.model.shapes.Rectangle) {
                // Rectangles avec lettres : A, B, C, D, ... (partagent avec les cercles)
                label = String.valueOf((char)('A' + rectangleCounter));
                rectangleCounter++;
            } else {
                // Autres formes avec l'ancien système
                label = shape.getClass().getSimpleName() + " " + i;
            }

            node.setLabel(label);
            graph.addNode(node);
            shapeToNodeMapForPathfinding.put(shape, node); // Store mapping
            logAction(String.format("BuildGraph: Created Node ID: %s, Label: %s, Hash: %s for Shape Hash: %s (Class: %s). Current graph.getNodesMapSize(): %d, shapeToNodeMap size: %d",
                                    nodeId, node.getLabel(), System.identityHashCode(node),
                                    System.identityHashCode(shape), shape.getClass().getSimpleName(),
                                    graph.getNodesMapSize(), shapeToNodeMapForPathfinding.size()));
        }
        logAction("BuildGraph: Finished node creation. Final graph.getNodesMapSize() before edge creation: " + graph.getNodesMapSize() + ", shapeToNodeMap size: " + shapeToNodeMapForPathfinding.size());
    
        // 2. Create edges based on proximity (not fully connected graph)
        // Ensure we use the actual nodes from the graph for edge creation
        Collection<Node> graphNodes = graph.getNodeCollection(); // Use the new method to get nodes directly from the map's values
        logAction("BuildGraph: Number of nodes retrieved for edge creation: " + graphNodes.size());
        List<Node> nodesForEdges = new ArrayList<>(graphNodes);

        // Calculate dynamic proximity threshold based on canvas size and number of nodes
        double proximityThreshold = calculateProximityThreshold(nodesForEdges);
        logAction(String.format("BuildGraph: Using proximity threshold: %.2f", proximityThreshold));

        int edgesCreated = 0;
        for (int i = 0; i < nodesForEdges.size(); i++) {
            for (int j = i + 1; j < nodesForEdges.size(); j++) {
                Node node1 = nodesForEdges.get(i);
                Node node2 = nodesForEdges.get(j);
                double distance = Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2) +
                                           Math.pow(node1.getY() - node2.getY(), 2));

                // Only create edge if nodes are within proximity threshold
                if (distance > 0 && distance <= proximityThreshold) {
                    graph.addEdge(new Edge(node1, node2, distance));
                    edgesCreated++;
                    logAction(String.format("BuildGraph: Created edge %s-%s (distance: %.2f)",
                                          node1.getLabel(), node2.getLabel(), distance));
                }
            }
        }

        logAction(String.format("BuildGraph: Created %d edges with proximity threshold %.2f",
                                edgesCreated, proximityThreshold));

        // Vérifier la connectivité et ajouter des arêtes si nécessaire
        ensureGraphConnectivity(graph, nodesForEdges, proximityThreshold);

        logAction(String.format("BuildGraph: Finished. Final graph.getNodesMapSize(): %d, graph.getNodes().size(): %d, graph.getEdges().size(): %d, shapeToNodeMap size: %d",
                                graph.getNodesMapSize(), graph.getNodes().size(), graph.getEdges().size(), shapeToNodeMapForPathfinding.size()));
        return graph;
    }

    private Point2D getShapeCenter(Shape shape) {
        // Ensure getX() and getY() from Shape are consistently the top-left for calculations here
        if (shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;
            return new Point2D(rect.getX() + rect.getWidth() / 2,
                               rect.getY() + rect.getHeight() / 2);
        }
        else if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            // For Circle, getX() and getY() from the parent Shape class store the center coordinates
            return new Point2D(circle.getX(), circle.getY());
        }
        else if (shape instanceof Line) {
            Line line = (Line) shape;
            double midX = (line.getX() + line.getEndX()) / 2;
            double midY = (line.getY() + line.getEndY()) / 2;
            return new Point2D(midX, midY);
        }
        else {
            // Default fallback: use the shape's primary (x,y) coordinates.
            // This might be top-left for many shapes, adjust if specific shapes have different anchor points.
            logAction("Warning: Using default (x,y) as center for shape type: " + shape.getClass().getSimpleName());
            return new Point2D(shape.getX(), shape.getY());
        }
    }

    /**
     * Calcule le seuil de proximité dynamique pour la création d'arêtes
     * Basé sur la taille du canvas et le nombre de nœuds
     */
    private double calculateProximityThreshold(List<Node> nodes) {
        if (nodes.size() < 2) {
            return Double.MAX_VALUE; // Si moins de 2 nœuds, connecter tout
        }

        // Calculer les dimensions du canvas basées sur les positions des nœuds
        double minX = nodes.stream().mapToDouble(Node::getX).min().orElse(0);
        double maxX = nodes.stream().mapToDouble(Node::getX).max().orElse(800);
        double minY = nodes.stream().mapToDouble(Node::getY).min().orElse(0);
        double maxY = nodes.stream().mapToDouble(Node::getY).max().orElse(600);

        double canvasWidth = maxX - minX;
        double canvasHeight = maxY - minY;
        double canvasDiagonal = Math.sqrt(canvasWidth * canvasWidth + canvasHeight * canvasHeight);

        // Calculer la distance moyenne entre tous les nœuds
        double totalDistance = 0;
        int pairCount = 0;
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);
                double distance = Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2) +
                                          Math.pow(node1.getY() - node2.getY(), 2));
                totalDistance += distance;
                pairCount++;
            }
        }
        double averageDistance = totalDistance / pairCount;

        // Le seuil est basé sur un pourcentage de la distance moyenne
        // Plus il y a de nœuds, plus le seuil est restrictif
        double baseThreshold = averageDistance * 0.6; // 60% de la distance moyenne

        // Ajuster selon le nombre de nœuds (plus de nœuds = seuil plus petit)
        double nodeCountFactor = Math.max(0.3, 1.0 - (nodes.size() - 2) * 0.1);
        double threshold = baseThreshold * nodeCountFactor;

        // S'assurer que le seuil n'est pas trop petit (au moins 30% de la diagonale du canvas)
        // AUGMENTÉ pour assurer la connectivité du graphe
        double minThreshold = canvasDiagonal * 0.3;
        threshold = Math.max(threshold, minThreshold);

        // S'assurer que le seuil n'est pas trop grand (au plus 80% de la diagonale du canvas)
        double maxThreshold = canvasDiagonal * 0.8;
        threshold = Math.min(threshold, maxThreshold);

        logAction(String.format("ProximityThreshold: Canvas(%.0fx%.0f), AvgDist=%.2f, Nodes=%d, Threshold=%.2f",
                                canvasWidth, canvasHeight, averageDistance, nodes.size(), threshold));

        return threshold;
    }

    /**
     * S'assure que le graphe est connecté en ajoutant des arêtes aux nœuds isolés
     */
    private void ensureGraphConnectivity(Graph graph, List<Node> nodes, double proximityThreshold) {
        if (nodes.size() < 2) {
            return;
        }

        // Identifier les nœuds isolés (sans arêtes)
        List<Node> isolatedNodes = new ArrayList<>();
        for (Node node : nodes) {
            Map<Node, Double> neighbors = graph.getNeighbors(node);
            if (neighbors.isEmpty()) {
                isolatedNodes.add(node);
                logAction("BuildGraph: Found isolated node: " + node.getLabel());
            }
        }

        // Pour chaque nœud isolé, le connecter au nœud le plus proche
        for (Node isolatedNode : isolatedNodes) {
            Node closestNode = null;
            double minDistance = Double.MAX_VALUE;

            for (Node otherNode : nodes) {
                if (otherNode != isolatedNode) {
                    double distance = Math.sqrt(Math.pow(isolatedNode.getX() - otherNode.getX(), 2) +
                                              Math.pow(isolatedNode.getY() - otherNode.getY(), 2));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestNode = otherNode;
                    }
                }
            }

            if (closestNode != null) {
                graph.addEdge(new Edge(isolatedNode, closestNode, minDistance));
                logAction(String.format("BuildGraph: Connected isolated node %s to %s (distance: %.2f)",
                                      isolatedNode.getLabel(), closestNode.getLabel(), minDistance));
            }
        }

        // Vérifier si le graphe est maintenant connecté en utilisant BFS
        if (!isGraphConnected(graph, nodes)) {
            logAction("BuildGraph: Graph still not fully connected, adding minimum spanning tree edges");
            addMinimumSpanningTreeEdges(graph, nodes, proximityThreshold * 1.5);
        }
    }

    /**
     * Vérifie si le graphe est connecté en utilisant BFS
     */
    private boolean isGraphConnected(Graph graph, List<Node> nodes) {
        if (nodes.isEmpty()) {
            return true;
        }

        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();

        // Commencer BFS depuis le premier nœud
        Node startNode = nodes.get(0);
        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            Map<Node, Double> neighbors = graph.getNeighbors(current);

            for (Node neighbor : neighbors.keySet()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        boolean isConnected = visited.size() == nodes.size();
        logAction(String.format("BuildGraph: Connectivity check - Visited %d/%d nodes, Connected: %s",
                                visited.size(), nodes.size(), isConnected));
        return isConnected;
    }

    /**
     * Ajoute des arêtes d'arbre couvrant minimal pour assurer la connectivité
     */
    private void addMinimumSpanningTreeEdges(Graph graph, List<Node> nodes, double maxDistance) {
        // Algorithme de Kruskal simplifié pour ajouter les arêtes manquantes
        List<Edge> potentialEdges = new ArrayList<>();

        // Créer toutes les arêtes possibles
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);
                double distance = Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2) +
                                          Math.pow(node1.getY() - node2.getY(), 2));

                // Vérifier si cette arête existe déjà
                Map<Node, Double> neighbors = graph.getNeighbors(node1);
                if (!neighbors.containsKey(node2) && distance <= maxDistance) {
                    potentialEdges.add(new Edge(node1, node2, distance));
                }
            }
        }

        // Trier par distance
        potentialEdges.sort((e1, e2) -> Double.compare(e1.getWeight(), e2.getWeight()));

        // Ajouter les arêtes jusqu'à ce que le graphe soit connecté
        for (Edge edge : potentialEdges) {
            graph.addEdge(edge);
            logAction(String.format("BuildGraph: Added MST edge %s-%s (distance: %.2f)",
                                  edge.getSource().getLabel(), edge.getTarget().getLabel(), edge.getWeight()));

            if (isGraphConnected(graph, nodes)) {
                break;
            }
        }
    }

    /**
     * Trouve les deux nœuds les plus éloignés dans la liste pour créer un chemin plus intéressant
     */
    private Node[] findFarthestNodes(List<Node> nodes) {
        if (nodes.size() < 2) {
            return new Node[]{nodes.get(0), nodes.get(0)};
        }

        if (nodes.size() == 2) {
            return new Node[]{nodes.get(0), nodes.get(1)};
        }

        double maxDistance = 0;
        Node farthestNode1 = nodes.get(0);
        Node farthestNode2 = nodes.get(1);

        // Trouver la paire de nœuds avec la plus grande distance euclidienne
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);

                double distance = Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2) +
                                          Math.pow(node1.getY() - node2.getY(), 2));

                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestNode1 = node1;
                    farthestNode2 = node2;
                }
            }
        }

        logAction(String.format("Farthest nodes found: %s and %s with distance %.2f",
                               farthestNode1.getLabel(), farthestNode2.getLabel(), maxDistance));

        return new Node[]{farthestNode1, farthestNode2};
    }

    /**
     * Change the shortest path algorithm
     * @param algorithmName "Dijkstra" or "BFS"
     */
    public void setShortestPathAlgorithm(String algorithmName) {
        switch (algorithmName.toLowerCase()) {
            case "dijkstra":
                currentAlgorithm = new DijkstraStrategy();
                currentAlgorithmName = "Dijkstra";
                logAction("Algorithme changé vers: Dijkstra");
                break;
            case "bfs":
                currentAlgorithm = new BFSAlgorithm();
                currentAlgorithmName = "BFS";
                logAction("Algorithme changé vers: BFS (Breadth-First Search)");
                break;
            default:
                logError("Algorithme non reconnu: " + algorithmName, null);
                return;
        }
        mainView.getStatusBar().setMessage("Algorithme de plus court chemin: " + currentAlgorithmName);
    }

    /**
     * Get current algorithm name
     */
    public String getCurrentAlgorithmName() {
        return currentAlgorithmName;
    }

    /**
     * Calcule la distance totale d'un chemin
     */
    private double calculatePathDistance(List<Node> path) {
        if (path == null || path.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);
            double distance = Math.sqrt(Math.pow(next.getX() - current.getX(), 2) +
                                      Math.pow(next.getY() - current.getY(), 2));
            totalDistance += distance;
        }

        return totalDistance;
    }

    /**
     * Convertit un chemin en JSON pour la base de données
     */
    private String convertPathToJson(List<Node> path) {
        if (path == null || path.isEmpty()) {
            return "[]";
        }

        try {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < path.size(); i++) {
                Node node = path.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"label\":\"").append(node.getLabel()).append("\",")
                    .append("\"x\":").append(node.getX()).append(",")
                    .append("\"y\":").append(node.getY())
                    .append("}");
            }
            json.append("]");
            return json.toString();
        } catch (Exception e) {
            logError("Erreur lors de la conversion du chemin en JSON", e);
            return "[]";
        }
    }

    /**
     * Enregistre une session de plus court chemin en base de données
     */
    private void saveShortestPathSession(String startLabel, String endLabel, int pathLength,
                                       double totalDistance, String pathNodes, long executionTime) {
        try {
            databaseManager.saveShortestPathSession(
                currentDrawingId,
                currentAlgorithmName.toUpperCase(),
                startLabel,
                endLabel,
                pathLength,
                totalDistance,
                pathNodes,
                executionTime
            );

            logAction(String.format("Session plus court chemin enregistrée: %s → %s (%s, %.2fms)",
                startLabel, endLabel, currentAlgorithmName, (double)executionTime));

        } catch (Exception e) {
            logError("Erreur lors de l'enregistrement de la session plus court chemin", e);
        }
    }

    /**
     * Désérialise un dessin depuis JSON
     */
    private Drawing deserializeDrawingFromJson(String jsonData, String name, String description) throws Exception {
        Drawing drawing = new Drawing(name, description);

        // Parser le JSON
        com.fasterxml.jackson.databind.JsonNode rootNode = jsonMapper.readTree(jsonData);

        // Récupérer les formes
        com.fasterxml.jackson.databind.JsonNode shapesNode = rootNode.get("shapes");
        if (shapesNode != null && shapesNode.isArray()) {

            for (com.fasterxml.jackson.databind.JsonNode shapeNode : shapesNode) {
                Shape shape = deserializeShape(shapeNode);
                if (shape != null) {
                    drawing.addShape(shape);
                }
            }
        }

        return drawing;
    }

    /**
     * Désérialise une forme depuis un nœud JSON
     */
    private Shape deserializeShape(com.fasterxml.jackson.databind.JsonNode shapeNode) throws Exception {
        String type = shapeNode.get("type").asText();
        double x = shapeNode.get("x").asDouble();
        double y = shapeNode.get("y").asDouble();
        String colorStr = shapeNode.get("color").asText();
        double strokeWidth = shapeNode.get("strokeWidth").asDouble();

        // Parser la couleur
        javafx.scene.paint.Color color = javafx.scene.paint.Color.web(colorStr);

        switch (type) {
            case "Circle":
                double radius = shapeNode.get("radius").asDouble();
                return factory2D.createCircle(x, y, radius, color, strokeWidth);

            case "Rectangle":
                double width = shapeNode.get("width").asDouble();
                double height = shapeNode.get("height").asDouble();
                return factory2D.createRectangle(x, y, width, height, color, strokeWidth);

            case "Line":
                double endX = shapeNode.get("endX").asDouble();
                double endY = shapeNode.get("endY").asDouble();
                return new com.modelisation.model.shapes.Line(x, y, endX, endY, color, strokeWidth);

            default:
                logError("Type de forme non reconnu lors de la désérialisation: " + type, null);
                return null;
        }
    }

    /**
     * Show algorithm selection dialog
     */
    public void showAlgorithmSelectionDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sélection d'algorithme");
        alert.setHeaderText("Choisissez l'algorithme de plus court chemin");
        alert.setContentText("Algorithme actuel: " + currentAlgorithmName);

        ButtonType dijkstraButton = new ButtonType("Dijkstra");
        ButtonType bfsButton = new ButtonType("BFS");
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(dijkstraButton, bfsButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == dijkstraButton) {
                setShortestPathAlgorithm("dijkstra");
            } else if (result.get() == bfsButton) {
                setShortestPathAlgorithm("bfs");
            }
        }
    }

    // getCurrentDrawing() and getCurrentFile() are already present

    /**
     * Crée automatiquement un fichier de log pour un nouveau dessin
     */
    private void createLogFileForDrawing(Drawing drawing) {
        try {
            // Générer un nom de fichier unique basé sur le nom du dessin et timestamp
            String drawingName = drawing.getName().replaceAll("[^a-zA-Z0-9]", "_");
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
            );

            // Créer le nom du fichier de log
            String logFileName = "logs/dessin_" + drawingName + "_" + timestamp + ".log";
            currentDrawingLogFile = logFileName;

            // Créer le répertoire logs s'il n'existe pas
            java.nio.file.Path logDir = java.nio.file.Paths.get("logs");
            if (!java.nio.file.Files.exists(logDir)) {
                java.nio.file.Files.createDirectories(logDir);
                System.out.println("DrawingController - Répertoire logs créé: " + logDir.toAbsolutePath());
            }

            // Créer le fichier de log
            java.nio.file.Path logFile = java.nio.file.Paths.get(logFileName);
            if (!java.nio.file.Files.exists(logFile)) {
                java.nio.file.Files.createFile(logFile);
                System.out.println("DrawingController - ✅ Fichier de log créé pour le dessin: " + logFile.toAbsolutePath());
            }

            // Écrire un en-tête dans le fichier
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(logFileName, true))) {
                writer.write("=== FICHIER DE LOG AUTOMATIQUE POUR DESSIN ===\n");
                writer.write("=== Dessin: " + drawing.getName() + " ===\n");
                writer.write("=== Créé le: " + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ===\n");
                writer.write("=== Fichier: " + logFile.getFileName() + " ===\n");
                writer.write("\n");
                writer.flush();
            }

            logAction("Fichier de log automatique créé pour le dessin: " + logFileName);

        } catch (Exception e) {
            System.err.println("DrawingController - ❌ Erreur lors de la création du fichier de log automatique: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retourne le chemin du fichier de log du dessin actuel
     */
    public String getCurrentDrawingLogFile() {
        return currentDrawingLogFile;
    }

    // Ensure ShapeFactory enum is correctly defined and accessible
    // For example, if it's nested in model.shapes:
    // public enum ShapeType { RECTANGLE, CIRCLE, LINE /*, etc. */ }
}
