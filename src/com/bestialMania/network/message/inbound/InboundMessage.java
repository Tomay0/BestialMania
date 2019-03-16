package com.bestialMania.network.message.inbound;

public abstract class InboundMessage {
    private char code;

    /**
     * Create an inbound message with a char for its code
     * @param code message code, different for each message
     */
    public InboundMessage(char code) {
        this.code = code;
    }

    /**
     * Get the message code
     */
    public char getCode() {return code;}
}
