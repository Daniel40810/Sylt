@echo off
rem Sylt mit Konsole starten: alle Meldungen bleiben stehen (zur Fehlersuche; Phase 19)
rem Ohne Startbild: Sylt_Konsole.bat -Dsylt.splash=false
cd /d "%~dp0"
if not exist dist\Sylt.jar (echo dist\Sylt.jar fehlt: bitte in NetBeans Clean and Build ausfuehren. & pause & exit /b 1)
java -Xmx6g -Dsun.java2d.uiScale.enabled=true --enable-native-access=ALL-UNNAMED --add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.awt.windows=ALL-UNNAMED --add-opens java.desktop/sun.java2d=ALL-UNNAMED %* -jar dist\Sylt.jar
echo.
echo Sylt beendet (Rueckgabewert %errorlevel%).
pause
