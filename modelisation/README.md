# Application de Dessin de Formes Géométriques

Une application JavaFX moderne pour dessiner des formes géométriques avec une architecture basée sur des design patterns pour une meilleure modularité, extensibilité et maintenance du code.

## 🎯 Fonctionnalités

### Dessin de Formes
- **Sélection de formes** : Rectangle, Cercle, Ligne
- **Palette d'outils** : Sélection de couleurs et épaisseur de trait
- **Dessin interactif** : Cliquer-glisser pour créer des formes
- **Prévisualisation** : Aperçu en temps réel pendant le dessin
- **Sélection et suppression** : Double-clic pour supprimer une forme

### Gestion des Fichiers
- **Nouveau dessin** : Créer un nouveau dessin vide
- **Sauvegarde** : Enregistrer dans un fichier JSON ou en base de données
- **Ouverture** : Charger un dessin depuis un fichier ou la base de données
- **Effacement** : Vider complètement le dessin

### Journalisation (Logging)
Trois stratégies de journalisation disponibles :
- **Console** : Affichage dans la console de l'application
- **Fichier** : Enregistrement dans un fichier de log horodaté
- **Base de données** : Stockage des logs en base de données SQLite

### Algorithmes de Graphe
- **Création de graphes** : Dessiner des nœuds et des arêtes
- **Algorithmes de plus court chemin** :
  - **Dijkstra** : Pour graphes avec poids positifs
  - **BFS** : Pour graphes non pondérés ou analyse de connectivité
- **Visualisation** : Mise en évidence des chemins trouvés

## 🏗️ Architecture et Design Patterns

### Design Patterns Utilisés

1. **Factory Pattern** (`ShapeFactory`)
   - Création centralisée des différents types de formes
   - Facilite l'ajout de nouveaux types de formes

2. **Strategy Pattern** 
   - `LoggingStrategy` : Différentes stratégies de journalisation
   - `ShortestPathStrategy` : Différents algorithmes de plus court chemin

3. **Observer Pattern** (`Drawing`)
   - Notification automatique des changements dans le modèle
   - Mise à jour de la vue en temps réel

4. **Singleton Pattern** (`DatabaseManager`)
   - Instance unique pour la gestion de la base de données
   - Contrôle centralisé des connexions

5. **MVC Pattern**
   - **Model** : `Drawing`, `Shape`, `Graph`, etc.
   - **View** : `MainView`, `DrawingCanvas`, `ShapePalette`
   - **Controller** : `DrawingController`

6. **Template Method Pattern** (`Shape`)
   - Structure commune pour toutes les formes géométriques
   - Méthodes abstraites pour les spécificités de chaque forme

### Structure du Projet

```
src/main/java/com/modelisation/
├── Main.java                          # Point d'entrée de l'application
├── controller/
│   └── DrawingController.java         # Contrôleur principal (MVC)
├── model/
│   ├── Drawing.java                   # Modèle principal du dessin
│   ├── shapes/                        # Package des formes géométriques
│   │   ├── Shape.java                 # Classe abstraite de base
│   │   ├── Rectangle.java             # Implémentation Rectangle
│   │   ├── Circle.java                # Implémentation Cercle
│   │   ├── Line.java                  # Implémentation Ligne
│   │   └── ShapeFactory.java          # Factory pour créer les formes
│   ├── graph/                         # Package pour les graphes
│   │   ├── Graph.java                 # Modèle de graphe
│   │   ├── Node.java                  # Nœud de graphe
│   │   ├── Edge.java                  # Arête de graphe
│   │   └── algorithms/                # Algorithmes de graphe
│   │       ├── ShortestPathStrategy.java
│   │       ├── DijkstraAlgorithm.java
│   │       └── BFSAlgorithm.java
│   ├── logging/                       # Système de journalisation
│   │   ├── LoggingStrategy.java       # Interface Strategy
│   │   ├── ConsoleLogger.java         # Logger console
│   │   ├── FileLogger.java            # Logger fichier
│   │   └── DatabaseLogger.java        # Logger base de données
│   └── database/
│       └── DatabaseManager.java       # Gestionnaire BDD (Singleton)
└── view/                              # Interface utilisateur
    ├── MainView.java                  # Vue principale
    ├── DrawingCanvas.java             # Zone de dessin
    └── ShapePalette.java              # Palette d'outils
```

