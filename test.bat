@echo off
if not exist out mkdir out
javac -d out -sourcepath src src\lms\test\LmsSelfTest.java
if errorlevel 1 (
  echo Compilation failed. Check that the JDK is installed and javac is on the PATH.
  pause
  exit /b 1
)
java -cp out lms.test.LmsSelfTest
pause
