@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, for Windows.
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=.

set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if exist "%WRAPPER_JAR%" goto runWrapper

echo Downloading Maven Wrapper jar...
for /f "usebackq delims=" %%A in (`powershell -NoProfile -Command "(Get-Content '%WRAPPER_PROPERTIES%' | Where-Object { $_ -like 'wrapperUrl=*' } | Select-Object -First 1).Split('=')[1]"`) do set WRAPPER_URL=%%A

if "%WRAPPER_URL%"=="" (
  echo ERROR: wrapperUrl not found in %WRAPPER_PROPERTIES%
  exit /b 1
)

powershell -NoProfile -Command "$u='%WRAPPER_URL%'; $o='%WRAPPER_JAR%'; New-Item -ItemType Directory -Force (Split-Path $o) | Out-Null; Invoke-WebRequest -Uri $u -OutFile $o"

:runWrapper
set BASEDIR=%MAVEN_PROJECTBASEDIR%
if "%BASEDIR:~-1%"=="\" set BASEDIR=%BASEDIR:~0,-1%
set MAVEN_MULTI_MODULE_PROJECT_DIRECTORY=%BASEDIR%
java -Dmaven.multiModuleProjectDirectory=%BASEDIR% -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*
endlocal
