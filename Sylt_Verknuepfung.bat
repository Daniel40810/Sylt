@echo off
rem Sylt: Verknuepfung "Sylt" mit Symbol auf dem Desktop und im Startmenue anlegen (Phase 19)
cd /d "%~dp0"
if not exist Sylt.ico (echo Sylt.ico fehlt im Projektordner: ant symbol ausfuehren. & pause & exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -Command "$w = New-Object -ComObject WScript.Shell; foreach ($d in @([Environment]::GetFolderPath('Desktop'), [Environment]::GetFolderPath('Programs'))) { $s = $w.CreateShortcut((Join-Path $d 'Sylt.lnk')); $s.TargetPath = '%~dp0Sylt.bat'; $s.WorkingDirectory = '%~dp0'; $s.IconLocation = '%~dp0Sylt.ico'; $s.WindowStyle = 7; $s.Description = 'Sylt - Rotes Kliff bei Kampen: das Projekt der Brandung'; $s.Save() }"
if errorlevel 1 (echo Die Verknuepfung liess sich nicht anlegen. & pause & exit /b 1)
echo Verknuepfung "Sylt" liegt auf dem Desktop und im Startmenue.
pause
