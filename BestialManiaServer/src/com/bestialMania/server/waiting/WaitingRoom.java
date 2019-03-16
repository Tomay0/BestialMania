package com.bestialMania.server.waiting;

import com.bestialMania.server.Client;
import com.bestialMania.server.Server;
import com.bestialMania.server.message.JoinMessage;
import com.bestialMania.server.message.ReadyMessage;

import java.util.HashMap;
import java.util.Map;

public class WaitingRoom implements Runnable {
    //list of clients waiting to join a game
    private Map<Integer, WaitingClient> waitingClients = new HashMap<>();

    //set to false when the server closes
    private boolean running = true;

    /**
     * Terminate the server, ending the run loop
     */
    public void terminate() {
        this.running = false;
    }

    /**
     * When a new client joins
     */
    public void addClient(Client client) {
        for(WaitingClient wc : waitingClients.values()) {
            //tell all waiting clients that a new client has joined
            wc.getClient().sendMessage(new JoinMessage(client,true));
            //give the new client a list of all currently joined clients
            client.sendMessage(new JoinMessage(wc.getClient(),true));
            //tell the new client that this particular user has pressed "everyone ready"
            if(wc.isEveryoneReady()) client.sendMessage(new ReadyMessage(wc.getClient(),true));
        }

        WaitingClient waitingClient = new WaitingClient(this,client);
        waitingClients.put(client.getId(),waitingClient);
    }

    /**
     * When a client leaves
     */
    public void removeClient(Client client) {
        if(!running) return;//prevent sending messages when removing clients due to server close
        waitingClients.remove(client.getId());
        //write a quit message
        for(WaitingClient wc : waitingClients.values()) {
            wc.getClient().sendMessage(new JoinMessage(client,false));
        }
    }

    /**
     * Main waiting room loop
     * Causes update() to run every 1000ms
     */
    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        int delta = 0;

        while(running) {
            //work out change in time
            long time = System.currentTimeMillis();
            long dt = time-lastTime;
            lastTime = time;
            delta+=dt;

            //update every 1000ms
            while(delta>=1000) {
                update();

                delta-=1000;
            }

            try {
                Thread.sleep(2);
            }catch(InterruptedException e) {
                throw new Error(e);
            }
        }
    }

    /**
     * Update occurs once every 1000ms
     */
    private void update() {
        //Check if all players have pressed the "Everyone is ready button"
        boolean allReady = waitingClients.size()>= Server.MIN_PLAYERS;
        for(WaitingClient client : waitingClients.values()) {
            if(!client.isEveryoneReady()) {
                allReady = false;
                break;
            }
        }

        if(allReady) System.out.println("Everyone is ready: TODO start game.");
    }

    /**
     * When a waiting client presses the "everyone is ready" button
     * @param client
     */
    public void updateEveryoneReady(WaitingClient client) {
        for(WaitingClient wc : waitingClients.values()) {
            if(wc.getClient().getId()!=client.getClient().getId()) {
                wc.getClient().sendMessage(new ReadyMessage(client.getClient(),client.isEveryoneReady()));
            }
        }
    }
}
