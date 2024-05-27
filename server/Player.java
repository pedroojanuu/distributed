package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.lang.Math;
import java.util.concurrent.locks.ReentrantLock;

import utils.Pair;

public class Player {
    // represents a Player from the perspective of the server

    private static Map<Pair<String, String>, Player> loggedPlayers = new HashMap<Pair<String, String>, Player>();
    private static Map<String, Player> playersByToken = new HashMap<String, Player>();

    public static ReentrantLock lockLoggedPlayers = new ReentrantLock();
    public static ReentrantLock lockPlayersByToken = new ReentrantLock();

    private static ReentrantLock databaseLock = new ReentrantLock();

    private ReentrantLock lockPlayer = new ReentrantLock();

    private String currentToken;
    private Socket currentSocket;

    private String username;
    private String password;

    private PlayerState state = PlayerState.IDLE;

    private String lastMessage = null;

    private int points = 0;

    public Player(String username, String password, int points) {
        this.username = username;
        this.password = password;
        this.points = points;
    }

    public static Player login(String username, String password, Socket socket) {
        lockLoggedPlayers.lock();
        Player player;
        String point = existsInDatabase(username, password);
        if (loggedPlayers.containsKey(new Pair<String, String>(username, password))) {
            System.out.println("Encontrei");
            player = loggedPlayers.get(new Pair<String, String>(username, password));
        } 
        else if ( point != null) {
            // player = new Player(username, password, (int) (Integer.parseInt(username)* Math.pow(10, Integer.parseInt(username))));
            player = new Player(username, password, Integer.parseInt(point));

            player.lockPlayer.lock();
            player.generateToken();
            player.lockPlayer.unlock();

            loggedPlayers.put(new Pair<String, String>(username, password), player);

            lockPlayersByToken.lock();
            playersByToken.put(player.getToken(), player);
            lockPlayersByToken.unlock();

        } else {
            lockLoggedPlayers.unlock();
            return null;
        }

        lockLoggedPlayers.unlock();

        player.lockPlayer.lock();
        player.currentSocket = socket;
        player.lockPlayer.unlock();

        return player;
    }

    public static Player getPlayerByToken(String token, Socket socket) {
        lockPlayersByToken.lock();
        Player p = playersByToken.get(token);
        lockPlayersByToken.unlock();
        
        p.lockPlayer.lock();
        p.currentSocket = socket;
        p.lockPlayer.unlock();

        return p;
    }

    public static void logout(Player player) {
        lockLoggedPlayers.lock();
        loggedPlayers.remove(new Pair<String, String>(player.username, player.password));
        lockLoggedPlayers.unlock();
    }

    private void generateToken() {
        this.lockPlayer.lock();
        this.currentToken = this.username + Integer.toString((int) (Math.random() * 1000000));
        this.lockPlayer.unlock();
    }

    public String getToken() {
        this.lockPlayer.lock();
        String token = this.currentToken;
        this.lockPlayer.unlock();
        return token;
    }

    private static String existsInDatabase(String username, String password) {
        databaseLock.lock();
        try {
            String working_dir = System.getProperty("user.dir");
            File file = new File(working_dir + "/server/storage/players.csv");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");
                String storedUsername = data[0];
                String storedPassword = data[1];
                
                if (storedUsername.equals(username) && storedPassword.equals(password)) {
                    scanner.close();
                    databaseLock.unlock();
                    return data[2];
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: players.csv file not found.");
        }
        databaseLock.unlock();
        return null;
    }

    public static boolean register(String newUser, String newPassword, Socket socket){
        databaseLock.lock();
        try {
            String working_dir = System.getProperty("user.dir");
            File file = new File(working_dir + "/server/storage/players.csv");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");
                String storedUsername = data[0];
               
                if (storedUsername.equals(newUser)) { //check if userName already exists
                    scanner.close();
                    return false;
                    }
            }
            //if doesnt exist, add new user with password
            scanner.close();
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(newUser + "," + newPassword + ",0");
            writer.newLine();
            writer.close();
            
            return true;
            
        } catch (FileNotFoundException e) {
        System.out.println("Error: players.csv file not found.");
        } catch (IOException e) {
        System.out.println("Error: IOException occurred while accessing players.csv.");
        } finally {
        databaseLock.unlock(); // Sempre chame unlock() no bloco finally
        }

        return false;
    }

    public void send(String message) {
        this.lockPlayer.lock();
        try {
            OutputStream output = this.currentSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println(message);
        } catch (IOException ex) {
            System.out.println("Cannot send message to player " + this.username);
        }
        this.lockPlayer.unlock();
    }

    public PlayerState getState() {
        this.lockPlayer.lock();
        PlayerState retState = this.state;
        this.lockPlayer.unlock();
        return retState;
    }

    public void setState(PlayerState state) {
        this.lockPlayer.lock();
        this.state = state;
        this.lockPlayer.unlock();
    }

    public void setLastMessage(String message) {
        this.lockPlayer.lock();
        this.lastMessage = message;
        this.lockPlayer.unlock();
    }

    public String receive() {
        this.lockPlayer.lock();
        String ret = this.lastMessage;
        this.lastMessage = null;
        this.lockPlayer.unlock();
        return ret;
    }

    public String getUsername() {
        this.lockPlayer.lock();
        String retName = this.username;
        this.lockPlayer.unlock();
        return retName;
    }

    public Socket getSocket() {
        this.lockPlayer.lock();
        Socket socket = this.currentSocket;
        this.lockPlayer.unlock();
        return socket;
    }

    public int getPoints() {
        this.lockPlayer.lock();
        int retPoints = this.points;
        this.lockPlayer.unlock();
        return retPoints;
    }

    
    public void incrementPoints(int pointIncrement) {
        this.lockPlayer.lock();
        this.points += pointIncrement;
        this.lockPlayer.unlock();
        databaseLock.lock();
        try {
            String workingDir = System.getProperty("user.dir");
            File file = new File(workingDir + "/server/storage/players.csv");
            File tempFile = new File(workingDir + "/server/storage/temp_players.csv");
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            
            String currentLine;
            boolean userFound = false;
            
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",");
                String storedUsername = data[0];
                String storedPassword = data[1];
                int points = Integer.parseInt(data[2]);

                if (storedUsername.equals(username) && storedPassword.equals(password)) {
                    points += pointIncrement;
                    
                    userFound = true;
                }
                
                writer.write(storedUsername + "," + storedPassword + "," + points);
                writer.newLine();
            }
            
            reader.close();
            writer.close();

            if (!file.delete()) {
                System.out.println("Error");
                return;
            }

            if (!tempFile.renameTo(file)) {
                System.out.println("Error");
            }

            if (!userFound) {
                System.out.println("User not found or password incorrect.");
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("Error: players.csv file not found.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            databaseLock.unlock();
        }
    }
}
