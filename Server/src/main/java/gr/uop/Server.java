package gr.uop;

import java.util.Scanner;

public class Server {

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

    private static void runCommandLineGame() {
        CommandLineGame.main(new String[]{});
    }

    private static void runNetworkedServer() {
        DominoServer server = new DominoServer();
        server.startServer();
    }
}
