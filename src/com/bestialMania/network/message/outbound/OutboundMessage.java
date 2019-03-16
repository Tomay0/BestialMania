package com.bestialMania.network.message.outbound;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class OutboundMessage {
    private char code;

    /**
     * Create a message with a char for its code
     * @param code message code, different for each message
     */
    public OutboundMessage(char code) {this.code = code;}

    /**
     * Send the message
     * @param outputStream The data output stream to write to
     */
    public void send(DataOutputStream outputStream) {
        try {
            outputStream.writeChar(code);
            writeData(outputStream);
            outputStream.flush();
        }catch(IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Write arguments for the individual message
     * @param outputStream Data output stream to write to
     */
    protected abstract void writeData(DataOutputStream outputStream) throws IOException;

}
