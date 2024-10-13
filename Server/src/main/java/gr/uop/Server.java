package gr.uop;

import java.util.Scanner;

/**
 * The Server class serves as the main entry point for choosing between local (command line) or networked (server-client) gameplay.
 * It prompts the user for a choice and either starts the local command line game or the networked server game.
 */
public class Server {

    /**
     * The main method that starts the server and prompts the user to choose between local or networked gameplay.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose the game version:");
        System.out.println("1. Command Line (Local)");
        System.out.println("2. Networked Server");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); //Consume newline

        switch (choice) {
            case 1:
                runCommandLineGame();  //Run command line game locally
                break;
            case 2:
                runNetworkedServer();  //Run command line game on the networked server
                break;
            default:
                System.out.println("Invalid choice. Exiting...");
                break;
        }
        scanner.close();
    }

    /**
     * Starts the local command line game.
     * It invokes the CommandLineGame's main method to run the game locally.
     */
    private static void runCommandLineGame() {
        CommandLineGame.main(new String[]{});
    }

    /**
     * Starts the networked server game.
     * It creates an instance of the DominoServer and starts it to allow networked play.
     */
    private static void runNetworkedServer() {
        DominoServer server = new DominoServer();
        server.startServer();
    }
}
