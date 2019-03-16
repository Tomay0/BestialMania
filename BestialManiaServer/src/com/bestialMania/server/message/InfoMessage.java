package com.bestialMania.server.message;

import com.bestialMania.server.Client;

import java.io.DataOutputStream;
import java.io.IOException;

public class InfoMessage extends OutboundMessage{
    private int id;
    private int maxPlayers;

    /**
     * Create a new info message
     */
    public InfoMessage(Client client, int maxPlayers) {
        super('i');
        this.id = client.getId();
        this.maxPlayers = maxPlayers;
    }


    /**
     * Write a bunch of info about the server to the player joined
     * @param outputStream Data output stream to write to
     */
    @Override
    protected void writeData(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(id);
        outputStream.writeInt(maxPlayers);
    }
}
