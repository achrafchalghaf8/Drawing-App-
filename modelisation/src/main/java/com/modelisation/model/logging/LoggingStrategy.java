package com.modelisation.model.logging;

/**
 * Interface Strategy pour les différentes stratégies de journalisation
 * Permet de changer dynamiquement la méthode de logging
 */
public interface LoggingStrategy {
    
    /**
     * Enregistre un message de log
     * @param level Niveau de log (INFO, WARNING, ERROR, etc.)
     * @param message Message à enregistrer
     */
    void log(LogLevel level, String message);
    
    /**
     * Enregistre un message de log avec une exception
     * @param level Niveau de log
     * @param message Message à enregistrer
     * @param throwable Exception associée
     */
    void log(LogLevel level, String message, Throwable throwable);
    
    /**
     * Ferme les ressources utilisées par le logger (fichiers, connexions DB, etc.)
     */
    void close();
    
    /**
     * Énumération des niveaux de log
     */
    enum LogLevel {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR");
        
        private final String label;
        
        LogLevel(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
}
