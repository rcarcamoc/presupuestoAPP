@echo off
setlocal enabledelayedexpansion

:: Configuración básica
set "MAX_DAEMONS=2"  :: Número máximo de daemons a mantener

echo [DEBUG] ===== INICIO DAEMON MANAGER =====
echo [DEBUG] Directorio actual: %CD%
echo [DEBUG] Configuración:
echo [DEBUG] - MAX_DAEMONS: %MAX_DAEMONS%

:: Función para obtener la lista de daemons activos
:get_active_daemons
    setlocal enabledelayedexpansion
    set "DAEMON_COUNT=0"
    set "found_header=false"
    set "DAEMON_LIST="

    echo [DEBUG] Obteniendo lista de daemons activos...

    for /f "tokens=1,2,3* delims= " %%a in ('gradlew --status 2^>nul') do (
        if "!found_header!"=="false" (
            set "found_header=true"
            echo [DEBUG] Encabezado encontrado: %%a %%b %%c
        ) else (
            :: Mostrar valor hexadecimal de %%a para debug
            set "PID_RAW=%%a"
            for /l %%i in (0,1,31) do (
                set "char=!PID_RAW:~%%i,1!"
                if not "!char!"=="" (
                    set /a "hex=0x100+0x!char!" 2>nul
                    if errorlevel 1 (
                        set /a "hex=0x100+0x0"
                    )
                    set "hexstr=!hex!"
                    echo [DEBUG] PID char %%i: !char! (hex: !hexstr!)
                )
            )
            echo [DEBUG] Valor crudo de PID: '%%a'
            echo %%a | findstr /r "^[0-9][0-9]*$" >nul
            if not errorlevel 1 (
                if not "%%b"=="" (
                    set /a DAEMON_COUNT+=1
                    set "DAEMON_PID=%%a"
                    set "DAEMON_STATUS=%%b"
                    set "DAEMON_INFO=%%c %%d"
                    echo [DEBUG] Daemon encontrado: PID=!DAEMON_PID! Status=!DAEMON_STATUS! Info=!DAEMON_INFO!
                    if not defined DAEMON_LIST (
                        set "DAEMON_LIST=!DAEMON_PID!|!DAEMON_STATUS!|!DAEMON_INFO!"
                    ) else (
                        set "DAEMON_LIST=!DAEMON_LIST!;!DAEMON_PID!|!DAEMON_STATUS!|!DAEMON_INFO!"
                    )
                ) else (
                    echo [DEBUG] Ignorando línea incompleta: %%a %%b %%c
                )
            ) else (
                echo [DEBUG] Ignorando línea sin PID válido: %%a %%b %%c
            )
        )
    )

    echo [DEBUG] Total de daemons encontrados: !DAEMON_COUNT!
    if defined DAEMON_LIST (
        echo [DEBUG] Lista de daemons: !DAEMON_LIST!
    ) else (
        echo [DEBUG] No se encontraron daemons activos
    )

    endlocal & (
        set "DAEMON_COUNT=%DAEMON_COUNT%"
        set "DAEMON_LIST=%DAEMON_LIST%"
    )
    goto :eof

:: Función para limpiar daemons excedentes
:cleanup_daemons
    setlocal enabledelayedexpansion
    echo [INFO] Verificando daemons activos...
    
    call :get_active_daemons
    echo [DEBUG] Después de get_active_daemons:
    echo [DEBUG] - DAEMON_COUNT: %DAEMON_COUNT%
    echo [DEBUG] - DAEMON_LIST: %DAEMON_LIST%

    if !DAEMON_COUNT! leq %MAX_DAEMONS% (
        echo [INFO] Número de daemons (!DAEMON_COUNT!) dentro del límite (%MAX_DAEMONS%)
        endlocal & (
            set "DAEMON_COUNT=%DAEMON_COUNT%"
            set "DAEMON_LIST=%DAEMON_LIST%"
        )
        goto :eof
    )

    echo [INFO] Limpiando daemons excedentes...
    
    :: Ordenar daemons por última actividad (más reciente primero)
    set "SORTED_DAEMONS="
    for %%d in ("!DAEMON_LIST:;=" "!") do (
        set "DAEMON=%%~d"
        if not defined SORTED_DAEMONS (
            set "SORTED_DAEMONS=!DAEMON!"
        ) else (
            set "SORTED_DAEMONS=!DAEMON!;!SORTED_DAEMONS!"
        )
    )

    :: Mantener solo los daemons más recientes
    set "KEEP_COUNT=0"
    set "DAEMONS_TO_KILL="

    for %%d in ("!SORTED_DAEMONS:;=" "!") do (
        set "DAEMON=%%~d"
        for /f "tokens=1,2,3 delims=|" %%a in ("!DAEMON!") do (
            set "PID=%%a"
            set "STATUS=%%b"
            set "INFO=%%c"

            if !KEEP_COUNT! lss %MAX_DAEMONS% (
                set /a "KEEP_COUNT+=1"
                echo [DEBUG] Manteniendo daemon PID: !PID! (Status: !STATUS!, Info: !INFO!)
            ) else (
                if not defined DAEMONS_TO_KILL (
                    set "DAEMONS_TO_KILL=!PID!"
                ) else (
                    set "DAEMONS_TO_KILL=!DAEMONS_TO_KILL! !PID!"
                )
            )
        )
    )

    :: Matar daemons excedentes
    if defined DAEMONS_TO_KILL (
        echo [INFO] Deteniendo daemons excedentes: !DAEMONS_TO_KILL!
        for %%p in (!DAEMONS_TO_KILL!) do (
            echo [DEBUG] Deteniendo daemon PID: %%p
            taskkill /F /PID %%p 2>nul
            if !errorlevel! equ 0 (
                echo [INFO] Daemon %%p detenido exitosamente
            ) else (
                echo [WARN] No se pudo detener el daemon %%p
            )
        )
    )

    endlocal & (
        set "DAEMON_COUNT=%DAEMON_COUNT%"
        set "DAEMON_LIST=%DAEMON_LIST%"
    )
    goto :eof

:: Procesar argumentos
if "%~1"=="" (
    echo [INFO] Uso: daemon_manager.bat [--check ^| --stop ^| --clean]
    echo [INFO]   --check : Muestra los daemons activos
    echo [INFO]   --stop  : Detiene todos los daemons
    echo [INFO]   --clean : Limpia daemons excedentes
    exit /b 1
)

:: Ejecutar acción según argumento
if /i "%~1"=="--check" (
    echo [DEBUG] Verificando daemons activos...
    call :get_active_daemons
    echo [INFO] Daemons activos: %DAEMON_COUNT%
    goto :end
)

if /i "%~1"=="--stop" (
    echo [DEBUG] Deteniendo todos los daemons...
    call gradlew --stop
    if !errorlevel! neq 0 (
        echo [WARN] No se pudo detener los daemons
    ) else (
        echo [INFO] Todos los daemons detenidos exitosamente
    )
    goto :end
)

if /i "%~1"=="--clean" (
    echo [DEBUG] Limpiando daemons excedentes...
    call :cleanup_daemons
    goto :end
)

echo [ERROR] Argumento no válido: %~1
exit /b 1

:end
echo [DEBUG] ===== FIN DAEMON MANAGER =====
endlocal 