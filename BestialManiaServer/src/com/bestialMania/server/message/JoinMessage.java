package com.bestialMania.server.message;

import com.bestialMania.server.Client;

import java.io.DataOutputStream;
import java.io.IOException;

public class JoinMessage extends OutboundMessage{
    private boolean join;
    private int clientId;

    /**
     * Create a new join/quit message
     */
    public JoinMessage(Client client, boolean join) {
        super('J');
        this.join = join;
        this.clientId = client.getId();
    }


    /**
     * Write a boolean to indicate if the client has joined and their id
     * @param outputStream Data output stream to write to
     */
    @Override
    protected void writeData(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(clientId);
        outputStream.writeBoolean(join);
    }
}
