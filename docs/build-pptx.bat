@echo off
cd /d "%~dp0"

marp --theme-set cursor-api-theme.css --pptx cursor-api-presentation.md
if errorlevel 1 exit /b 1

echo Built cursor-api-presentation.pptx
