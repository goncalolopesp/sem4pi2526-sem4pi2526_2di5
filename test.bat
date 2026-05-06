@echo off

echo [=================================]
echo [            T E S T S            ]
echo [=================================]

call mvn test

if %errorlevel% neq 0 (
    echo.
    echo [X] WARNING: TESTS FAILED! Check logs above.
    echo [=================================]
) else (
    echo.
    echo [V] SUCCESS: ALL THE TESTS PASSED! Ready to push.
    echo [=================================]
)

echo.
pause