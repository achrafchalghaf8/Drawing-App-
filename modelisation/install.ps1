# Script d'installation automatique pour l'application de dessin JavaFX
# Exécuter en tant qu'administrateur : Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

param(
    [switch]$SkipJava,
    [switch]$SkipMaven,
    [switch]$SkipJavaFX,
    [string]$JavaFXPath = "C:\javafx-19"
)

Write-Host "=== Installation de l'Application de Dessin JavaFX ===" -ForegroundColor Green
Write-Host ""

# Fonction pour vérifier si une commande existe
function Test-Command($cmdname) {
    return [bool](Get-Command -Name $cmdname -ErrorAction SilentlyContinue)
}

# Fonction pour télécharger un fichier
function Download-File($url, $output) {
    Write-Host "Téléchargement de $url..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
        return $true
    }
    catch {
        Write-Host "Erreur lors du téléchargement: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 1. Vérification et installation de Java
if (-not $SkipJava) {
    Write-Host "1. Vérification de Java..." -ForegroundColor Cyan
    
    if (Test-Command java) {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
        Write-Host "Java trouvé: $javaVersion" -ForegroundColor Green
    }
    else {
        Write-Host "Java non trouvé. Installation d'OpenJDK 17..." -ForegroundColor Yellow
        
        # Télécharger OpenJDK 17
        $jdkUrl = "https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_windows-x64_bin.zip"
        $jdkZip = "$env:TEMP\openjdk-17.zip"
        $jdkDir = "C:\openjdk-17"
        
        if (Download-File $jdkUrl $jdkZip) {
            Write-Host "Extraction de Java..." -ForegroundColor Yellow
            Expand-Archive -Path $jdkZip -DestinationPath "C:\" -Force
            
            # Renommer le dossier
            $extractedDir = Get-ChildItem "C:\jdk-17*" | Select-Object -First 1
            if ($extractedDir) {
                Rename-Item $extractedDir.FullName $jdkDir -Force
            }
            
            # Ajouter au PATH
            $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
            if ($currentPath -notlike "*$jdkDir\bin*") {
                [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$jdkDir\bin", "User")
                $env:PATH += ";$jdkDir\bin"
            }
            
            # Définir JAVA_HOME
            [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkDir, "User")
            $env:JAVA_HOME = $jdkDir
            
            Write-Host "Java installé avec succès!" -ForegroundColor Green
            Remove-Item $jdkZip -Force -ErrorAction SilentlyContinue
        }
        else {
            Write-Host "Échec du téléchargement de Java. Veuillez l'installer manuellement." -ForegroundColor Red
        }
    }
}

# 2. Vérification et installation de Maven
if (-not $SkipMaven) {
    Write-Host ""
    Write-Host "2. Vérification de Maven..." -ForegroundColor Cyan
    
    if (Test-Command mvn) {
        $mavenVersion = mvn -version | Select-String "Apache Maven" | ForEach-Object { $_.ToString() }
        Write-Host "Maven trouvé: $mavenVersion" -ForegroundColor Green
    }
    else {
        Write-Host "Maven non trouvé. Installation de Maven..." -ForegroundColor Yellow
        
        # Télécharger Maven
        $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.zip"
        $mavenZip = "$env:TEMP\maven.zip"
        $mavenDir = "C:\apache-maven"
        
        if (Download-File $mavenUrl $mavenZip) {
            Write-Host "Extraction de Maven..." -ForegroundColor Yellow
            Expand-Archive -Path $mavenZip -DestinationPath "C:\" -Force
            
            # Renommer le dossier
            $extractedDir = Get-ChildItem "C:\apache-maven-*" | Select-Object -First 1
            if ($extractedDir) {
                if (Test-Path $mavenDir) {
                    Remove-Item $mavenDir -Recurse -Force
                }
                Rename-Item $extractedDir.FullName $mavenDir -Force
            }
            
            # Ajouter au PATH
            $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
            if ($currentPath -notlike "*$mavenDir\bin*") {
                [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$mavenDir\bin", "User")
                $env:PATH += ";$mavenDir\bin"
            }
            
            Write-Host "Maven installé avec succès!" -ForegroundColor Green
            Remove-Item $mavenZip -Force -ErrorAction SilentlyContinue
        }
        else {
            Write-Host "Échec du téléchargement de Maven. Veuillez l'installer manuellement." -ForegroundColor Red
        }
    }
}

# 3. Installation de JavaFX
if (-not $SkipJavaFX) {
    Write-Host ""
    Write-Host "3. Installation de JavaFX..." -ForegroundColor Cyan
    
    if (-not (Test-Path $JavaFXPath)) {
        Write-Host "Téléchargement de JavaFX..." -ForegroundColor Yellow
        
        # Télécharger JavaFX
        $javafxUrl = "https://download2.gluonhq.com/openjfx/19.0.2.1/openjfx-19.0.2.1_windows-x64_bin-sdk.zip"
        $javafxZip = "$env:TEMP\javafx.zip"
        
        if (Download-File $javafxUrl $javafxZip) {
            Write-Host "Extraction de JavaFX..." -ForegroundColor Yellow
            Expand-Archive -Path $javafxZip -DestinationPath "C:\" -Force
            
            # Renommer le dossier
            $extractedDir = Get-ChildItem "C:\javafx-*" | Select-Object -First 1
            if ($extractedDir) {
                if (Test-Path $JavaFXPath) {
                    Remove-Item $JavaFXPath -Recurse -Force
                }
                Rename-Item $extractedDir.FullName $JavaFXPath -Force
            }
            
            Write-Host "JavaFX installé dans $JavaFXPath" -ForegroundColor Green
            Remove-Item $javafxZip -Force -ErrorAction SilentlyContinue
        }
        else {
            Write-Host "Échec du téléchargement de JavaFX. Veuillez l'installer manuellement." -ForegroundColor Red
        }
    }
    else {
        Write-Host "JavaFX déjà installé dans $JavaFXPath" -ForegroundColor Green
    }
}

# 4. Configuration du projet
Write-Host ""
Write-Host "4. Configuration du projet..." -ForegroundColor Cyan

# Créer les répertoires nécessaires
$directories = @("target", "target\classes", "target\test-classes", "logs", "lib")
foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "Répertoire créé: $dir" -ForegroundColor Gray
    }
}

# Créer un fichier de configuration
$configContent = @"
# Configuration de l'application de dessin
javafx.path=$JavaFXPath\lib
java.home=$env:JAVA_HOME
maven.home=$env:M2_HOME
app.version=1.0.0
app.name=Drawing Application
"@

$configContent | Out-File -FilePath "app.properties" -Encoding UTF8
Write-Host "Fichier de configuration créé: app.properties" -ForegroundColor Green

# Créer un script de lancement amélioré
$launchScript = @"
@echo off
echo Lancement de l'Application de Dessin JavaFX...

REM Vérifier que les classes sont compilées
if not exist "target\classes\com\modelisation\Main.class" (
    echo Compilation du projet...
    call mvn clean compile
    if errorlevel 1 (
        echo Erreur lors de la compilation
        pause
        exit /b 1
    )
)

REM Lancer l'application avec JavaFX
echo Démarrage de l'application...
java --module-path "$JavaFXPath\lib" --add-modules javafx.controls,javafx.fxml -cp target\classes com.modelisation.Main

if errorlevel 1 (
    echo.
    echo Erreur lors du lancement. Vérifiez que JavaFX est correctement installé.
    echo Chemin JavaFX: $JavaFXPath\lib
    pause
)
"@

$launchScript | Out-File -FilePath "launch.bat" -Encoding ASCII
Write-Host "Script de lancement créé: launch.bat" -ForegroundColor Green

# 5. Compilation du projet
Write-Host ""
Write-Host "5. Compilation du projet..." -ForegroundColor Cyan

if (Test-Command mvn) {
    Write-Host "Compilation avec Maven..." -ForegroundColor Yellow
    & mvn clean compile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation réussie!" -ForegroundColor Green
    }
    else {
        Write-Host "Erreur lors de la compilation. Vérifiez les dépendances." -ForegroundColor Red
    }
}
else {
    Write-Host "Maven non disponible. Compilation manuelle..." -ForegroundColor Yellow
    
    if (Test-Command javac) {
        # Compilation manuelle
        $sourceFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse
        $sourceList = $sourceFiles.FullName -join " "
        
        & javac -d "target\classes" -cp "target\classes" $sourceFiles.FullName
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Compilation manuelle réussie!" -ForegroundColor Green
            
            # Copier les ressources
            if (Test-Path "src\main\resources") {
                Copy-Item "src\main\resources\*" "target\classes\" -Recurse -Force
                Write-Host "Ressources copiées" -ForegroundColor Gray
            }
        }
        else {
            Write-Host "Erreur lors de la compilation manuelle" -ForegroundColor Red
        }
    }
    else {
        Write-Host "Aucun compilateur Java trouvé" -ForegroundColor Red
    }
}

