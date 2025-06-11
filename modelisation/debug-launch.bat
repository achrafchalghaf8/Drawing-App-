@echo off
echo ========================================
echo   DIAGNOSTIC ET LANCEMENT APPLICATION
echo ========================================

REM Configuration
set JAVA_HOME=C:\Program Files\Java\jdk-22
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

echo 1. Verification de Java...
if exist "%JAVA_EXE%" (
    echo ✅ Java 22 trouve: %JAVA_EXE%
    "%JAVA_EXE%" -version
) else (
    echo ❌ Java 22 non trouve, utilisation de java par defaut
    set JAVA_EXE=java
    java -version
)

echo.
echo 2. Verification du JAR...
if exist "target\drawing-app-1.0-SNAPSHOT.jar" (
    echo ✅ JAR trouve: target\drawing-app-1.0-SNAPSHOT.jar
    dir target\drawing-app-1.0-SNAPSHOT.jar
) else (
    echo ❌ JAR non trouve
    echo Contenu du dossier target:
    dir target\
    goto :end
)

echo.
echo 3. Verification des classes...
if exist "target\classes\com\modelisation\Main.class" (
    echo ✅ Classe Main trouvee
) else (
    echo ❌ Classe Main non trouvee
    echo Contenu de target\classes:
    dir target\classes\ /s
)

echo.
echo 4. Test de lancement avec logs detailles...
echo ========================================
"%JAVA_EXE%" -Djavafx.verbose=true -Dprism.verbose=true -jar target\drawing-app-1.0-SNAPSHOT.jar > launch.log 2>&1

echo.
echo 5. Affichage des logs...
echo ========================================
if exist "launch.log" (
    type launch.log
) else (
    echo Aucun fichier de log cree
)

:end
echo.
echo ========================================
echo Diagnostic termine
echo ========================================
pause