## 🚀 Installation et Exécution

### Prérequis
- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- JavaFX 19

### Installation
1. Cloner le projet :
```bash
git clone <url-du-projet>
cd modelisation
```

2. Compiler le projet :
```bash
mvn clean compile
```

3. Exécuter l'application :
```bash
mvn javafx:run
```

### Compilation d'un JAR exécutable
```bash
mvn clean package
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/drawing-app-1.0-SNAPSHOT.jar
```

## 📊 Base de Données

L'application utilise SQLite pour le stockage persistant :

### Tables créées automatiquement :
- **drawings** : Stockage des dessins (nom, description, données JSON)
- **logs** : Journalisation des actions utilisateur

### Localisation :
- Fichier de base de données : `drawing_app.db` (créé automatiquement)
- Fichiers de log : `logs/drawing_app_YYYYMMDD_HHMMSS.log`

## 🎮 Utilisation

### Dessiner des Formes
1. Sélectionner un type de forme dans la palette
2. Choisir une couleur et une épaisseur de trait
3. Cliquer-glisser sur la zone de dessin pour créer la forme
4. Double-cliquer sur une forme pour la supprimer

### Gestion des Fichiers
- **Fichier > Nouveau** : Créer un nouveau dessin
- **Fichier > Ouvrir** : Charger un dessin existant
- **Fichier > Enregistrer** : Sauvegarder le dessin actuel
- **Édition > Effacer tout** : Vider le dessin

### Journalisation
- Utiliser la liste déroulante dans la barre d'outils pour changer la stratégie de logging
- Les actions sont automatiquement enregistrées selon la stratégie choisie

### Algorithmes de Graphe
1. Dessiner des cercles pour créer des nœuds
2. Dessiner des lignes entre les nœuds pour créer des arêtes
3. Utiliser **Outils > Outils de graphe** pour accéder aux algorithmes
4. Sélectionner deux nœuds pour calculer le plus court chemin

## 🧪 Tests

Exécuter les tests unitaires :
```bash
mvn test
```

## 🔧 Extension

### Ajouter une Nouvelle Forme
1. Créer une classe héritant de `Shape`
2. Implémenter les méthodes abstraites
3. Ajouter le type dans `ShapeFactory.ShapeType`
4. Mettre à jour `ShapeFactory.createShape()`
5. Ajouter l'option dans `ShapePalette`

### Ajouter un Nouvel Algorithme
1. Implémenter `ShortestPathStrategy`
2. Ajouter la logique spécifique de l'algorithme
3. Intégrer dans l'interface utilisateur

### Ajouter une Nouvelle Stratégie de Logging
1. Implémenter `LoggingStrategy`
2. Ajouter dans les options de `MainView`

## 📝 Licence

Ce projet est développé dans un cadre éducatif pour démontrer l'utilisation des design patterns en Java.

## 👥 Contribution

Les contributions sont les bienvenues ! Veuillez suivre les bonnes pratiques :
- Respecter l'architecture existante
- Utiliser les design patterns appropriés
- Documenter le code
- Ajouter des tests pour les nouvelles fonctionnalités

## 🐛 Problèmes Connus

- La sérialisation/désérialisation JSON des dessins n'est pas encore implémentée
- L'interface pour les algorithmes de graphe est basique
- Pas de fonction d'annulation (undo/redo) pour l'instant

## 📚 Ressources

- [Documentation JavaFX](https://openjfx.io/)
- [Design Patterns](https://refactoring.guru/design-patterns)
- [Maven](https://maven.apache.org/)
- [SQLite](https://www.sqlite.org/)
