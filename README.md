# AI Smart Irrigation System

An Artificial Intelligence project that combines **Machine Learning** and **Optimization Algorithms** to build a smart plant watering scheduler.

## Project Overview

This system predicts whether plants need watering using a **Perceptron classifier**, then finds an efficient watering route using **Simulated Annealing**.

The project also includes a graphical user interface (GUI) for plant management, prediction results, and route visualization.

---

## Features

### Perceptron Classification
- Train a Perceptron model using dataset values
- Predict if a plant needs water
- Display model accuracy
- Show learning progress curve

### Simulated Annealing Optimization
- Generate watering routes
- Minimize walking distance
- Reduce unnecessary watering
- Improve route quality iteratively

### GUI Interface
- Add new plants
- Enter plant information:
  - Soil Moisture
  - Last Watered Time
  - Plant Type
  - Coordinates (X,Y)
- Show prediction results
- Display optimized watering route

---

## Technologies Used

- Java
- Java Swing
- Maven
- CSV Dataset
- Perceptron Algorithm
- Simulated Annealing

---

## Project Structure

```text
src/
 ├── main.java
 ├── perceptron.java
 ├── SA.java
 └── plants.java

Data.csv
pom.xml
README.md
