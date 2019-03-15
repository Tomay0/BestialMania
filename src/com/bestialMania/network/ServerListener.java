package com.bestialMania.network;

public interface ServerListener {
    /**
     * Called when you initially connect to the server
     */
    void establishedConnection();

    /**
     * Called when you lose connection with the server
     */
    void lostConnection();
}
