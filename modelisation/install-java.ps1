# Script PowerShell pour installer Java et JavaFX automatiquement
# Exécuter en tant qu'administrateur si possible

Write-Host "=== Installation Java et JavaFX ===" -ForegroundColor Green
Write-Host ""

# Fonction pour télécharger un fichier
function Download-File {
    param($url, $output)
    try {
        Write-Host "Téléchargement de $output..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
        return $true
    }
    catch {
        Write-Host "Erreur: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 1. Vérifier si Java est déjà installé
Write-Host "1. Vérification de Java..." -ForegroundColor Cyan
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion) {
        Write-Host "✅ Java déjà installé: $javaVersion" -ForegroundColor Green
        $javaInstalled = $true
    }
}
catch {
    Write-Host "❌ Java non trouvé" -ForegroundColor Red
    $javaInstalled = $false
}

# 2. Installer Java si nécessaire
if (-not $javaInstalled) {
    Write-Host "2. Installation de Java 17..." -ForegroundColor Cyan
    
    # Télécharger Java 17 (version portable)
    $javaUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip"
    $javaZip = "java17.zip"
    $javaDir = "C:\java-17"
    
    if (Download-File $javaUrl $javaZip) {
        Write-Host "Extraction de Java..." -ForegroundColor Yellow
        
        # Créer le répertoire de destination
        if (Test-Path $javaDir) {
            Remove-Item $javaDir -Recurse -Force
        }
        
        # Extraire
        Expand-Archive -Path $javaZip -DestinationPath "C:\" -Force
        
        # Renommer le dossier extrait
        $extractedDir = Get-ChildItem "C:\jdk-17*" | Select-Object -First 1
        if ($extractedDir) {
            Rename-Item $extractedDir.FullName $javaDir -Force
        }
        
        # Ajouter au PATH utilisateur
        $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
        if ($currentPath -notlike "*$javaDir\bin*") {
            [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$javaDir\bin", "User")
            $env:PATH += ";$javaDir\bin"
        }
        
        # Définir JAVA_HOME
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaDir, "User")
        $env:JAVA_HOME = $javaDir
        
        Write-Host "✅ Java installé dans $javaDir" -ForegroundColor Green
        Remove-Item $javaZip -Force -ErrorAction SilentlyContinue
    }
    else {
        Write-Host "❌ Échec du téléchargement de Java" -ForegroundColor Red
        exit 1
    }
}

# 3. Installer JavaFX
Write-Host "3. Installation de JavaFX..." -ForegroundColor Cyan
$javafxDir = "C:\javafx-19"

if (-not (Test-Path "$javafxDir\lib\javafx.controls.jar")) {
    $javafxUrl = "https://download2.gluonhq.com/openjfx/19.0.2.1/openjfx-19.0.2.1_windows-x64_bin-sdk.zip"
    $javafxZip = "javafx.zip"
    
    if (Download-File $javafxUrl $javafxZip) {
        Write-Host "Extraction de JavaFX..." -ForegroundColor Yellow
        
        # Supprimer l'ancien répertoire s'il existe
        if (Test-Path $javafxDir) {
            Remove-Item $javafxDir -Recurse -Force
        }
        
        # Extraire
        Expand-Archive -Path $javafxZip -DestinationPath "C:\" -Force
        
        # Renommer le dossier extrait
        $extractedDir = Get-ChildItem "C:\javafx-*" | Select-Object -First 1
        if ($extractedDir) {
            Rename-Item $extractedDir.FullName $javafxDir -Force
        }
        
        Write-Host "✅ JavaFX installé dans $javafxDir" -ForegroundColor Green
        Remove-Item $javafxZip -Force -ErrorAction SilentlyContinue
    }
    else {
        Write-Host "❌ Échec du téléchargement de JavaFX" -ForegroundColor Red
        exit 1
    }
}
else {
    Write-Host "✅ JavaFX déjà installé" -ForegroundColor Green
}

# 4. Créer les répertoires du projet
Write-Host "4. Préparation du projet..." -ForegroundColor Cyan
$directories = @("target", "target\classes", "lib", "logs")
foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}
Write-Host "✅ Répertoires créés" -ForegroundColor Green

