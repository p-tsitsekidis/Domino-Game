package gr.uop;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The {@code GameplayScreen} class represents the main gameplay UI for the Domino game client.
 * It handles the user interface, game logic, and communication with the server during gameplay.
 *
 * <p>
 * This class uses JavaFX for the UI components and manages the game state based on messages
 * received from the server. It allows the player to interact with the game, view their tiles,
 * the line of play, and respond to game events.
 * </p>
 */
public class GameplayScreen {

    // 1. Network communication
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;

    // 2. Game state information from the server
    private String playerName;
    private String opponentName;
    private String winner;
    private String tiles;
    private String lineOfPlay;
    private String tile;
    private String score;
    private String stockSize;
    private int index;
    private int currentPlayerTiles;
    private int opponentTiles;

    private String data; // Any data received from the server

    // 3. Game logic-related fields
    private boolean yourTurn;
    private boolean stock = false;
    private int invalidMoveSum = 0;

    // HashMaps for command handling
    private Map<String, Runnable> gameCommands = new HashMap<>();

    // 4. JavaFX layout and UI elements
    private Stage primaryStage; // Primary stage for the game
    private Runnable onGameShutdown; // Callback for game shutdown event
    private BorderPane gameLayout;
    private HBox JavaFXlineOfPlay;

    // General Labels
    private Label turnMessageLabel;
    private Label infoLabel;
    private Label stockLabel;

    // Player 1 (Current player) UI elements
    private Label player1Label;
    private HBox player1Info;
    private HBox player1HBoxRectangles;
    private VBox player1Fix;
    private List<StackPane> player1Plates; // Player 1's tiles on UI

    // Player 2 (Opponent) UI elements
    private HBox player2Info;
    private HBox player2HBoxRectangles;
    private VBox player2Fix;
    private Label player2Label;

    /**
     * Constructs a new {@code GameplayScreen} with the specified parameters.
     *
     * @param primaryStage   The primary stage for displaying the game UI.
     * @param socket         The socket connected to the game server.
     * @param playerName     The name of the player.
     * @param opponentName   The name of the opponent player.
     * @param toServer       The output stream to send data to the server.
     * @param fromServer     The input stream to receive data from the server.
     * @param onGameShutdown A callback to be executed when the game is over and needs to shut down.
     */
    public GameplayScreen(Stage primaryStage, Socket socket, String playerName, String opponentName, PrintWriter toServer, Scanner fromServer, Runnable onGameShutdown) {
        this.primaryStage = primaryStage;
        this.socket = socket;
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.toServer = toServer;
        this.fromServer = fromServer;
        this.onGameShutdown = onGameShutdown;
    }

    /**
     * Starts the gameplay screen by initializing the UI components and starting the game loop.
     */
    public void start() {
        initializeCommandMaps();
        setupUI();
        new Thread(this::handleGameLoop).start();
    }

    /**
     * Initializes the mapping of server commands to their corresponding handler methods.
     */
    private void initializeCommandMaps() {
        gameCommands.put("TURN", this::handleTurn);
        gameCommands.put("TILES", () -> handleTiles(data));
        gameCommands.put("BOARD", () -> handleLineOfPlay(data));
        gameCommands.put("STOCK_SIZE", () -> handleStock(data));
        gameCommands.put("OPPONENT_TILE_SIZE", () -> handleTileSize(data));
        gameCommands.put("WAIT_OPPONENT_MOVE", this::handleWaitForMove);
        gameCommands.put("NO_AVAILABLE_MOVES", this::handleNoAvailableMoves);
        gameCommands.put("DRAW", () -> handleDraw(data));
        gameCommands.put("OPPONENT_DRAW", this::handleOppDraw);
        gameCommands.put("PLAYED", () -> handlePlayed(data));
        gameCommands.put("OPP_PLAYED", () -> handleOppPlayed(data));
        gameCommands.put("PASS", this::handlePass);
        gameCommands.put("OPP_PASS", this::handleOppPass);
        gameCommands.put("INDEX", () -> handleIndex(index));
        gameCommands.put("INVALID_MOVE", this::handleInvalidMove);
        gameCommands.put("INVALID_INPUT", this::handleInvalidInput);
        gameCommands.put("GAME_OVER", () -> handleGameOver(data));
        gameCommands.put("SCORE", () -> handleScore(data));
    }

    /**
     * Sets up the initial UI components for the gameplay screen.
     */
    private void setupUI() {
        startGameScreen();

        Scene gameScene = new Scene(gameLayout, 1200, 650);

        // Switch the scene on the same primary stage
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Domino Game - Playing");
        primaryStage.show();
    }

