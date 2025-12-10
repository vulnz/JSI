@echo off
setlocal ENABLEDELAYEDEXPANSION

REM ------------------------------
REM Validate input parameters
REM ------------------------------
if "%~1"=="" (
    echo Usage: MakePayload.bat ^<binary^> ^<argument^> [-r]
    exit /b
)

if "%~2"=="" (
    echo Usage: MakePayload.bat ^<binary^> ^<argument^> [-r]
    exit /b
)

set BIN=%~1
set ARG=%~2
set FLAG=%~3

REM ------------------------------
REM Random name generation (8 hex chars)
REM ------------------------------
set RAND=
for /l %%A in (1,1,8) do (
    set /a N=!random! %% 16
    for %%H in (0 1 2 3 4 5 6 7 8 9 A B C D E F) do (
        if "!N!"=="%%H" set RAND=!RAND!%%H
    )
)

REM ------------------------------
REM Decide output name
REM ------------------------------
if /I "%FLAG%"=="-r" (
    set OUT=payload_!RAND!.bin
) else (
    REM Strip .exe → .bin
    set OUT=%BIN:.exe=%.bin
)

echo [+] Building payload...
echo     Binary  : %BIN%
echo     Argument: %ARG%
echo     Flag    : %FLAG%
echo     Output  : %OUT%

donut.exe -i "%BIN%" -p "%ARG%" -a 2 -o "%OUT%" -z 3 -e 3 -t 5

echo [+] Done.
endlocal
