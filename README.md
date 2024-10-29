# Java Domino Game

Welcome to the Java Domino Game! This project is a networked implementation of the classic domino game, allowing two players to play against each other either on the same machine or over a network. The game includes both a command-line interface and a graphical user interface (GUI) built with JavaFX.

## Features

- **Two-player Gameplay**: Play against another human player locally or over a network.
- **Networked Game Server**: Host a game server to allow clients to connect and play remotely.
- **Graphical User Interface**: Enjoy the game with a user-friendly GUI built using JavaFX.
- **Command-line Interface**: Option to play the game using a simple command-line interface.
- **Game Logic**: Full implementation of domino game rules, including drawing from the stock, passing turns, and calculating scores.
- **Javadoc Documentation**: Comprehensive documentation is available within the code for deeper understanding.

## Getting Started

These instructions will help you set up the project on your local machine for development and testing purposes.

### Prerequisites

- **Java Development Kit (JDK) 8 or higher**: Ensure that Java is installed on your system.
- **JavaFX SDK**: Required for running the GUI version of the client.

### Installation

1. **Clone the Repository**

   ```bash
   git clone https://github.com/yourusername/java-domino-game.git

2. **Navigate to the Project Directory**

   ```bash
   cd java-domino-game

## How to Play
The game consists of a server and clients. You need to start the server first and then run the client(s).

1. Run the Server.java file

2. Choose Game Mode.

   ```bash
   Choose the game version:
   1. Command Line (Local)
   2. Networked Server

3. Run the Client: 
You can run the client either as a command-line application or as a JavaFX GUI application.

   - Run CommandLineClient.java or JavaFXClient.java for each of the clients.

   - Enter the server's IP address when prompted (e.g., ```localhost``` if running on the same machine).

   - Enter your player name.

   - Wait for the opponent player to join the game.

   - > **Note:** By default the socket that the server and the clients communicate is ```7777```

## Game Rules

1. **Objective**: The main objective is to be the first player to play all your tiles or have the lowest total pip count when the game ends.

2. **Starting the Game**:
   - Each player is dealt 7 tiles at the start of the game.
   - The player with the highest double or the randomly selected starting player plays first.
   
3. **Gameplay**:
   - Players take turns placing a tile that matches one of the numbers on the open ends of the line of play.
   - A valid move means the tile you place must have at least one value matching an open end of the line.
   
4. **Drawing from the Stock**:
   - If you do not have a valid tile to play, the code automatically draws a tile from the stock.
   - The code will continue to draw tiles until it finds a valid tile to play or the stock is empty.
   
5. **Automatic Tile Playing**:
   - Once a valid tile is drawn from the stock, the code will automatically place it on the line of play. You do not need to manually choose the drawn tile.
   
6. **Passing a Turn**:
   - If the stock is empty and you do not have a valid move, you automatically pass your turn.
   - When a player passes, the opponent continues to play if they have a valid move.

7. **End of Game**:
   - The game ends when one player has no tiles left or when both players cannot play and the stock is empty.
   - The player with the lowest pip count wins if the game ends with a block.

8. **Winning and Scoring**:
   - If a player plays all their tiles, they win the game.
   - If neither player can play and the stock is empty, the player with the fewest pips (sum of both sides of remaining tiles) wins.
   - The score is calculated based on the sum of pips in the opponent's hand at the end of the game.

### Important Note

> Pay attention that the code automatically checks each round if you have a valid tile to play. If you don't, it automatically draws from the stock until it draws a valid tile and plays it automatically. You **cannot manually draw** from the stock, and you **cannot manually choose to play** the valid tile you drew.

## Code Structure

### **Client** (`src/main/java/gr/uop`)

- **CommandLineClient.java**: Command-line client for connecting to the networked server.
- **GameplayScreen.java**: JavaFX class managing the main gameplay UI and logic.
- **InitializationScreen.java**: JavaFX class handling server connection and player name input.
- **JavaFXClient.java**: Main entry point for the JavaFX-based client.

### **Server** (`src/main/java/gr/uop`)

- **Server.java**: Entry point for starting either a local or networked game.
- **DominoServer.java**: Manages the networked game server and client communication.
- **CommandLineGame.java**: Command-line game for local play without networking.

### **Game Engine** (`src/main/java/gr/uop/GameEngine`)

- **GameEngine.java**: Core class for managing game state, rules, and logic.
- **Player.java**: Represents a player, holding their tiles and score.
- **Tile.java**: Represents a single domino tile with two values.

---

> **Note**: The code includes detailed Javadoc comments for all classes and methods.

## Credits

Special thanks to [Manos Schoinoplokakis](https://github.com/Manos1Dev) for providing the JavaFX implementation of the game. Their contribution was essential in building the graphical interface for this project.


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.