package gr.uop;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

public class GameplayScreen {

    // Network communication
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;

    // Information from the server
    private String playerName;
    private String opponentName;
    private String winner;
    private String tiles;
    private String lineOfPlay;
    private String tile;
    private String score;
    private String stock_size;
    private int index;
    
    private String data; // Any data received from the server
    
    // HashMaps for command handling
    private Map<String, Runnable> gameCommands = new HashMap<>();
    
    private Stage primaryStage; // Primary stage
    private Runnable onGameShutdown; // Signals the end of the game
    
    // JavaFX items
    private Label turnMessageLabel;
    
    private List<StackPane> player1Plates;
    private boolean your_turn;
    private BorderPane gameLayout;
    private HBox JavaFXlineOfPlay;

    private int player_tiles_size;
    private Label player1Label;
    private HBox player1HBoxRectangles;
    private VBox player1Fix;
    private HBox player1Info;


    private int opp_tiles_size;
    private HBox player2Info;
    private HBox player2HBoxRectangles;
    private VBox player2Fix;
    private Label player2Label;

    private Label VBox_stock;
    
    public GameplayScreen(Stage primaryStage, Socket socket, String playerName, String opponentName, PrintWriter toServer, Scanner fromServer, Runnable onGameShutdown) {
        this.primaryStage = primaryStage;
        this.socket = socket;
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.toServer = toServer;
        this.fromServer = fromServer;
        this.onGameShutdown = onGameShutdown;
    }

    public void start() {
        initializeCommandMaps();
        setupUI();
        new Thread(this::handleGameLoop).start();
    }

    private void initializeCommandMaps() {
        gameCommands.put("TURN", this::handleTurn);
        gameCommands.put("TILES", () -> handleTiles());
        gameCommands.put("BOARD", () -> handleLineOfPlay());
        gameCommands.put("STOCK_SIZE", () -> handleStock());
        gameCommands.put("WAIT_OPPONENT_MOVE", this::handleWaitForMove);
        gameCommands.put("NO_AVAILABLE_MOVES", this::handleNoAvailableMoves);
        gameCommands.put("DRAW", () -> handleDraw(data));
        gameCommands.put("OPPONENT_DRAW", this::handleOppDraw);
        gameCommands.put("PLAYED", () -> handlePlayed(data));
        gameCommands.put("OPP_PLAYED", () -> handleOppPlayed(data));
        gameCommands.put("PASS", this::handlePass);
        gameCommands.put("OPP_PASS", this::handleOppPass);
        gameCommands.put("INDEX", this::handleIndex);
        gameCommands.put("INVALID_MOVE", this::handleInvalidMove);
        gameCommands.put("INVALID_INPUT", this::handleInvalidInput);
        gameCommands.put("GAME_OVER", () -> handleGameOver(data));
        gameCommands.put("SCORE", () -> handleScore(data));
    }

    private void setupUI() {
        startGameScreen();

        Scene gameScene = new Scene(gameLayout, 1200, 650);
    
        // Switch the scene on the same primary stage
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Domino Game - Playing");
        primaryStage.show();
    }

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
        
        this.VBox_stock = new Label();
        this.VBox_stock.setStyle("-fx-font-size: 13px;");

        this.player1Fix.getChildren().addAll(player1Label, VBox_stock, player1HBoxRectangles);
        this.player2Fix.getChildren().addAll(player2Label, player2HBoxRectangles);

        this.player_tiles_size = 7;
        this.opp_tiles_size = 7;
        this.your_turn = false;
        this.player1Plates = new ArrayList<>();

