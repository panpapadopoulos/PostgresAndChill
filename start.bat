@echo off
setlocal

echo Starting Postgres & Chill...

:: Open loading screen
start "" "%~dp0frontend\loading.html"

:: Run your backend startup invisibly
powershell -WindowStyle Hidden -Command ^
  "Start-Process 'cmd.exe' -ArgumentList '/c docker rm -f postgres-and-chill-db >nul 2>&1 && docker-compose down >nul 2>&1 && docker-compose up --build' -WorkingDirectory '%~dp0' -WindowStyle Hidden"

exit
