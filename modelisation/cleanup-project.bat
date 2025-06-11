@echo off
echo ========================================
echo    NETTOYAGE DU PROJET MODELISATION
echo ========================================
echo.
echo Ce script va supprimer les fichiers non utiles...
echo.
pause

echo Suppression de la documentation redondante...
del /q "DEPLOYMENT-GUIDE.md" 2>nul
del /q "EXECUTION-GUIDE.md" 2>nul
del /q "GUIDE-INSTALLATION-RAPIDE.md" 2>nul
del /q "INSTALLATION.md" 2>nul
del /q "PROJECT-SUMMARY.md" 2>nul
del /q "QUICK-START.md" 2>nul

echo Suppression des scripts redondants...
del /q "check-environment.bat" 2>nul
del /q "compile-final.bat" 2>nul
del /q "compile.bat" 2>nul
del /q "compile.sh" 2>nul
del /q "compiler-avec-javafx.bat" 2>nul
del /q "configure-environment.bat" 2>nul
del /q "diagnostic-app.bat" 2>nul
del /q "executer-app.bat" 2>nul
del /q "explorer-javafx.bat" 2>nul
del /q "installer-et-lancer.bat" 2>nul
del /q "lancer-app-final.bat" 2>nul
del /q "lancer-app-maintenant.bat" 2>nul
del /q "lancer-app-simple.bat" 2>nul
del /q "lancer-application.bat" 2>nul
del /q "quick-start.bat" 2>nul
del /q "relancer-app.bat" 2>nul
del /q "run-final.bat" 2>nul
del /q "run.bat" 2>nul
del /q "setup.bat" 2>nul
del /q "simple-check.bat" 2>nul
del /q "verifier-installation.bat" 2>nul

echo Suppression des fichiers temporaires...
del /q "compilation_log.txt" 2>nul
del /q "maven_debug_output.txt" 2>nul
del /q "log_file.txt" 2>nul
del /q "drawing_app.db" 2>nul
del /q "config.properties" 2>nul
del /q "database_schema.sql" 2>nul
del /q "Modelisation.iml" 2>nul
del /q "dependency-reduced-pom.xml" 2>nul
del /q "src.zip" 2>nul

echo Suppression des dossiers obsoletes...
rmdir /s /q "lib" 2>nul
rmdir /s /q "logs" 2>nul
rmdir /s /q "out" 2>nul
rmdir /s /q "src\Logger" 2>nul
rmdir /s /q "src\ShapeFactory" 2>nul
rmdir /s /q "src\Zone" 2>nul
del /q "src\Main.java" 2>nul

echo.
echo ========================================
echo         NETTOYAGE TERMINE !
echo ========================================
echo.
echo Fichiers conserves :
echo - pom.xml (configuration Maven)
echo - src/main/ (code source principal)
echo - src/test/ (tests)
echo - target/ (JAR compile)
echo - javafx-sdk-19.0.2.1/ (JavaFX)
echo - install-java.ps1 et install.ps1 (installation)
echo.
echo Le projet est maintenant propre et optimise !
echo.
pause
