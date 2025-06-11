Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   LANCEMENT APPLICATION MODELISATION" -ForegroundColor Cyan
Write-Host "   (Version Debug avec Logging)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration des chemins
$javaPath = "C:\java\jdk-21.0.2\bin\java.exe"
$jarPath = "C:\Users\HP\Downloads\modelisation Proj\modelisation\modelisation\modelisation\modelisation\target\drawing-app-1.0-SNAPSHOT.jar"
$javafxPath = "C:\Users\HP\Downloads\modelisation Proj\modelisation\modelisation\modelisation\modelisation\javafx-sdk-19.0.2.1\lib"

Write-Host "1. Vérification des composants..." -ForegroundColor Yellow

# Vérifier Java
if (Test-Path $javaPath) {
    Write-Host "✅ Java 21 trouvé: $javaPath" -ForegroundColor Green
    $javaVersion = & $javaPath -version 2>&1 | Select-String "version"
    Write-Host "   Version: $javaVersion" -ForegroundColor Gray
} else {
    Write-Host "❌ Java 21 non trouvé à: $javaPath" -ForegroundColor Red
    exit 1
}

# Vérifier JAR
if (Test-Path $jarPath) {
    Write-Host "✅ JAR trouvé: $jarPath" -ForegroundColor Green
    $jarInfo = Get-Item $jarPath
    Write-Host "   Taille: $([math]::Round($jarInfo.Length/1MB, 2)) MB" -ForegroundColor Gray
} else {
    Write-Host "❌ JAR non trouvé à: $jarPath" -ForegroundColor Red
    exit 1
}

# Vérifier JavaFX
if (Test-Path $javafxPath) {
    Write-Host "✅ JavaFX trouvé: $javafxPath" -ForegroundColor Green
} else {
    Write-Host "❌ JavaFX non trouvé à: $javafxPath" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Informations de lancement..." -ForegroundColor Yellow
Write-Host "Répertoire de travail: $(Get-Location)" -ForegroundColor Gray
Write-Host "Dossier logs: $(Join-Path (Get-Location) 'logs')" -ForegroundColor Gray

# Vérifier le dossier logs
$logsPath = Join-Path (Get-Location) "logs"
if (Test-Path $logsPath) {
    $logFiles = Get-ChildItem $logsPath -Filter "*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 3
    Write-Host "Derniers fichiers de log:" -ForegroundColor Gray
    foreach ($file in $logFiles) {
        Write-Host "   - $($file.Name) ($($file.LastWriteTime))" -ForegroundColor Gray
    }
} else {
    Write-Host "Dossier logs non trouvé, sera créé automatiquement" -ForegroundColor Gray
}

Write-Host ""
Write-Host "3. Lancement de l'application..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# Arguments pour Java
$javaArgs = @(
    "--module-path", $javafxPath,
    "--add-modules", "javafx.controls,javafx.fxml",
    "-jar", $jarPath
)

Write-Host "Commande: $javaPath $($javaArgs -join ' ')" -ForegroundColor Gray
Write-Host ""

try {
    Write-Host "🚀 Démarrage de l'application..." -ForegroundColor Green
    Write-Host "   - L'interface graphique devrait s'ouvrir" -ForegroundColor White
    Write-Host "   - Testez le basculement vers 'Fichier' dans le menu logging" -ForegroundColor White
    Write-Host "   - Créez quelques formes pour générer des logs" -ForegroundColor White
    Write-Host ""
    
    # Lancer l'application
    $process = Start-Process -FilePath $javaPath -ArgumentList $javaArgs -PassThru -WindowStyle Normal
    
    Write-Host "✅ Application lancée avec PID: $($process.Id)" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Instructions de test:" -ForegroundColor Yellow
    Write-Host "1. Dans l'application, changez le logging vers 'Fichier'" -ForegroundColor White
    Write-Host "2. Créez quelques formes (cercles, rectangles)" -ForegroundColor White
    Write-Host "3. Testez le mode 'Plus Court Chemin'" -ForegroundColor White
    Write-Host "4. Sauvegardez un dessin" -ForegroundColor White
    Write-Host ""
    Write-Host "Appuyez sur une touche pour vérifier les logs..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    
    # Vérifier les nouveaux logs
    Write-Host ""
    Write-Host "4. Vérification des logs..." -ForegroundColor Yellow
    if (Test-Path $logsPath) {
        $newLogFiles = Get-ChildItem $logsPath -Filter "*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 5
        Write-Host "Fichiers de log (les plus récents):" -ForegroundColor Green
        foreach ($file in $newLogFiles) {
            Write-Host "   - $($file.Name) ($($file.LastWriteTime)) - $([math]::Round($file.Length/1KB, 1)) KB" -ForegroundColor White
        }
        
        # Afficher le contenu du dernier log
        $latestLog = $newLogFiles | Select-Object -First 1
        if ($latestLog) {
            Write-Host ""
            Write-Host "Contenu du dernier log ($($latestLog.Name)):" -ForegroundColor Green
            Get-Content $latestLog.FullName | Select-Object -Last 10 | ForEach-Object {
                Write-Host "   $_" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "❌ Aucun dossier logs trouvé" -ForegroundColor Red
    }
    
    if (!$process.HasExited) {
        Write-Host ""
        Write-Host "Application toujours en cours. Voulez-vous l'arrêter? (O/N)" -ForegroundColor Yellow
        $response = Read-Host
        if ($response -eq "O" -or $response -eq "o") {
            $process.Kill()
            Write-Host "❌ Application arrêtée" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "❌ Erreur lors du lancement: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fin du script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
