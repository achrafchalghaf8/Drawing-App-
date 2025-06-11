@echo off
echo ========================================
echo   LANCEMENT APPLICATION AVEC DEBUG
echo ========================================
echo.

set JAVA_PATH="C:\java\jdk-21.0.2\bin\java.exe"
set JAR_PATH="C:\Users\HP\Downloads\modelisation Proj\modelisation\modelisation\modelisation\modelisation\target\drawing-app-1.0-SNAPSHOT.jar"
set JAVAFX_PATH="C:\Users\HP\Downloads\modelisation Proj\modelisation\modelisation\modelisation\modelisation\javafx-sdk-19.0.2.1\lib"

echo 1. Verification des fichiers...
if exist %JAVA_PATH% (
    echo ✅ Java 21 trouvé
) else (
    echo ❌ Java 21 non trouvé
    pause
    exit /b 1
)

if exist %JAR_PATH% (
    echo ✅ JAR trouvé
) else (
    echo ❌ JAR non trouvé
    pause
    exit /b 1
)

if exist %JAVAFX_PATH% (
    echo ✅ JavaFX trouvé
) else (
    echo ❌ JavaFX non trouvé
    pause
    exit /b 1
)

echo.
echo 2. Lancement avec debug...
echo Répertoire de travail: %CD%
echo Commande: %JAVA_PATH% --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml -jar %JAR_PATH%
echo.

echo Lancement de l'application...
%JAVA_PATH% --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml -jar %JAR_PATH%

echo.
echo 3. Application fermée.
echo.
pause
