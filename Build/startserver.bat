@echo off
if not "%ECHO%" == ""  echo %ECHO%
rem if "%OS%" == "Windows_NT" setlocal
cd ..\jboss-as-7.1.1.Final\bin
standalone.bat