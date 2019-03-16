package com.bestialMania.network.message;

import java.io.DataOutputStream;
import java.io.IOException;

public class ReadyMessage extends OutboundMessage {
    private boolean ready;

    /**
     * Create a new ready message
     */
    public ReadyMessage(boolean ready) {
        super('r');
        this.ready = ready;
    }

    /**
     * Write a boolean to indicate if the user is ready
     * @param outputStream Data output stream to write to
     */
    @Override
    protected void writeData(DataOutputStream outputStream) throws IOException {
        outputStream.writeBoolean(ready);
    }
}
