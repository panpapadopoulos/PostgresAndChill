@echo off
echo Opening backend console output...
echo (Press CTRL+C to stop viewing logs but keep backend running)
echo.

start "Backend Logs" cmd /k "docker-compose logs -f"
