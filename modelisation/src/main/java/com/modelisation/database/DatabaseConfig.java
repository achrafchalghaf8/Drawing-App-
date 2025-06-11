package com.modelisation.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration de la base de données MySQL
 */
public class DatabaseConfig {
    
    private static final String CONFIG_FILE = "/database.properties";
    private static DatabaseConfig instance;
    private Properties properties;
    
    // Valeurs par défaut
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "drawing-app";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    
    private DatabaseConfig() {
        loadProperties();
    }
    
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        
        // Charger depuis le fichier de configuration s'il existe
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("Configuration de base de données chargée depuis " + CONFIG_FILE);
            } else {
                System.out.println("Fichier de configuration non trouvé, utilisation des valeurs par défaut");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la configuration: " + e.getMessage());
        }
        
        // Définir les valeurs par défaut si elles ne sont pas présentes
        setDefaultIfMissing("db.host", DEFAULT_HOST);
        setDefaultIfMissing("db.port", DEFAULT_PORT);
        setDefaultIfMissing("db.database", DEFAULT_DATABASE);
        setDefaultIfMissing("db.username", DEFAULT_USERNAME);
        setDefaultIfMissing("db.password", DEFAULT_PASSWORD);
    }
    
    private void setDefaultIfMissing(String key, String defaultValue) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, defaultValue);
        }
    }
    
    public String getHost() {
        return properties.getProperty("db.host");
    }
    
    public String getPort() {
        return properties.getProperty("db.port");
    }
    
    public String getDatabase() {
        return properties.getProperty("db.database");
    }
    
    public String getUsername() {
        return properties.getProperty("db.username");
    }
    
    public String getPassword() {
        return properties.getProperty("db.password");
    }
    
    public String getJdbcUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                getHost(), getPort(), getDatabase());
    }
    
    public void printConfiguration() {
        System.out.println("=== Configuration Base de Données ===");
        System.out.println("Host: " + getHost());
        System.out.println("Port: " + getPort());
        System.out.println("Database: " + getDatabase());
        System.out.println("Username: " + getUsername());
        System.out.println("Password: " + (getPassword().isEmpty() ? "(vide)" : "***"));
        System.out.println("JDBC URL: " + getJdbcUrl());
        System.out.println("=====================================");
    }
}
