@echo off
setlocal
cd /d "%~dp0"
echo ============================================================
echo   INICIANDO SUBNOTWAY - SERVIDOR WEB EN SPRING BOOT
echo ============================================================
echo.
echo 1. Recuerda tener iniciado el servicio MySQL en XAMPP.
echo 2. La base de datos debe llamarse: pan_puntos_premios
echo 3. Abre tu navegador en: http://localhost:8080
echo.

where mvn >nul 2>nul
if %errorlevel% equ 0 (
    mvn spring-boot:run
    goto fin
)

if exist "C:\netbeans-31\netbeans\java\maven\bin\mvn.cmd" (
    if exist "E:\Program Files\Java\jdk-25.0.2" (
        set "JAVA_HOME=E:\Program Files\Java\jdk-25.0.2"
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    )
    "C:\netbeans-31\netbeans\java\maven\bin\mvn.cmd" spring-boot:run
    goto fin
)

echo ERROR: No se encontro Maven configurado en la variable PATH.
echo Puedes abrir la carpeta 'Subnotway' directamente en NetBeans o IntelliJ y darle 'Run Project'.
echo O instalar Maven para ejecutarlo por consola.

:fin
pause
