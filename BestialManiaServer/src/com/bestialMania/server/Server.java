package com.bestialMania.server;

import com.bestialMania.server.waiting.WaitingRoom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Server {
    private int currentClientId = 0;

    private ServerSocket socket;
    private boolean running = true;
    private Set<Client> connectedClients = new HashSet<>();
    private WaitingRoom waitingRoom;


    /**
     * Initialize the server by opening a server socket
     */
    public Server() {
        try {
            socket = new ServerSocket(6969);
            System.out.println("Server started successfully");

            //thread for connecting new clients to the server
            new Thread(new Runnable() {
                public void run() {
                    connectClients();
                }
            }).start();

            //thread for the waiting room
            waitingRoom = new WaitingRoom();
            new Thread(waitingRoom).start();

            //main thread used for reading commands
            readInput();

        }catch(IOException e) {
            System.err.println("Server failed to start. Unhandled Exception.");
            e.printStackTrace();
        }
    }

    /**
     * Continuously add clients to the list of clients
     */
    public void connectClients() {
        while(running) {
            try {
                //add a new client to the list
                Socket clientSocket = socket.accept();
                Client client = new Client(this, currentClientId, clientSocket);
                currentClientId++;
                connectedClients.add(client);
                waitingRoom.addClient(client);
                new Thread(client).start();
            }
            //usually occurs if you terminate the server
            catch(SocketException e) {
                //only print an error if the issue is not the socket closing
                if(!e.getMessage().equalsIgnoreCase("socket closed")) {
                    System.err.println("Unhandled exception while connecting new clients");
                    e.printStackTrace();
                }
            }
            //if something goes wrong - don't close the server
            catch(IOException e) {
                System.err.println("Unhandled exception while connecting new clients");
                e.printStackTrace();
            }
        }
    }

    /**
     * Read input typed into the console.
     */
    public void readInput() {
        Scanner scan = new Scanner(System.in);
        while(running) {
            String input = scan.nextLine();
            if(input.equalsIgnoreCase("exit")) {
                running = false;
            }
        }
        terminateServer();
    }

    /**
     * Terminate the server
     */
    public void terminateServer() {
        try {
            System.out.println("Server terminated");
            //close all client sockets
            for(Client client : connectedClients) {
                client.closeSocket();
            }
            waitingRoom.terminate();
            //close server socket
            socket.close();
        }catch(IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Disconnect a client by removing from the list of clients
     */
    public void disconnect(Client client) {
        connectedClients.remove(client);
        waitingRoom.removeClient(client);
    }


    public static void main(String[] args) {
        new Server();
    }
}
