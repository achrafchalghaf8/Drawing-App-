Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   LANCEMENT APPLICATION MODELISATION" -ForegroundColor Cyan
Write-Host "   (Avec Support JavaFX)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$javaPath = "C:\java\jdk-21.0.2\bin\java.exe"
$jarPath = "target\drawing-app-1.0-SNAPSHOT.jar"

Write-Host "1. Verification de Java et JavaFX..." -ForegroundColor Yellow
if (Test-Path $javaPath) {
    Write-Host "✅ Java 21 trouve: $javaPath" -ForegroundColor Green
} else {
    Write-Host "❌ Java 21 non trouve" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Verification du JAR..." -ForegroundColor Yellow
if (Test-Path $jarPath) {
    Write-Host "✅ JAR trouve: $jarPath" -ForegroundColor Green
    $jarInfo = Get-Item $jarPath
    Write-Host "Taille: $([math]::Round($jarInfo.Length/1MB, 2)) MB" -ForegroundColor Gray
} else {
    Write-Host "❌ JAR non trouve" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "3. Lancement avec support JavaFX..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# Arguments JavaFX pour Java 21
$javaArgs = @(
    "--add-opens", "javafx.controls/com.sun.javafx.charts=ALL-UNNAMED",
    "--add-opens", "javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED", 
    "--add-opens", "javafx.graphics/com.sun.javafx.iio.common=ALL-UNNAMED",
    "--add-opens", "javafx.graphics/com.sun.javafx.css=ALL-UNNAMED",
    "--add-opens", "javafx.base/com.sun.javafx.runtime=ALL-UNNAMED",
    "-Djavafx.preloader=",
    "-jar", $jarPath
)

Write-Host "Arguments Java: $($javaArgs -join ' ')" -ForegroundColor Gray
Write-Host ""

try {
    # Lancer l'application
    Write-Host "🚀 Lancement de l'application..." -ForegroundColor Green
    $process = Start-Process -FilePath $javaPath -ArgumentList $javaArgs -PassThru -WindowStyle Normal
    
    Write-Host "✅ Application lancee avec PID: $($process.Id)" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Instructions:" -ForegroundColor Yellow
    Write-Host "- L'application devrait s'ouvrir dans une nouvelle fenetre" -ForegroundColor White
    Write-Host "- Testez les fonctionnalites de dessin" -ForegroundColor White
    Write-Host "- Testez le mode 'Plus Court Chemin'" -ForegroundColor White
    Write-Host "- Appuyez sur une touche ici pour arreter le monitoring..." -ForegroundColor White
    
    # Attendre une entrée utilisateur
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    
    if (!$process.HasExited) {
        Write-Host ""
        Write-Host "🔄 Application toujours en cours..." -ForegroundColor Green
        Write-Host "Voulez-vous l'arreter? (O/N)" -ForegroundColor Yellow
        $response = Read-Host
        if ($response -eq "O" -or $response -eq "o") {
            $process.Kill()
            Write-Host "❌ Application arretee" -ForegroundColor Red
        } else {
            Write-Host "✅ Application laissee en cours d'execution" -ForegroundColor Green
        }
    } else {
        Write-Host "❌ Application fermee. Code de sortie: $($process.ExitCode)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erreur lors du lancement: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fin du script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
