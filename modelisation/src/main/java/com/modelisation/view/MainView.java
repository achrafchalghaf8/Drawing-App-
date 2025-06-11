package com.modelisation.view;

import com.modelisation.controller.DrawingController;
import com.modelisation.model.Drawing;
import com.modelisation.model.logging.*;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Vue principale de l'application
 * Gère l'interface utilisateur principale et coordonne les différents composants
 */
public class MainView {
    
    private Stage primaryStage;
    private DrawingCanvas drawingCanvas;
    private ShapePalette shapePalette;
    private DrawingController controller;
    private MenuBar menuBar;
    private ToolBar toolBar;
    private StatusBar statusBar;
    
    // Composants de logging
    private ComboBox<String> loggingStrategyComboBox;
    private LoggingStrategy currentLogger;
    private String sessionLogFile; // Fichier de log pour cette session d'application
    
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // CRÉER LE FICHIER DE LOG DE SESSION AU DÉMARRAGE
        createSessionLogFile();

        initializeComponents();
        setupEventHandlers();
        setupController();

        primaryStage.setTitle("Application de Dessin - Formes Géométriques");
        primaryStage.setScene(new Scene(createMainLayout(), 1200, 800));
        primaryStage.show();

        // Initialiser le logger par défaut avec logging console
        setLoggingStrategy("Console");
    }
    
    /**
     * Initialise tous les composants de l'interface
     */
    private void initializeComponents() {
        // Canvas de dessin
        drawingCanvas = new DrawingCanvas(800, 600);
        
        // Palette d'outils
        shapePalette = new ShapePalette();
        
        // Barre de menu
        menuBar = createMenuBar();

        // ComboBox pour la stratégie de logging
        loggingStrategyComboBox = new ComboBox<>();
        loggingStrategyComboBox.getItems().addAll("Console", "Fichier", "Base de données");
        loggingStrategyComboBox.setValue("Console"); // Démarrer avec le logging console par défaut
        
        // Barre d'outils
        toolBar = createToolBar();
        
        // Barre de statut
        statusBar = new StatusBar();
    }
    
    /**
     * Configure la mise en page principale
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        
        // Menu en haut
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, toolBar);
        root.setTop(topContainer);
        
        // Canvas au centre
        ScrollPane scrollPane = new ScrollPane(drawingCanvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);
        
        // Palette à gauche
        root.setLeft(shapePalette);
        
        // Barre de statut en bas
        root.setBottom(statusBar);
        
        return root;
    }
    
    /**
     * Crée la barre de menu
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Menu Fichier
        Menu fileMenu = new Menu("Fichier");
        MenuItem newItem = new MenuItem("Nouveau");
        MenuItem openItem = new MenuItem("Ouvrir...");
        MenuItem saveItem = new MenuItem("Enregistrer");
        MenuItem saveAsItem = new MenuItem("Enregistrer sous...");
        MenuItem exitItem = new MenuItem("Quitter");
        
        fileMenu.getItems().addAll(newItem, new SeparatorMenuItem(), 
                                  openItem, new SeparatorMenuItem(),
                                  saveItem, saveAsItem, new SeparatorMenuItem(),
                                  exitItem);
        
        // Menu Édition
        Menu editMenu = new Menu("Édition");
        MenuItem clearItem = new MenuItem("Effacer tout");
        MenuItem undoItem = new MenuItem("Annuler");
        MenuItem redoItem = new MenuItem("Rétablir");
        
        editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(), clearItem);
        
        // Menu Outils
        Menu toolsMenu = new Menu("Outils");
        MenuItem statisticsItem = new MenuItem("Statistiques du dessin");
        MenuItem graphItem = new MenuItem("Outils de graphe");
        MenuItem shortestPathItem = new MenuItem("Plus court chemin");
        MenuItem algorithmSelectionItem = new MenuItem("Choisir algorithme...");

        toolsMenu.getItems().addAll(statisticsItem, graphItem, new SeparatorMenuItem(),
                                   shortestPathItem, algorithmSelectionItem);
        
        // Menu Aide
        Menu helpMenu = new Menu("Aide");
        MenuItem aboutItem = new MenuItem("À propos");
        
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, toolsMenu, helpMenu);
        
        return menuBar;
    }
    
    /**
     * Crée la barre d'outils
     */
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        
        // Boutons d'action rapide
        Button newButton = new Button("Nouveau");
        Button openButton = new Button("Ouvrir");
        Button saveButton = new Button("Enregistrer");
        
        Separator separator1 = new Separator(Orientation.VERTICAL);
        
        Button clearButton = new Button("Effacer");
        
        Separator separator2 = new Separator(Orientation.VERTICAL);
        
        // Sélecteur de stratégie de logging
        Label loggingLabel = new Label("Logging:");
        
        toolBar.getItems().addAll(
            newButton, openButton, saveButton,
            separator1,
            clearButton,
            separator2,
            loggingLabel, loggingStrategyComboBox
        );
        
        return toolBar;
    }
    
    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Événements de la palette
        shapePalette.setShapeSelectionListener(shapeType -> {
            drawingCanvas.setCurrentShapeType(shapeType);
            statusBar.setMessage("Forme sélectionnée: " + shapeType);
        });
        
        shapePalette.setColorChangeListener(color -> {
            drawingCanvas.setCurrentColor(color);
            statusBar.setMessage("Couleur changée: " + color);
        });
        
        shapePalette.setStrokeWidthChangeListener(strokeWidth -> {
            drawingCanvas.setCurrentStrokeWidth(strokeWidth);
            statusBar.setMessage("Épaisseur changée: " + strokeWidth);
        });
        shapePalette.setDimensionTypeSelectionListener(drawingCanvas::setCurrentDimensionType);
        
        // Événement de changement de stratégie de logging
        loggingStrategyComboBox.setOnAction(event -> {
            String selectedStrategy = loggingStrategyComboBox.getValue();
            setLoggingStrategy(selectedStrategy);
        });
        
        // Événement de fermeture de l'application
        primaryStage.setOnCloseRequest(event -> {
            if (currentLogger != null) {
                currentLogger.close();
            }
            Platform.exit();
        });
    }
    
    /**
     * Configure le contrôleur
     */
    private void setupController() {
        controller = new DrawingController(drawingCanvas.getDrawing(), this);
        drawingCanvas.setDrawingController(controller); // Set the controller on the canvas
        
        // Connecter les événements de menu au contrôleur
        connectMenuEvents();
    }
    
    /**
     * Connecte les événements de menu au contrôleur
     */
    private void connectMenuEvents() {
        // Les événements seront connectés quand le contrôleur sera complètement implémenté
        // Pour l'instant, on configure les actions de base
        
        // Menu Fichier
        menuBar.getMenus().get(0).getItems().get(0).setOnAction(e -> controller.newDrawing());
        menuBar.getMenus().get(0).getItems().get(2).setOnAction(e -> controller.openDrawing());
        menuBar.getMenus().get(0).getItems().get(4).setOnAction(e -> controller.saveDrawing());
        menuBar.getMenus().get(0).getItems().get(5).setOnAction(e -> controller.saveDrawingAs());
        menuBar.getMenus().get(0).getItems().get(7).setOnAction(e -> Platform.exit());
        
        // Menu Édition
        menuBar.getMenus().get(1).getItems().get(2).setOnAction(e -> controller.clearDrawing());
        
        // Barre d'outils
        ((Button) toolBar.getItems().get(0)).setOnAction(e -> controller.newDrawing());
        ((Button) toolBar.getItems().get(1)).setOnAction(e -> controller.openDrawing());
        ((Button) toolBar.getItems().get(2)).setOnAction(e -> controller.saveDrawing());
        ((Button) toolBar.getItems().get(4)).setOnAction(e -> controller.clearDrawing());

        // Menu Outils - Assuming "Outils" is the 3rd menu (index 2)
        if (menuBar.getMenus().size() > 2 && menuBar.getMenus().get(2).getItems().size() > 4) {
            // "Plus court chemin" is now at index 3 (after separator)
            menuBar.getMenus().get(2).getItems().get(3).setOnAction(e -> controller.toggleShortestPathMode());
            // "Choisir algorithme..." is at index 4
            menuBar.getMenus().get(2).getItems().get(4).setOnAction(e -> controller.showAlgorithmSelectionDialog());
        }
    }
    
    /**
     * Configure la stratégie de logging
     */
    private void setLoggingStrategy(String strategy) {
        System.out.println("MainView - Basculement vers logging: " + strategy);

        // Fermer l'ancien logger
        if (currentLogger != null) {
            currentLogger.close();
        }

        // Créer le nouveau logger
        switch (strategy) {
            case "Console":
                currentLogger = new ConsoleLogger();
                break;
            case "Fichier":
                // Utiliser le fichier de log de session créé au démarrage
                FileLogger fileLogger;
                if (sessionLogFile != null && !sessionLogFile.isEmpty()) {
                    // Utiliser le fichier de log de session
                    fileLogger = new FileLogger(sessionLogFile);
                    System.out.println("MainView - ✅ Utilisation du fichier de log de session: " + sessionLogFile);
                } else {
                    // Fallback : créer un nouveau fichier
                    fileLogger = new FileLogger();
                    System.out.println("MainView - ⚠️ Fallback: Nouveau FileLogger créé: " + fileLogger.getLogFilePath());
                }

                // Test immédiat du FileLogger
                fileLogger.log(LoggingStrategy.LogLevel.INFO, "=== BASCULEMENT VERS LOGGING FICHIER ===");
                fileLogger.log(LoggingStrategy.LogLevel.INFO, "Session d'application - Logging fichier activé");
                fileLogger.log(LoggingStrategy.LogLevel.INFO, "Fichier de session: " + (sessionLogFile != null ? sessionLogFile : "non défini"));

                currentLogger = fileLogger;
                break;
            case "Base de données":
                currentLogger = new DatabaseLoggingStrategy();
                break;
            default:
                currentLogger = new ConsoleLogger();
        }
        
        // Appliquer le logger au canvas et au modèle
        drawingCanvas.setLogger(currentLogger);

        // Appliquer le logger au Drawing lui-même
        if (drawingCanvas.getDrawing() != null) {
            drawingCanvas.getDrawing().setLogger(currentLogger);
        }

        // Logger le changement
        currentLogger.log(LoggingStrategy.LogLevel.INFO,
                         "Stratégie de logging changée: " + strategy);

        statusBar.setMessage("Logging: " + strategy);
    }
    
    // Getters pour le contrôleur
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public DrawingCanvas getDrawingCanvas() {
        return drawingCanvas;
    }
    
    public StatusBar getStatusBar() {
        return statusBar;
    }
    
    public LoggingStrategy getCurrentLogger() {
        return currentLogger;
    }

    /**
     * Crée le fichier de log de session au démarrage de l'application
     */
    private void createSessionLogFile() {
        System.out.println("MainView - DÉBUT createSessionLogFile()");
        try {
            System.out.println("MainView - Génération du timestamp...");
            // Générer un nom de fichier unique pour cette session
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
            );
            System.out.println("MainView - Timestamp généré: " + timestamp);

            // Créer le nom du fichier de log de session
            sessionLogFile = "logs/session_" + timestamp + ".log";
            System.out.println("MainView - Nom du fichier de session: " + sessionLogFile);

            // Créer le répertoire logs s'il n'existe pas
            System.out.println("MainView - Vérification du répertoire logs...");
            java.nio.file.Path logDir = java.nio.file.Paths.get("logs");
            System.out.println("MainView - Chemin du répertoire: " + logDir.toAbsolutePath());

            if (!java.nio.file.Files.exists(logDir)) {
                System.out.println("MainView - Répertoire logs n'existe pas, création...");
                java.nio.file.Files.createDirectories(logDir);
                System.out.println("MainView - ✅ Répertoire logs créé: " + logDir.toAbsolutePath());
            } else {
                System.out.println("MainView - Répertoire logs existe déjà: " + logDir.toAbsolutePath());
            }

            // Créer le fichier de log de session
            System.out.println("MainView - Création du fichier de session...");
            java.nio.file.Path logFile = java.nio.file.Paths.get(sessionLogFile);
            System.out.println("MainView - Chemin complet du fichier: " + logFile.toAbsolutePath());

            if (!java.nio.file.Files.exists(logFile)) {
                System.out.println("MainView - Fichier n'existe pas, création...");
                java.nio.file.Files.createFile(logFile);
                System.out.println("MainView - ✅ Fichier de log de session créé: " + logFile.toAbsolutePath());
            } else {
                System.out.println("MainView - ⚠️ Fichier existe déjà: " + logFile.toAbsolutePath());
            }

            // Écrire un en-tête dans le fichier
            System.out.println("MainView - Écriture de l'en-tête...");
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(sessionLogFile, true))) {
                writer.write("=== FICHIER DE LOG DE SESSION ===\n");
                writer.write("=== Application de Dessin - Formes Géométriques ===\n");
                writer.write("=== Session démarrée le: " + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ===\n");
                writer.write("=== Fichier: " + logFile.getFileName() + " ===\n");
                writer.write("=== Répertoire: " + logFile.getParent().toAbsolutePath() + " ===\n");
                writer.write("\n");
                writer.flush();
                System.out.println("MainView - En-tête écrit avec succès");
            }

            System.out.println("MainView - ✅ Fichier de log de session prêt: " + sessionLogFile);

        } catch (Exception e) {
            System.err.println("MainView - ❌ ERREUR lors de la création du fichier de log de session: " + e.getMessage());
            System.err.println("MainView - Type d'erreur: " + e.getClass().getSimpleName());
            e.printStackTrace();
            sessionLogFile = null; // Réinitialiser en cas d'erreur
        }
        System.out.println("MainView - FIN createSessionLogFile() - sessionLogFile = " + sessionLogFile);
    }
    
    /**
     * Classe interne pour la barre de statut
     */
    public static class StatusBar extends ToolBar {
        private Label messageLabel;
        
        public StatusBar() {
            messageLabel = new Label("Prêt");
            getItems().add(messageLabel);
            setStyle("-fx-background-color: #f0f0f0;");
        }
        
        public void setMessage(String message) {
            messageLabel.setText(message);
        }
    }
}