    /**
     * Initializes the game screen layout and UI elements.
     */
    private void startGameScreen() {
        this.gameLayout = new BorderPane();

        // this.gameLayout.setCenter(new Label("elaaaaaa"));

        this.player1Label = new Label();
        this.player1Label.setStyle("-fx-font-size: 17px;");

        this.player1Info = new HBox(10);
        this.player1Info.setAlignment(Pos.CENTER);

        this.player1HBoxRectangles = new HBox(8);
        this.player1HBoxRectangles.setAlignment(Pos.CENTER);

        this.player1Fix = new VBox(10);
        this.player1Fix.setAlignment(Pos.CENTER);

        // ---------------------------------------------------------------------------------

        this.infoLabel = new Label();

        // ---------------------------------------------------------------------------------

        this.player2Label = new Label();
        this.player2Label.setStyle("-fx-font-size: 17px;");

        this.player2Info = new HBox(10);
        this.player2Info.setAlignment(Pos.CENTER);

        this.player2HBoxRectangles = new HBox(8);
        this.player2HBoxRectangles.setAlignment(Pos.CENTER);

        this.player2Fix = new VBox(20);
        this.player2Fix.setAlignment(Pos.CENTER);

        this.JavaFXlineOfPlay = new HBox(6);

        this.turnMessageLabel = new Label();
        this.turnMessageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");

        VBox lineOfPlayContainer = new VBox();
        lineOfPlayContainer.setAlignment(Pos.CENTER);
        lineOfPlayContainer.getChildren().addAll(turnMessageLabel, JavaFXlineOfPlay);
        this.gameLayout.setCenter(lineOfPlayContainer);

        this.player1Info.getChildren().add(this.player1Fix);
        this.player2Info.getChildren().add(this.player2Fix);

        this.stockLabel = new Label();
        this.stockLabel.setStyle("-fx-font-size: 13px;");

        this.player1Fix.getChildren().addAll(player1Label, stockLabel, player1HBoxRectangles, this.infoLabel);
        this.player2Fix.getChildren().addAll(player2Label, player2HBoxRectangles);

        this.yourTurn = false;
        this.player1Plates = new ArrayList<>();

        // this.player1Fix.getChildren()
        // this.gameLayout.setRight(stockLabel);
    }

    /**
     * Handles the main game loop by processing messages from the server.
     */
    private void handleGameLoop() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            if(serverMessage.contains(" ")) {
                this.data = serverMessage.substring(serverMessage.indexOf(" ") + 1);
                serverMessage = serverMessage.substring(0, serverMessage.indexOf(" "));
            }

            processServerMessage(serverMessage);

