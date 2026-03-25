@echo off

call .\build.bat

echo Finished compiling, trying to run

java -cp target/classes Main