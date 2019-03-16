package com.bestialMania.network.message.inbound;

public class InfoMessage extends InboundMessage {
    private int id;
    private int maxPlayers;

    /**
     * Create a new inbound info message with details about the server
     * @param maxPlayers maximum number of players
     */
    public InfoMessage(int id, int maxPlayers) {
        super('i');
        this.id = id;
        this.maxPlayers = maxPlayers;
    }

    /**
     * Get player id
     */
    public int getId() {return id;}

    /**
     * Get maximum amount of players
     */
    public int getMaxPlayers() {return maxPlayers;}
}