            if (serverMessage.equals("SCORE")) {
                break;
            }
        }
    }

    /**
     * Processes a server message by looking up and executing the corresponding command handler.
     *
     * @param serverMessage The command received from the server.
     */
    private void processServerMessage(String serverMessage) {
        // Look up the command in the gameCommands map
        Runnable command = gameCommands.get(serverMessage);
        if (command != null) {
            command.run();
        }
    }

    // ------------------------------------------ GAMEPLAY HANDLES ---------------------------------------

    /**
     * Handles the "TURN" command indicating it's the player's turn.
     */
    private void handleTurn() {
        this.yourTurn = true;
        // gameLayout.setStyle("-fx-background-color: lightgreen;");

        Platform.runLater(() -> {
            this.turnMessageLabel.setText("It's your turn!");
        });

        System.out.println("It's your turn [" + playerName + "]!");
    }

    /**
     * Handles the "STOCK_SIZE" command by updating the stock size.
     *
     * @param data The stock size data from the server.
     */
    private void handleStock(String data) {
        this.stockSize = data;
        Platform.runLater(() -> {
            int size = Integer.parseInt(stockSize);
            stockLabel.setText("Stock: " + size);
        });
    }

    /**
     * Handles the "OPPONENT_TILE_SIZE" command by updating the opponent's tile count.
     *
     * @param data The data containing the opponent's tile count.
     */
    private void handleTileSize(String data) {
        this.opponentTiles = Integer.parseInt(data);
    }

    /**
     * Handles the "TILES" command by updating the player's tiles.
     *
     * @param data The data containing the player's tiles.
     */
    private void handleTiles(String data) {
        this.tiles = data;
        Platform.runLater(() -> {
            this.player1HBoxRectangles.getChildren().clear();
            this.player2HBoxRectangles.getChildren().clear();
        });

        HBox player1HBoxRectangles2 = new HBox(8);
        HBox player2HBoxRectangles2 = new HBox(8);

        this.tiles = this.tiles.replace("[", "").replace("]", "").trim();
        String[] pairs2 = this.tiles.split(", ");

        Platform.runLater(() -> {
            player1Label.setText(playerName + " (Tiles: " + pairs2.length + ")");
        });

        for (int i = 0; i < pairs2.length; i++) {
            String tile = pairs2[i].trim();
            String[] numbers = tile.split(":");

            HBox plate_hBox = new HBox(20);
            StackPane plate = createDominoPlate(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), "PLAYER1", "" + i + "");

            this.player1Plates.add(plate);

            Platform.runLater(() -> {
                plate_hBox.getChildren().add(plate);
                player1HBoxRectangles2.getChildren().add(plate_hBox);
                this.player1HBoxRectangles = player1HBoxRectangles2;
            });
        }
        
        Platform.runLater(() -> {
            if (player1Fix.getChildren().size() == 4)
                player1Fix.getChildren().set(2, player1HBoxRectangles);

            this.gameLayout.setTop(player1Info);

            this.primaryStage.setMinHeight(Math.max(this.primaryStage.getMinHeight(), calculatePlayerHeight(player1HBoxRectangles)));
            this.primaryStage.setMinWidth(Math.max(this.primaryStage.getMinWidth(), calculatePlayerWidth(player1HBoxRectangles)));
        });
    
        Platform.runLater(() -> {
            this.player2Label.setText(opponentName + " (Tiles: " + this.opponentTiles + ")");
        });
        
        for (int i = 1; i <= this.opponentTiles; i++) {
            HBox plate_hBox = new HBox(20);
            StackPane plate = createDominoPlate(0, 0, "PLAYER2", "" + -1 + "");
    
            Platform.runLater(() -> {
                plate_hBox.getChildren().add(plate);
                player2HBoxRectangles2.getChildren().add(plate_hBox);
                this.player2HBoxRectangles = player2HBoxRectangles2;
            });
        }
        
        Platform.runLater(() -> {
            if (player2Fix.getChildren().size() == 2)
                player2Fix.getChildren().set(1, player2HBoxRectangles);

            this.gameLayout.setBottom(player2Info);

            this.primaryStage.setMinHeight(Math.max(this.primaryStage.getMinHeight(), calculatePlayerHeight(player2HBoxRectangles)));
            this.primaryStage.setMinWidth(Math.max(this.primaryStage.getMinWidth(), calculatePlayerWidth(player2HBoxRectangles)));
        });


        updatePlateColors();
    }

    /**
     * Handles the "BOARD" command by updating the line of play.
     *
     * @param data The data containing the line of play.
     */
    private void handleLineOfPlay(String data) {
        this.lineOfPlay = data;
        if (!this.lineOfPlay.equals("[]")) {
            Platform.runLater(() -> {
                this.JavaFXlineOfPlay.getChildren().clear();
            });

            this.lineOfPlay = this.lineOfPlay.replace("[", "").replace("]", "").trim();

            String[] pairs = this.lineOfPlay.split(", ");

            for (String pair : pairs) {
                String[] values = pair.split(":");
                
                StackPane plate = createDominoPlateForLineOfPlay(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
                
                Platform.runLater(() -> {
                    this.JavaFXlineOfPlay.getChildren().add(plate);
                });
            }

            Platform.runLater(() -> {
                double lineOfPlayWidth = calculateLineOfPlayWidth();
                this.primaryStage.setMinWidth(lineOfPlayWidth);
            });

            System.out.println("Current Board: " + this.lineOfPlay);
        }
    }

    /**
     * Handles the "WAIT_OPPONENT_MOVE" command indicating waiting for the opponent's move.
     */
    private void handleWaitForMove() {
        // gameLayout.setStyle("-fx-background-color: red;");
        updatePlateColors();

        Platform.runLater(() -> {
            this.turnMessageLabel.setText("Waiting for [" + this.opponentName + "] to make a move.");
        });

        System.out.println("Waiting for [" + this.opponentName + "] to make a move.");
    }

    /**
     * Handles the "NO_AVAILABLE_MOVES" command indicating no available moves and drawing from stock.
     */    
    private void handleNoAvailableMoves() {
        stock = true;
        Platform.runLater(() -> {
            this.infoLabel.setText("No available moves. Drawing from stock...");

            // this.playPauseTransition();
        });

        System.out.println("No available moves. Drawing from stock...");
    }

    /**
     * Handles the "DRAW" command indicating the player drew a tile.
     *
     * @param data The data about the drawn tile.
     */
    private void handleDraw(String data) {
        // tile = data;
        // handleTiles();
        System.out.println("You drew a tile!");
    }

    /**
     * Handles the "OPPONENT_DRAW" command indicating the opponent drew a tile.
     */
    private void handleOppDraw() {  
        System.out.println(opponentName + " drew a tile.");
    }

    /**
     * Handles the "PLAYED" command indicating the player played a tile.
     *
     * @param data The data about the played tile.
     */
    private void handlePlayed(String data) {
        this.tile = data;
        this.yourTurn = false;
        this.invalidMoveSum = 0;

        if (!stock) {
            Platform.runLater(() -> {
                this.infoLabel.setText("You played: " + tile);
                
                // this.playPauseTransition();
            });
        } else stock = false;

        System.out.println("You played: " + tile);
    }

    /**
     * Handles the "OPP_PLAYED" command indicating the opponent played a tile.
     *
     * @param data The data about the opponent's played tile.
     */
    private void handleOppPlayed(String data) {
        this.tile = data;
        this.yourTurn = true;

        if (!stock) {
            Platform.runLater(() -> {
                this.infoLabel.setText("[" + this.opponentName + "] (opponent) played: " + this.tile);
            });
        } else stock = false;

        System.out.println(opponentName + " played: " + tile);
    }

    /**
     * Handles the "PASS" command indicating the player passed their turn.
     */
    private void handlePass() {
        Platform.runLater(() -> {
            this.infoLabel.setText("No valid tiles to play and no more tiles in the stock. Passing turn.");

            // this.playPauseTransition();
        });

        System.out.println("No valid tiles to play and no more tiles in the stock. Passing turn.");
    }

    /**
     * Handles the "OPP_PASS" command indicating the opponent passed their turn.
     */
    private void handleOppPass() {
        Platform.runLater(() -> {
            this.infoLabel.setText("[" + this.opponentName + "] (opponent) has no valid tiles to play and the stock is empty. Passed the turn.");

            // this.playPauseTransition();
        });

        System.out.println(opponentName + " has no valid tiles to play and the stock is empty.");
        System.out.println(opponentName + " passed the turn.");
    }

    /**
     * Handles the "INDEX" command by requesting the player to select a tile index.
     *
     * @param index The index of the tile.
     */
    private void handleIndex(int index) {
        // System.out.println("Enter the index of the tile you want to play: ");
        // index = userInput.nextLine();
        // toServer.println(index);
    }

    /**
     * Handles the "INVALID_MOVE" command indicating the player made an invalid move.
     */
    private void handleInvalidMove() {
        Platform.runLater(() -> {
            this.infoLabel.setText("Invalid move. Choose a different tile. (" + (++invalidMoveSum) + ")");
        });

        System.out.println("Invalid move. Choose a different tile.");
    }

    /**
     * Handles the "INVALID_INPUT" command indicating the player provided invalid input.
     */
    private void handleInvalidInput() {
        System.out.println("Invalid input or tile index. Try again.");
    }

    /**
     * Handles the "GAME_OVER" command indicating the game is over.
     *
     * @param data The data containing the winner's name.
     */
    private void handleGameOver(String data) {
        this.winner = data;

        System.out.println("\nGame over! The winner is: " + winner + "!");
    }

    private void handleScore(String data) {
        this.score = data;
        showWinnerDialog(this.winner, "Your score: " + this.score);
    }

    // ----------------------- JAVA FX FUNCTIONS ---------------------------------

    /**
     * Plays a pause transition to clear informational messages after a delay.
     */
    private void playPauseTransition() {
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(event -> {
            this.infoLabel.setText("");
        });

        pause.play();
    }

    /**
     * Displays a dialog showing the winner and the score.
     *
     * @param winner The name of the winning player.
     * @param score  The score message to display.
     */
    private void showWinnerDialog(String winner, String score) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(winner.equals(this.playerName) ? "🎉 Congratulations! 🎉" : "You lost!");
            alert.setContentText("Winner: " + winner + "\n" + score);
            alert.showAndWait();

            System.out.println("You scored: " + score + " points!");
            onGameShutdown.run();
        });
    }

    /**
     * Calculates the minimum height required for displaying the player's tiles.
     *
     * @param playerHBox The HBox containing the player's tiles.
     * @return The minimum height required.
     */
    private double calculatePlayerHeight(HBox playerHBox) {
        return playerHBox.getChildren().size() > 0 ? 200 * 2 : 0;
    }

    /**
     * Calculates the minimum width required for displaying the player's tiles.
     *
     * @param playerHBox The HBox containing the player's tiles.
     * @return The minimum width required.
     */
    private double calculatePlayerWidth(HBox playerHBox) {
        return playerHBox.getChildren().size() * 60 + (playerHBox.getChildren().size() - 1) * 20;
    }

    /**
     * Calculates the total width of the line of play.
     *
     * @return The total width required for the line of play.
     */
    private double calculateLineOfPlayWidth() {
        double totalWidth = 0;
        int numberOfTiles = this.JavaFXlineOfPlay.getChildren().size();
        totalWidth = (numberOfTiles * 69) + ((numberOfTiles - 1) * 6);

        return totalWidth;
    }

    /**
     * Creates a domino tile plate as a StackPane for the player or line of play.
     *
     * @param leftNumber  The left number on the tile.
     * @param rightNumber The right number on the tile.
     * @param type        The type indicating "PLAYER1", "PLAYER2", or line of play.
     * @param index       The index of the tile.
     * @return A StackPane representing the domino tile.
     */
    private StackPane createDominoPlate(int leftNumber, int rightNumber, String type, String index) {
        StackPane stackPanePlate = createRectangle(leftNumber, rightNumber, index);

        if (type.equals("PLAYER1") && yourTurn) {
            stackPanePlate.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                String plateIndex = (String) stackPanePlate.getUserData();
                toServer.println(plateIndex);
            });

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), stackPanePlate);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            scaleTransition.setCycleCount(Animation.INDEFINITE);
            scaleTransition.setAutoReverse(true);
            scaleTransition.play();
        }

        if (type.equals("PLAYER2")) stackPanePlate.setDisable(true);

        return stackPanePlate;
    }

    /**
     * Creates a domino tile plate as a StackPane for the line of play.
     *
     * @param leftNumber  The left number on the tile.
     * @param rightNumber The right number on the tile.
     * @return A StackPane representing the domino tile.
     */
    private StackPane createDominoPlateForLineOfPlay(int leftNumber, int rightNumber) {
        return createRectangle(leftNumber, rightNumber, " ");
    }

    /**
     * Creates a rectangle representing a domino tile.
     *
     * @param leftNumber The left number on the tile.
     * @param rightNumber The right number on the tile.
     * @param index The index of the tile.
     * @return A StackPane containing the domino tile.
     */
    private StackPane createRectangle(int leftNumber, int rightNumber, String index) {
        Rectangle rectangle = new Rectangle(60, 70, Color.BLACK);
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);
        // rectangle.setStroke(Color.BLACK);

        VBox leftPane = createCirclePane(leftNumber);
        VBox rightPane = createCirclePane(rightNumber);

        HBox circles_hBox = new HBox();
        circles_hBox.setAlignment(Pos.CENTER);

        
        if (!index.equals("-1")) {
            Line line = new Line(100, 0, 100, 50);
            line.setStroke(Color.WHITE);

            Platform.runLater(() -> {
                circles_hBox.getChildren().addAll(leftPane, line, rightPane);
            });
        } else {
            Platform.runLater(() -> {
                circles_hBox.getChildren().addAll(leftPane, rightPane);
            });
        }

        StackPane stackPanePlate = new StackPane();
        Platform.runLater(() -> {
            stackPanePlate.getChildren().addAll(rectangle, circles_hBox);
        });

        if (index != " ") stackPanePlate.setUserData(index);

        return stackPanePlate;
    }

    /**
     * Creates a VBox containing circles representing the pips on a domino tile side.
     *
     * @param number The number of pips to display.
     * @return A VBox containing the pips.
     */
    private VBox createCirclePane(int number) {
        VBox pane = new VBox();
        pane.setPrefWidth(30);
        pane.setAlignment(Pos.CENTER);

        double circleRadius = 3.8;
        double availableWidth = 27;
        double spaceBetweenCircles = 0;

        if (number > 1) {
            spaceBetweenCircles = (availableWidth - (circleRadius * number)) / (number);
        }

        for (int i = 0; i < number; i++) {
            Circle circle = new Circle(circleRadius, Color.WHITE);
            
            if (i > 0) {
                pane.setSpacing(spaceBetweenCircles);
            }

            // Platform.runLater(() -> {
                pane.getChildren().add(circle);
            // });
        }

        return pane;
    }

    /**
     * Updates the colors of the player's tiles based on whether it's their turn.
     */
    private void updatePlateColors() {
        for (StackPane plate : this.player1Plates) {
            Platform.runLater(() -> {
                Rectangle rectangle = (Rectangle) plate.getChildren().get(0);
                rectangle.setFill(this.yourTurn ? Color.LIGHTGREEN : Color.RED);
            });
        }
    }
}
