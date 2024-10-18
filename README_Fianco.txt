
Fianco
======

Fianco is a board game developed using the libGDX framework. This game involves strategic moves similar to checkers.

Requirements
------------

To run Fianco, ensure you have the following:

- Java Development Kit (JDK) 8 or higher
- Gradle
- libGDX libraries included in the project

How to Launch the Game
----------------------

1. Clone or download the Fianco project from the repository.
2. Open the project in your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse).
3. Navigate to the following path in your project:

   Fianco/lwjgl3/src/main/java/io/github/fianco/lwjgl3/Lwjgl3Launcher.java

4. Run the `Lwjgl3Launcher.java` file to start the game.

Controls
--------

- Click on a specific game mode, and to move the pieces click on them and then on the desired position

Game Rules
----------

TURN - A player must move one of his stones. A stone may:
Move forwards or sideways to an adjacent empty cell.
Capture by jumping (diagonally forward) over an enemy stone, landing on the immediate empty cell. Capturing is mandatory but not multiple (maximum of one captured stone per turn.)
GOAL - A player wins if he places one of his stones on the last row.
