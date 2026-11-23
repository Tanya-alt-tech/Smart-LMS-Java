@echo off
if not exist out mkdir out
javac -d out -sourcepath src src\lms\Main.java
if errorlevel 1 (
  echo Compilation failed. Check that the JDK is installed and javac is on the PATH.
  pause
  exit /b 1
)
java -cp out lms.Main
pause
