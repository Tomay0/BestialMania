package com.bestialMania.server.message;

import com.bestialMania.server.Client;

import java.io.DataOutputStream;
import java.io.IOException;

public class ReadyMessage extends OutboundMessage {
    private boolean ready;
    private int clientId;

    /**
     * Create a new ready message
     */
    public ReadyMessage(Client client, boolean ready) {
        super('r');
        this.ready = ready;
        this.clientId = client.getId();
    }


    /**
     * Write a boolean to indicate if the user is ready
     * @param outputStream Data output stream to write to
     */
    @Override
    protected void writeData(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(clientId);
        outputStream.writeBoolean(ready);
    }

}
