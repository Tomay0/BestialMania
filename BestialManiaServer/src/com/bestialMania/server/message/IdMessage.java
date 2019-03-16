package com.bestialMania.server.message;

import com.bestialMania.server.Client;

import java.io.DataOutputStream;
import java.io.IOException;

public class IdMessage extends OutboundMessage{
    private int id;

    /**
     * Create a new ready message
     */
    public IdMessage(Client client) {
        super('i');
        this.id = client.getId();
    }


    /**
     * Write an integer to tell the client what their id is
     * @param outputStream Data output stream to write to
     */
    @Override
    protected void writeData(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(id);
    }
}
