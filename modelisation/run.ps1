Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   LANCEMENT APPLICATION MODELISATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration Java
$javaPath = "C:\Program Files\Java\jdk-22\bin\java.exe"
$jarPath = "target\drawing-app-1.0-SNAPSHOT.jar"

Write-Host "1. Verification de Java..." -ForegroundColor Yellow
if (Test-Path $javaPath) {
    Write-Host "✅ Java 22 trouve: $javaPath" -ForegroundColor Green
    & $javaPath -version
} else {
    Write-Host "❌ Java 22 non trouve, utilisation de java par defaut" -ForegroundColor Red
    $javaPath = "java"
    java -version
}

Write-Host ""
Write-Host "2. Verification du JAR..." -ForegroundColor Yellow
if (Test-Path $jarPath) {
    Write-Host "✅ JAR trouve: $jarPath" -ForegroundColor Green
    $jarInfo = Get-Item $jarPath
    Write-Host "Taille: $($jarInfo.Length) bytes" -ForegroundColor Gray
} else {
    Write-Host "❌ JAR non trouve" -ForegroundColor Red
    Write-Host "Contenu du dossier target:" -ForegroundColor Gray
    Get-ChildItem target\ | Format-Table Name, Length
    exit 1
}

Write-Host ""
Write-Host "3. Lancement de l'application..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

try {
    # Lancer l'application et capturer les erreurs
    $process = Start-Process -FilePath $javaPath -ArgumentList "-jar", $jarPath -PassThru -RedirectStandardError "error.log" -RedirectStandardOutput "output.log" -WindowStyle Hidden
    
    Write-Host "Application lancee avec PID: $($process.Id)" -ForegroundColor Green
    Write-Host "Attente de 5 secondes..." -ForegroundColor Gray
    Start-Sleep -Seconds 5
    
    if ($process.HasExited) {
        Write-Host "❌ Application fermee rapidement. Code de sortie: $($process.ExitCode)" -ForegroundColor Red
        
        Write-Host ""
        Write-Host "Erreurs:" -ForegroundColor Red
        if (Test-Path "error.log") {
            Get-Content "error.log"
        }
        
        Write-Host ""
        Write-Host "Sortie:" -ForegroundColor Blue
        if (Test-Path "output.log") {
            Get-Content "output.log"
        }
    } else {
        Write-Host "✅ Application en cours d'execution..." -ForegroundColor Green
        Write-Host "Appuyez sur une touche pour arreter l'application..." -ForegroundColor Yellow
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        $process.Kill()
    }
} catch {
    Write-Host "❌ Erreur lors du lancement: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fin du script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