# 5. Télécharger les dépendances
Write-Host "5. Téléchargement des dépendances..." -ForegroundColor Cyan

$dependencies = @{
    "sqlite-jdbc-3.42.0.0.jar" = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar"
    "jackson-core-2.15.2.jar" = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar"
    "jackson-databind-2.15.2.jar" = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar"
    "jackson-annotations-2.15.2.jar" = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar"
}

foreach ($dep in $dependencies.GetEnumerator()) {
    $filePath = "lib\$($dep.Key)"
    if (-not (Test-Path $filePath)) {
        if (Download-File $dep.Value $filePath) {
            Write-Host "  ✅ $($dep.Key)" -ForegroundColor Gray
        }
        else {
            Write-Host "  ❌ $($dep.Key)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "  ✅ $($dep.Key) (déjà présent)" -ForegroundColor Gray
    }
}

# 6. Créer les scripts de compilation et lancement
Write-Host "6. Création des scripts..." -ForegroundColor Cyan

# Script de compilation
$compileScript = @"
@echo off
echo Compilation de l'application...

REM Copier les ressources
if exist "src\main\resources" (
    xcopy "src\main\resources\*" "target\classes\" /E /Y /Q >nul 2>&1
)

REM Compiler
javac -d target\classes -cp "lib\*" -sourcepath src\main\java src\main\java\com\modelisation\Main.java src\main\java\com\modelisation\controller\*.java src\main\java\com\modelisation\model\*.java src\main\java\com\modelisation\model\shapes\*.java src\main\java\com\modelisation\model\logging\*.java src\main\java\com\modelisation\model\database\*.java src\main\java\com\modelisation\model\graph\*.java src\main\java\com\modelisation\model\graph\algorithms\*.java src\main\java\com\modelisation\view\*.java

if errorlevel 1 (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
) else (
    echo ✅ Compilation réussie
)
"@

$compileScript | Out-File -FilePath "compile-now.bat" -Encoding ASCII

# Script de lancement
$runScript = @"
@echo off
echo Lancement de l'Application de Dessin JavaFX...

if not exist "target\classes\com\modelisation\Main.class" (
    echo Compilation nécessaire...
    call compile-now.bat
    if errorlevel 1 exit /b 1
)

echo Démarrage de l'application...
java --module-path "C:\javafx-19\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;lib\*" com.modelisation.Main

if errorlevel 1 (
    echo ❌ Erreur lors du lancement
    pause
)
"@

$runScript | Out-File -FilePath "run-now.bat" -Encoding ASCII

Write-Host "✅ Scripts créés: compile-now.bat et run-now.bat" -ForegroundColor Green

# 7. Test final
Write-Host "7. Test de l'installation..." -ForegroundColor Cyan

# Tester Java
try {
    $javaTest = java -version 2>&1
    Write-Host "✅ Java fonctionne" -ForegroundColor Green
}
catch {
    Write-Host "❌ Java ne fonctionne pas" -ForegroundColor Red
}

# Tester JavaFX
if (Test-Path "C:\javafx-19\lib\javafx.controls.jar") {
    Write-Host "✅ JavaFX disponible" -ForegroundColor Green
}
else {
    Write-Host "❌ JavaFX non disponible" -ForegroundColor Red
}

# Tester les dépendances
$depCount = (Get-ChildItem "lib\*.jar" | Measure-Object).Count
Write-Host "✅ $depCount dépendances téléchargées" -ForegroundColor Green

Write-Host ""
Write-Host "=== INSTALLATION TERMINÉE ===" -ForegroundColor Green
Write-Host ""
Write-Host "Pour compiler et lancer l'application:" -ForegroundColor Cyan
Write-Host "  1. Exécutez: compile-now.bat" -ForegroundColor White
Write-Host "  2. Puis: run-now.bat" -ForegroundColor White
Write-Host ""
Write-Host "Ou directement: run-now.bat (compile automatiquement)" -ForegroundColor White
Write-Host ""

# Redémarrer le terminal pour charger les nouvelles variables d'environnement
Write-Host "⚠️  Redémarrez votre terminal pour charger les nouvelles variables d'environnement" -ForegroundColor Yellow
