@echo off
echo Lancement de l'application JavaFX...
echo.

REM Utiliser Java 22
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%

echo Java version:
java -version
echo.

echo Lancement de l'application...
java -jar target/drawing-app-1.0-SNAPSHOT.jar

echo.
echo Application fermee. Code de retour: %ERRORLEVEL%
pause
