@echo off

if exist out rmdir /s /q out
mkdir out

set a=
for /r src\main\java %%f in (*.java) do (
    set a=%a% "%%f"
)

javac %a%

echo Success compiling the code