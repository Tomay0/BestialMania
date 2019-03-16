package com.bestialMania.server.waiting;

import com.bestialMania.server.Client;
import com.bestialMania.server.ClientListener;

public class WaitingClient implements ClientListener {
    //if the client has pressed the "everyone ready" button
    private boolean everyoneReady = false;
    private Client client;

    /**
     * Create a client for use in the waiting room
     * By default, they have not pressed the "everyone is ready" button.
     */
    public WaitingClient(Client client) {
        client.setListener(this);
        everyoneReady = false;
        this.client = client;
    }

    /**
     * Get the client
     */
    public Client getClient() {return client;}

    /**
     * Returns if they have pressed the "everyone is ready" button
     */
    public boolean isEveryoneReady() {return everyoneReady;}

    /**
     * Update if the user has pressed the "everyone is ready" button
     */
    @Override
    public void everyoneReady(boolean ready) {
        this.everyoneReady = ready;
    }
}