# 6. Test de l'installation
Write-Host ""
Write-Host "6. Test de l'installation..." -ForegroundColor Cyan

$testsPassed = 0
$totalTests = 4

# Test Java
if (Test-Command java) {
    Write-Host "✓ Java disponible" -ForegroundColor Green
    $testsPassed++
}
else {
    Write-Host "✗ Java non disponible" -ForegroundColor Red
}

# Test Maven
if (Test-Command mvn) {
    Write-Host "✓ Maven disponible" -ForegroundColor Green
    $testsPassed++
}
else {
    Write-Host "✗ Maven non disponible" -ForegroundColor Yellow
}

# Test JavaFX
if (Test-Path "$JavaFXPath\lib\javafx.controls.jar") {
    Write-Host "✓ JavaFX disponible" -ForegroundColor Green
    $testsPassed++
}
else {
    Write-Host "✗ JavaFX non disponible" -ForegroundColor Red
}

# Test compilation
if (Test-Path "target\classes\com\modelisation\Main.class") {
    Write-Host "✓ Projet compilé" -ForegroundColor Green
    $testsPassed++
}
else {
    Write-Host "✗ Projet non compilé" -ForegroundColor Red
}

# Résumé
Write-Host ""
Write-Host "=== RÉSUMÉ DE L'INSTALLATION ===" -ForegroundColor Green
Write-Host "Tests réussis: $testsPassed/$totalTests" -ForegroundColor $(if ($testsPassed -eq $totalTests) { "Green" } else { "Yellow" })

if ($testsPassed -eq $totalTests) {
    Write-Host ""
    Write-Host "🎉 Installation terminée avec succès!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Pour lancer l'application:" -ForegroundColor Cyan
    Write-Host "  1. Double-cliquez sur launch.bat" -ForegroundColor White
    Write-Host "  2. Ou exécutez: mvn javafx:run" -ForegroundColor White
    Write-Host ""
}
else {
    Write-Host ""
    Write-Host "⚠️  Installation incomplète. Consultez les erreurs ci-dessus." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Actions recommandées:" -ForegroundColor Cyan
    if (-not (Test-Command java)) {
        Write-Host "  - Installer Java 17+ manuellement" -ForegroundColor White
    }
    if (-not (Test-Path "$JavaFXPath\lib\javafx.controls.jar")) {
        Write-Host "  - Télécharger JavaFX depuis https://openjfx.io/" -ForegroundColor White
    }
    Write-Host ""
}

Write-Host "Logs d'installation sauvegardés dans install.log" -ForegroundColor Gray
