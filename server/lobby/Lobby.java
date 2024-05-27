package server.lobby;

import server.Player;

public interface Lobby {
    Iterable<Player> playersWaiting = null;

    public void addPlayer(Player player);

    public void removePlayer(Player player);

    public default void notifyPlayers(String message) {
        for (Player player : playersWaiting)
            player.send(message);
    }
}
