@echo off
echo ========================================
echo   CAPTURE DES LOGS DE DEBUG
echo ========================================
echo.

echo Lancement de l'application avec capture des logs...
echo Les logs de debug s'afficheront ici en temps réel.
echo.

"C:\java\jdk-21.0.2\bin\java.exe" --module-path "C:\Users\HP\Downloads\modelisation Proj\modelisation\modelisation\modelisation\modelisation\javafx-sdk-19.0.2.1\lib" --add-modules javafx.controls,javafx.fxml -jar "C:\Users\HP\Downloads\modelisation Proj\modelisation\modelisation\modelisation\modelisation\target\drawing-app-1.0-SNAPSHOT.jar"

echo.
echo Application fermée.
pause
