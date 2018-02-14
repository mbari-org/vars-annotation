@echo off
REM $Id: $
title VARS - Annotation
SET VARS_HOME=%~dp0..
SET VARS_CLASSPATH="%VARS_HOME%\conf";"%VARS_HOME%\lib\*"

echo [VARS] Starting VARS Annotation Application
java -cp %VARS_CLASSPATH% -Xms64m -Xmx256m -Duser.timezone=UTC org.mbari.m3.vars.annotation.App