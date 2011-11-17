@echo off
set SCRIPT_DIR=%~dp0
java -server -Xmx512M -jar "%SCRIPT_DIR%sbt-launch.jar" %*