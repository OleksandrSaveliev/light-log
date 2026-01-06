Light Log - JavaFX Application
A desktop application built with Java 25 and JavaFX 25.

Quick Start
Build JAR
bash
mvn clean package
Creates: target/light-log-1.0-SNAPSHOT.jar

Run Application

bash
java -jar target/light-log-1.0-SNAPSHOT.jar

Create Windows EXE (Portable)
bash
jpackage --input target --main-jar light-log-1.0-SNAPSHOT.jar --main-class com.tmdna.Main --type app-image --dest target/dist
Result: target/dist/LightLog/LightLog.exe

Create Windows Installer
bash
jpackage --input target --main-jar light-log-1.0-SNAPSHOT.jar --main-class com.tmdna.Main --type exe --dest target/installer
Result: target/installer/LightLog-1.0.exe
