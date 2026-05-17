@echo off

if exist target rmdir /s /q target
mkdir target\classes

for /r src\main\java %%f in (*.java) do (
    javac -d target\classes "%%f"
)

echo Success compiling the code