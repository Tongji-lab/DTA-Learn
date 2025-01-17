
# DTAL

A prototype on learning deterministic timed automata.

# Overview

This tool is used to learn deterministic timed automata (DTAs). We have implemented a prototype named DTAL-Tree in JAVA to learn DTAs based on the classification tree. We compared it with the learning algorithm of DTAs from a powerful teacher proposed in the paper ``Learning deterministic multi-clock timed automata", denoted by DTAL-Table. All evaluations have been carried out on an Intel Core-i7 processor with 32GB RAM.

# Installation 

Prerequisite: JDK 1.8 (or higher), Maven 3.6.3, com.microsoft.z3.jar

Installation: just download.

# Usage


## Take DTAL-Table as an example

To take DTAL-Table as an example, the following steps are required:

1. Open TimedAutomata-main and ttaSmartLearning-main in two windows respectively using IntelliJ IDEA.
2. Use Maven to install TimedAutomata-main by clicking the "Maven" button on the right side of the window and the "install" button in turn. If you encounter an error about lombok during the installation process, check the version of lombok in the pom.xml file of TimedAutomata-main.

<img src="./Pictures/Maven.png" style="width: 16em" />

3. Open the ttaSmartLearning-main file, refresh Maven by clicking the "Maven" button on the right side of the window and the "Reload" button in turn, and run the main file named ObservationTableExperiment.java in the path Experements/DTAL-Table/ttaSmartLearning-main/src/main/java/Experiment/ObservationTableExperiment.java. 

<img src="./Pictures/Maven2.png" style="width: 20em" />

If you encounter an error about javafx during the running process, add the following codes in the pom.xml file of ttaSmartLearning-main:
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.2</version> 
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>17.0.2</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-graphics</artifactId>
    <version>17.0.2</version>
</dependency>
```
If you are operating under Linux, replace the "\\" in each file path of the ObservationTableExperiment.java with "/".

## Take DTAL-Tree as an example:

To take DTAL-Table as an example, the following steps are required:

1. Open TimedAutomata-main and ttaSmartLearning-main in two windows respectively using IntelliJ IDEA.
2. Use Maven to install TimedAutomata-main by clicking the "Maven" button on the right side of the window and the "install" button in turn. If you encounter an error about lombok during the installation process, check the version of lombok in the pom.xml file of TimedAutomata-main.
3. Open the ttaSmartLearning-main file, refresh Maven, and run the main file named TreeExperiment.java in the path Experements/DTAL-Tree/ttaSmartLearning-main/src/main/java/Experiment/TreeExperiment.java. If you encounter an error about javafx during the running process, add the following codes in the pom.xml file of ttaSmartLearning-main:
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.2</version> 
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>17.0.2</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-graphics</artifactId>
    <version>17.0.2</version>
</dependency>
```
If you are operating under Linux, replace the "\\" in each file path of the ObservationTableExperiment.java with "/".

