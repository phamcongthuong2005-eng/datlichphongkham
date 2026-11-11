@echo off
chcp 65001 > nul
echo ===================================================
echo   🏥 ĐANG KHỞI ĐỘNG SERVER TCP/IP PHÒNG KHÁM LÊ LỢI 🏥  
echo   Chuyên Khoa: Cơ Xương Khớp - Lập Trình Mạng       
echo ===================================================
cd /d "%~dp0"

:: Auto-detect Java/Javac
set "JAVAC_CMD=javac"
set "JAVA_CMD=java"

where javac >nul 2>&1
if %errorlevel% neq 0 (
    if exist "D:\DAU_2027\Lap trinh di dong\jbr\bin\javac.exe" (
        set "JAVAC_CMD=D:\DAU_2027\Lap trinh di dong\jbr\bin\javac.exe"
        set "JAVA_CMD=D:\DAU_2027\Lap trinh di dong\jbr\bin\java.exe"
    ) else if exist "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
        set "JAVA_CMD=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    ) else if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

if not exist "bin" mkdir "bin"

echo [1/2] Đang biên dịch mã nguồn Java Server TCP/IP...
"%JAVAC_CMD%" -encoding UTF-8 -d bin src\org\json\*.java src\com\phongkham\server\*.java

if %errorlevel% neq 0 (
    echo.
    echo [LƯU Ý] Biên dịch thất bại. Hãy kiểm tra đường dẫn JDK.
    echo.
    pause
    exit /b
)

echo [2/2] Khởi chạy Server TCP/IP Phòng Khám Lê Lợi trên cổng 8888...
echo.
"%JAVA_CMD%" -cp bin com.phongkham.server.ServerMain
pause
