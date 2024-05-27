package server;

import java.util.*;

public class Game implements Runnable {

    private List<Player> players;
    int numGuesses = 0;
    private Map<Player, Integer> guessDists = new HashMap<>();
    private int totalPlayers;

    public Game(List<Player> players) {
        this.players = players;
        for (Player player : players){
            guessDists.put(player, null);
            player.setState(PlayerState.GAME_WAITING);
        }
        totalPlayers = players.size();
    }

    public void run() {
        this.play();
    }

    private void notifyPlayers(String message) {
        for (Player player : players)
            player.send(message);
    }

    private void play() {
        notifyPlayers("STARTING GAME");

        while (players.size() > 1) {
            notifyPlayers(players.size() + " players remaining.");

            int number = (int) (Math.random() * 101);

            System.out.println("Number = " + number);

            long start = System.currentTimeMillis();

            numGuesses = 0;

            for(Player player : players)
                player.send("PLAY: Place your guess as an integer between 0 and 100.");

            HashSet<Player> playersYetToGuess = new HashSet<>(players);
            for (Player player : players) 
                playersYetToGuess.add(player);

            while (playersYetToGuess.size() > 0) {
                Iterator<Player> it = playersYetToGuess.iterator();

                while (it.hasNext()) {
                    Player player = it.next();
                    player.setState(PlayerState.GAME_GUESSING);
                    int guess = -100;

                    if (System.currentTimeMillis() - start >= 60000) {
                        player.send("GOODBYE: You were kicked of the game due to inactivity...");
                        player.setState(PlayerState.IDLE);
                        guessDists.remove(player);
                        it.remove();
                        players.remove(player);
                        Player.logout(player);
                        continue;
                    }

                    String answer = player.receive();
                    if(answer != null){
                        numGuesses++;
                        try {
                            guess = Integer.parseInt(answer);
                            if (guess < 0 || guess > 100) throw new IllegalArgumentException();
                            player.send("OK: Guess registered");
                        } catch (Exception e) {
                            player.send("PLAY: Your guess is invalid. Please try again, making sure it is an integer between 0 and 100.");
                            guess = -100;
                        }
                        player.setState(PlayerState.GAME_WAITING);
                        guessDists.put(player, Math.abs(guess-number));
                        it.remove();
                    }
                }
            }

            notifyPlayers(generateRoundRank(number));

            Map.Entry<Player, Integer> entry = guessDists.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
            if (entry != null && players.size() >= 2) {
                Player last = entry.getKey();
                int points = totalPlayers - players.size();
                guessDists.remove(last);
                last.send("GOODBYE: You lost. +" + points + " points for you!");
                players.remove(last);
                last.incrementPoints(points);
                last.setState(PlayerState.IDLE);
            }
        }
        if(players.size() == 1){
            Player winner = players.get(0);
            winner.send("GOODBYE: You won. Congratulations! +" + totalPlayers + " points for you!");
            winner.incrementPoints(totalPlayers);
            winner.setState(PlayerState.IDLE);
            guessDists.clear();
            players.clear();
        }
    }

    private String generateRoundRank(int number) {
        StringBuilder ret = new StringBuilder();

        ret.append("Number=" + number);

        List<Map.Entry<Player, Integer>> sorted = this.guessDists.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();

        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<Player, Integer> entry = sorted.get(i);
            ret.append("; #" + i + ":" + entry.getKey().getUsername() + ",dist=" + entry.getValue());
        }

        return ret.toString();
    }
}
