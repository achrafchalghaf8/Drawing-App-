@echo off
echo ========================================
echo   LANCEMENT APPLICATION MODELISATION
echo ========================================
echo.

echo Verification de Java 21...
"C:\java\jdk-21.0.2\bin\java.exe" -version
echo.

echo Verification du JAR...
if exist "target\drawing-app-1.0-SNAPSHOT.jar" (
    echo JAR trouve: target\drawing-app-1.0-SNAPSHOT.jar
) else (
    echo ERREUR: JAR non trouve
    pause
    exit /b 1
)

echo.
echo Lancement de l'application JavaFX...
echo ========================================
"C:\java\jdk-21.0.2\bin\java.exe" -jar target\drawing-app-1.0-SNAPSHOT.jar

echo.
echo ========================================
echo Application fermee. Code: %ERRORLEVEL%
echo ========================================
pause
