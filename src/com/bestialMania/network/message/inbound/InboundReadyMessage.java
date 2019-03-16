package com.bestialMania.network.message.inbound;

public class InboundReadyMessage extends InboundMessage{
    private int id;
    private boolean ready;
    public InboundReadyMessage(int id, boolean ready) {
        super('r');
        this.id = id;
        this.ready = ready;
    }
    public int getId() {return id;}
    public boolean isReady() {return ready;}
}
