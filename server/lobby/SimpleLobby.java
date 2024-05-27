package server.lobby;

import server.Player;
import server.PlayerState;
import server.Game;
import java.util.HashSet;
import java.util.ArrayList;

public class SimpleLobby implements Lobby {
    private int numPlayers;
    private HashSet<Player> playersWaiting;

    public SimpleLobby(int numPlayers) {
        this.numPlayers = numPlayers;
        this.playersWaiting = new HashSet<Player>();
    }

    public void addPlayer(Player player) {
        synchronized (playersWaiting) {
            player.setState(PlayerState.SIMPLE_LOBBY);
            playersWaiting.add(player);
            System.out.println("Number of players waiting on Simple Lobby: " + playersWaiting.size());
            if(playersWaiting.size() == this.numPlayers) {
                Thread.ofVirtual().start(new Game(new ArrayList<Player>(playersWaiting)));
                playersWaiting.clear();
            }
        }
    }

    public synchronized void removePlayer(Player player) {
        synchronized (playersWaiting) {
            playersWaiting.remove(player);
        }
    }
}
