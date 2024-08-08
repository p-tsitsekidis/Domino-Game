package gr.uop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Scanner;

public class Server extends Application {

    @Override
    public void start(Stage stage) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose the game version:");
        System.out.println("1. Command Line");
        System.out.println("2. JavaFX");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                CommandLineGame.main(new String[]{});
                break;
            case 2:
                launchJavaFX(stage);
                break;
            default:
                System.out.println("Invalid choice. Exiting...");
                break;
        }
    }

    private void launchJavaFX(Stage stage) {
        var label = new Label("Hello, JavaFX Server");
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
