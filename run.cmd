@echo off
rem Starts the API on http://localhost:8080. Needs a JDK 17 or newer on PATH (or JAVA_HOME set).
call "%~dp0mvnw.cmd" spring-boot:run
