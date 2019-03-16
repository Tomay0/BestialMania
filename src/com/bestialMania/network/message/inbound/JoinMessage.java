package com.bestialMania.network.message.inbound;

public class JoinMessage extends InboundMessage {
    private boolean self;
    private int id = -1;
    private boolean joined;

    /**
     * Create a join message for when another player joins the waiting room
     * @param id id of the player that joined
     * @param joined if they joined or left
     */
    public JoinMessage(int id,boolean joined) {
        super('J');
        self = false;
        this.id = id;
        this.joined = joined;
    }

    /**
     * Create a join message for when this player has successfully or unsuccessfully connected to the server
     * @param joined if the player joined
     */
    public JoinMessage(boolean joined) {
        super('J');
        self = true;
        this.joined = joined;
    }

    /**
     * Return if the player that joined/left was yourself
     */
    public boolean getSelf() {
        return self;
    }

    /**
     * Return the id of the player that joined/left
     */
    public int getId() {
        return id;
    }

    /**
     * Return if the player joined/left
     */
    public boolean getJoined() {
        return joined;
    }
}
