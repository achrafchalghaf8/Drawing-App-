@echo off
echo ========================================
echo   LANCEMENT APPLICATION MODELISATION
echo ========================================
echo.

REM Configuration Java 22
set JAVA_HOME=C:\Program Files\Java\jdk-22
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

echo Verification de Java...
if not exist "%JAVA_EXE%" (
    echo ERREUR: Java 22 non trouve dans %JAVA_HOME%
    echo Tentative avec Java par defaut...
    set JAVA_EXE=java
)

echo Java utilise: %JAVA_EXE%
"%JAVA_EXE%" -version
echo.

echo Verification du JAR...
if not exist "target\drawing-app-1.0-SNAPSHOT.jar" (
    echo ERREUR: JAR non trouve. Compilation necessaire.
    echo Lancement de la compilation...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ERREUR: Compilation echouee
        pause
        exit /b 1
    )
)

echo.
echo Lancement de l'application JavaFX...
echo ========================================
"%JAVA_EXE%" -jar target\drawing-app-1.0-SNAPSHOT.jar 2>&1

echo.
echo ========================================
echo Application fermee. Code: %ERRORLEVEL%
echo ========================================
pause
