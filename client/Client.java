package client;

import java.awt.event.KeyEvent;
import java.util.*;

import utils.Keyboard;

@SuppressWarnings("resource")

public class Client {
    public static String token;
    public static void main(String[] args) {

        if (args.length < 2){
            System.out.println("Usage: <hostname> <port>");
            return;
        }

        ClientStub stub = new ClientStub();
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        
        try {
            stub.createSocket(hostname, port);
        } catch (Exception e) {
            System.out.println("Could not connect to server.");
            System.out.println("Exiting...");
            return;
        }

        ClientState.State state = ClientState.State.AUTH_MENU;

        while (true) {

            String command = switch (state) {
                case ClientState.State.AUTH_MENU -> authMenu();
                case ClientState.State.REGISTER -> clientRegister();
                case ClientState.State.LOGIN -> clientLogin();
                case ClientState.State.MAIN_MENU -> mainMenu();
                case ClientState.State.LOBBY -> lobby();
                case ClientState.State.IN_GAME_WAIT -> "wait";
                case ClientState.State.IN_GAME_PLAY -> play();
                default -> "exit";
            };

            if (command.equals("exit")) {
                if (state == ClientState.State.AUTH_MENU) {
                    try {
                        stub.send("LOGOUT " + token);
                    } catch (Exception e) {
                        continue;
                    }
                }
                stub.closeSocket();
                return;
            }

            String[] parts = command.split(" ");
            
            String answer;

            if (parts[0].equals("goto")) {
                answer = command;
            } else if (parts[0].equals("wait")) {
                try {
                    answer = stub.receive();
                } catch (Exception e) {
                    continue;
                }
            } else {
                try {
                    stub.send(command);
                } catch (Exception e) {
                    continue;
                }

                try {
                    answer = stub.receive();
                } catch (Exception e) {
                    continue;
                }
            }

            state = ClientState.transition(state, answer);
        }
    }

    private static String authMenu(){
        int option;
        do {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nWelcome!!!\n Select an option:");
            System.out.println("----------------------------");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Option: ");
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Login selected");
                    return "goto login";
                   
                case 2:
                    System.out.println("Register selected");
                    return "goto register";
                    
                case 0:
                    System.out.println("Exiting...");
                    return "exit";
                default:
                    System.out.println("Invalid option. Please select again.");
                    break;
            }
            

        } while (option < -1 || option > 3);
        return "";
    }

    private static String clientLogin() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        return "AUTH " + username + " " + password;
    }

    private static String clientRegister() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter new username: ");
        String newUsername = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        return "REGISTER " + newUsername + " " + newPassword;
    }

    private static String mainMenu() {
        int option;
        do {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nMain Menu\n Select an option:");
            System.out.println("----------------------------");
            System.out.println("1. Join Simple Lobby");
            System.out.println("2. Join Ranked Lobby");
            System.out.println("3. Check my points");
            System.out.println("0. Exit");
            System.out.print("Option: ");
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Join Simple Lobby selected");
                    return "SIMPLE " + token;
                case 2:
                    System.out.println("Join Ranked Lobby selected");
                    return "RANK " + token;
                case 3:
                    System.out.println("Check my points selected");
                    return "POINTS " + token;
                case 0:
                    System.out.println("Exiting...");
                    return "exit";
                default:
                    System.out.println("Invalid option. Please select again.");
                    break;
            }
        } while (option < -1 || option > 2);
        return "";
    }

    private static String lobby() {
        if (Keyboard.pressedKey() == KeyEvent.VK_ESCAPE)
            return "LEAVE_LOBBY " + token;
        else return "wait";
    }

    private static String play() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your guess: ");
        String guess = scanner.nextLine();
        return "PLAY " + token + " " + guess;
    }
}