        // this.player1Fix.getChildren()
        // this.gameLayout.setRight(VBox_stock);
    }

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

    private void processServerMessage(String serverMessage) {
        // Look up the command in the gameCommands map
        Runnable command = gameCommands.get(serverMessage);
        if (command != null) {
            command.run();
        }
    }

    // ------------------------------------------ GAMEPLAY HANDLES ---------------------------------------

    private void handleTurn() {
        this.your_turn = true;
        // gameLayout.setStyle("-fx-background-color: lightgreen;");
        this.turnMessageLabel.setText("It's your turn!");
        System.out.println("It's your turn [" + playerName + "]!");
    }

    private void handleStock() {
        int size = Integer.parseInt(this.stock_size);

        VBox_stock.setText("Stock: " + size);

        // VBox_stock.getChildren().clear();

        // for (int i = 0; i < size; i++) {
        //     StackPane plate = createDominoPlate(0, 0, "PLAYER2", "" + -1 + "");
        //     VBox_stock.getChildren().add(plate);
        // }
    }

    private void handleTiles() {
        this.player1HBoxRectangles.getChildren().clear();
        this.player2HBoxRectangles.getChildren().clear();

        HBox player1HBoxRectangles2 = new HBox(8);
        HBox player2HBoxRectangles2 = new HBox(8);

        this.tiles = this.tiles.replace("[", "").replace("]", "").trim();
        String[] pairs2 = this.tiles.split(", ");
    
        player1Label.setText(playerName + " (Tiles: " + pairs2.length + ")");

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
            if (player1Fix.getChildren().size() == 3)
                player1Fix.getChildren().set(2, player1HBoxRectangles);
                
            this.gameLayout.setTop(player1Info);

            this.primaryStage.setMinHeight(Math.max(this.primaryStage.getMinHeight(), calculatePlayerHeight(player1HBoxRectangles)));
            this.primaryStage.setMinWidth(Math.max(this.primaryStage.getMinWidth(), calculatePlayerWidth(player1HBoxRectangles)));
        });
    
        this.player2Label.setText(opponentName + " (Tiles: " + this.opp_tiles_size + ")");
        
        for (int i = 1; i <= this.opp_tiles_size; i++) {
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

    private void handleLineOfPlay() {
        if (!this.lineOfPlay.equals("[]")) {
            this.JavaFXlineOfPlay.getChildren().clear();
            this.lineOfPlay = this.lineOfPlay.replace("[", "").replace("]", "").trim();
            
            String[] pairs = this.lineOfPlay.split(", ");
            
            for (String pair : pairs) {
                String[] values = pair.split(":");
                
                StackPane plate = createDominoPlateForLineOfPlay(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
                
                this.JavaFXlineOfPlay.getChildren().add(plate);
            }

            double lineOfPlayWidth = calculateLineOfPlayWidth();
            this.primaryStage.setMinWidth(lineOfPlayWidth);

            System.out.println("Current Board: " + this.lineOfPlay);
        }
    }

    private void handleWaitForMove() {
        // gameLayout.setStyle("-fx-background-color: red;");
        updatePlateColors();
        this.turnMessageLabel.setText("Waiting for [" + this.opponentName + "] to make a move.");
        System.out.println("Waiting for [" + this.opponentName + "] to make a move.");
    }
    
    private void handleNoAvailableMoves() {
        System.out.println("No available moves. Drawing from stock...");
    }
    
    private void handleDraw(String data) {
        // tile = data;
        // handleTiles();
        System.out.println("You drew a tile!");
    }

    private void handleOppDraw() {
        this.opp_tiles_size++;    
        System.out.println(opponentName + " drew a tile.");
    }
    
    private void handlePlayed(String data) {
        tile = data;

        this.your_turn = false;
        System.out.println("You played: " + tile);
    }

    private void handleOppPlayed(String data) {
        tile = data;
        this.your_turn = true;
        this.opp_tiles_size--;
        System.out.println(opponentName + " played: " + tile);
    }

    private void handlePass() {
        System.out.println("No valid tiles to play and no more tiles in the stock. Passing turn.");
    }

    private void handleOppPass() {
        System.out.println(opponentName + " has no valid tiles to play and the stock is empty.");
        System.out.println(opponentName + " passed the turn.");
    }

    private void handleIndex() {
        // System.out.println("Enter the index of the tile you want to play: ");
        // index = userInput.nextLine();
        // toServer.println(index);
    }


    private void handleInvalidMove() {
        System.out.println("Invalid move. Choose a different tile.");
    }

    private void handleInvalidInput() {
        System.out.println("Invalid input or tile index. Try again.");
    }

    private void handleGameOver(String data) {
        winner = data;
        System.out.println("\nGame over! The winner is: " + winner + "!");
    }

    private void handleScore(String data) {
        score = data;
        System.out.println("You scored: " + score + " points!");
        onGameShutdown.run();
    }

    // ----------------------- JAVA FX FUNCTIONS ---------------------------------

    private double calculatePlayerHeight(HBox playerHBox) {
        return playerHBox.getChildren().size() > 0 ? 200 * 2 : 0;
    }

    private double calculatePlayerWidth(HBox playerHBox) {
        return playerHBox.getChildren().size() * 60 + (playerHBox.getChildren().size() - 1) * 20;
    }

    private double calculateLineOfPlayWidth() {
        double totalWidth = 0;
        int numberOfTiles = this.JavaFXlineOfPlay.getChildren().size();
        totalWidth = (numberOfTiles * 69) + ((numberOfTiles - 1) * 6);
    
        return totalWidth;
    }

    private StackPane createDominoPlate(int leftNumber, int rightNumber, String type, String index) {
        StackPane stackPanePlate = createRectangle(leftNumber, rightNumber, index);
        
        if (type.equals("PLAYER1") && your_turn) {
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

    private StackPane createDominoPlateForLineOfPlay(int leftNumber, int rightNumber) {
        return createRectangle(leftNumber, rightNumber, " ");
    }

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
         
            circles_hBox.getChildren().addAll(leftPane, line, rightPane);
        } else {
            circles_hBox.getChildren().addAll(leftPane, rightPane);
        }
        
        StackPane stackPanePlate = new StackPane();
        stackPanePlate.getChildren().addAll(rectangle, circles_hBox);

        if (index != " ") stackPanePlate.setUserData(index);

        return stackPanePlate;
    }

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

            pane.getChildren().add(circle);
        }

        return pane;
    }

    private void updatePlateColors() {
        for (StackPane plate : this.player1Plates) {
            Rectangle rectangle = (Rectangle) plate.getChildren().get(0);
            rectangle.setFill(this.your_turn ? Color.LIGHTGREEN : Color.RED);
        }
    }
}
