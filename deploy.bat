@echo off
call mvn clean
call mvn package

echo Project jar file created on directory {project-root}/target/
