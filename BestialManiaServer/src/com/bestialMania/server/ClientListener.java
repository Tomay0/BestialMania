package com.bestialMania.server;

public interface ClientListener {
    /**
     * When the "everyone is ready" button is clicked in the waiting room
     */
    void everyoneReady(boolean ready);
}
