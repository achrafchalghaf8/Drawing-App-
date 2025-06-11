package com.modelisation.view;

import com.modelisation.model.shapes.ShapeFactory;
import com.modelisation.view.DimensionType;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Palette d'outils pour sélectionner les formes et leurs propriétés
 * Interface utilisateur pour la sélection des outils de dessin
 */
public class ShapePalette extends VBox {
    
    private ToggleGroup shapeToggleGroup;
    private ColorPicker colorPicker;
    private Slider strokeWidthSlider;
    private Label strokeWidthLabel;
    private ToggleGroup dimensionToggleGroup;
    private RadioButton d2Button;
    private RadioButton d3Button;
    
    // Callbacks pour notifier les changements
    private ShapeSelectionListener shapeSelectionListener;
    private ColorChangeListener colorChangeListener;
    private StrokeWidthChangeListener strokeWidthChangeListener;
    private DimensionTypeSelectionListener dimensionTypeSelectionListener;
    
    public ShapePalette() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * Initialise les composants de la palette
     */
    private void initializeComponents() {
        // Groupe de boutons radio pour les formes
        shapeToggleGroup = new ToggleGroup();
        
        // Sélecteur de couleur
        colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setPrefWidth(150);
        
        // Slider pour l'épaisseur du trait
        strokeWidthSlider = new Slider(1, 10, 2);
        strokeWidthSlider.setShowTickLabels(true);
        strokeWidthSlider.setShowTickMarks(true);
        strokeWidthSlider.setMajorTickUnit(2);
        strokeWidthSlider.setMinorTickCount(1);
        strokeWidthSlider.setSnapToTicks(true);
        
        strokeWidthLabel = new Label("Épaisseur: 2.0");

        // Groupe de boutons radio pour la dimension (2D/3D)
        dimensionToggleGroup = new ToggleGroup();
        d2Button = new RadioButton("2D");
        d2Button.setToggleGroup(dimensionToggleGroup);
        d2Button.setUserData(DimensionType.D2);
        d2Button.setSelected(true); // 2D par défaut

        d3Button = new RadioButton("3D");
        d3Button.setToggleGroup(dimensionToggleGroup);
        d3Button.setUserData(DimensionType.D3);
    }
    
    /**
     * Configure la mise en page de la palette
     */
    private void setupLayout() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        // Titre
        Label title = new Label("Palette d'outils");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Section Dimension
        Label dimensionLabel = new Label("Dimension:");
        dimensionLabel.setStyle("-fx-font-weight: bold;");

        // Section formes
        Label shapesLabel = new Label("Formes:");
        shapesLabel.setStyle("-fx-font-weight: bold;");
        
        RadioButton rectangleButton = new RadioButton("Rectangle");
        rectangleButton.setToggleGroup(shapeToggleGroup);
        rectangleButton.setUserData(ShapeFactory.ShapeType.RECTANGLE);
        rectangleButton.setSelected(true); // Sélectionné par défaut
        
        RadioButton circleButton = new RadioButton("Cercle");
        circleButton.setToggleGroup(shapeToggleGroup);
        circleButton.setUserData(ShapeFactory.ShapeType.CIRCLE);
        
        RadioButton lineButton = new RadioButton("Ligne");
        lineButton.setToggleGroup(shapeToggleGroup);
        lineButton.setUserData(ShapeFactory.ShapeType.LINE);
        
        // Section couleur
        Label colorLabel = new Label("Couleur:");
        colorLabel.setStyle("-fx-font-weight: bold;");
        
        // Section épaisseur
        Label strokeLabel = new Label("Épaisseur du trait:");
        strokeLabel.setStyle("-fx-font-weight: bold;");
        
        // Séparateurs
        Separator separator1 = new Separator();
        Separator separator2 = new Separator();
        Separator separator3 = new Separator();
        
        // Ajouter tous les composants
        getChildren().addAll(
            title,
            separator1,
            shapesLabel,
            rectangleButton,
            circleButton,
            lineButton,
            separator2,
            dimensionLabel,
            d2Button,
            d3Button,
            new Separator(), // Additional separator before color
            colorLabel,
            colorPicker,
            separator3,
            strokeLabel,
            strokeWidthLabel,
            strokeWidthSlider
        );
    }
    
    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Gestionnaire pour le changement de forme
        shapeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && shapeSelectionListener != null) {
                ShapeFactory.ShapeType selectedType = (ShapeFactory.ShapeType) newValue.getUserData();
                shapeSelectionListener.onShapeSelected(selectedType);
            }
        });
        
        // Gestionnaire pour le changement de couleur
        colorPicker.setOnAction(event -> {
            if (colorChangeListener != null) {
                colorChangeListener.onColorChanged(colorPicker.getValue());
            }
        });
        
        // Gestionnaire pour le changement d'épaisseur
        strokeWidthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double strokeWidth = newValue.doubleValue();
            strokeWidthLabel.setText(String.format("Épaisseur: %.1f", strokeWidth));
            
            if (strokeWidthChangeListener != null) {
                strokeWidthChangeListener.onStrokeWidthChanged(strokeWidth);
            }
        });

        // Gestionnaire pour le changement de dimension
        dimensionToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && dimensionTypeSelectionListener != null) {
                DimensionType selectedDimension = (DimensionType) newValue.getUserData();
                dimensionTypeSelectionListener.onDimensionTypeSelected(selectedDimension);
            }
        });
    }
    
    /**
     * Obtient le type de forme actuellement sélectionné
     */
    public ShapeFactory.ShapeType getSelectedShapeType() {
        Toggle selectedToggle = shapeToggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            return (ShapeFactory.ShapeType) selectedToggle.getUserData();
        }
        return ShapeFactory.ShapeType.RECTANGLE; // Par défaut
    }

    /**
     * Obtient le type de dimension actuellement sélectionné (2D ou 3D)
     */
    public DimensionType getSelectedDimensionType() {
        Toggle selectedToggle = dimensionToggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            return (DimensionType) selectedToggle.getUserData();
        }
        return DimensionType.D2; // Par défaut 2D
    }
    
    /**
     * Obtient la couleur actuellement sélectionnée
     */
    public Color getSelectedColor() {
        return colorPicker.getValue();
    }
    
    /**
     * Obtient l'épaisseur de trait actuellement sélectionnée
     */
    public double getSelectedStrokeWidth() {
        return strokeWidthSlider.getValue();
    }
    
    // Setters pour les listeners
    public void setShapeSelectionListener(ShapeSelectionListener listener) {
        this.shapeSelectionListener = listener;
    }
    
    public void setColorChangeListener(ColorChangeListener listener) {
        this.colorChangeListener = listener;
    }
    
    public void setStrokeWidthChangeListener(StrokeWidthChangeListener listener) {
        this.strokeWidthChangeListener = listener;
    }

    public void setDimensionTypeSelectionListener(DimensionTypeSelectionListener listener) {
        this.dimensionTypeSelectionListener = listener;
    }
    
    // Interfaces pour les callbacks
    @FunctionalInterface
    public interface ShapeSelectionListener {
        void onShapeSelected(ShapeFactory.ShapeType shapeType);
    }
    
    @FunctionalInterface
    public interface ColorChangeListener {
        void onColorChanged(Color color);
    }
    
    @FunctionalInterface
    public interface StrokeWidthChangeListener {
        void onStrokeWidthChanged(double strokeWidth);
    }

    @FunctionalInterface
    public interface DimensionTypeSelectionListener {
        void onDimensionTypeSelected(DimensionType dimensionType);
    }
}
