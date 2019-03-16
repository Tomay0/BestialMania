package com.bestialMania.network;

import com.bestialMania.network.message.inbound.InboundReadyMessage;
import com.bestialMania.network.message.inbound.InfoMessage;
import com.bestialMania.network.message.inbound.JoinMessage;
import com.bestialMania.network.message.outbound.OutboundMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client{
    private final String ip;
    private final int port;
    private MessageQueue queue = new MessageQueue();
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean connected = false;
    private int id = -1;


    /**
     * Open up a socket connection
     * @param ip IP address
     * @param port port
     */
    public Client(final String ip, final int port) {
        this.ip = ip;
        this.port = port;

        //open up a new thread and connect to the server
        new Thread(new Runnable() {
            public void run() {
                //connect to the server
                connect();
                if(connected) readInput();
                //close the socket
                if(socket!=null) {
                    try {
                        socket.close();
                        //System.out.println("Connection to server terminated");
                    }catch(IOException e) {
                        throw new Error(e);
                    }
                }
            }
        }).start();
    }

    /**
     * Returns if the client is currently connected to the server
     * Becomes false if you exit out or there is an error with connection
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Get the client's ID
     */
    public int getId() {return id;}

    /**
     * Get the message queue
     */
    public MessageQueue getQueue() {return queue;}

    /**
     * Connect to the server
     */
    public void connect() {
        //connect to server
        try {
            socket = new Socket(ip,port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            connected = true;
            queue.postMessage(new JoinMessage(true));
            //System.out.println("Connected to server successfully");
        }catch(UnknownHostException e) {
            //System.err.println("Unknown host");
            queue.postMessage(new JoinMessage(false));
        }catch(IOException e) {
            //System.err.println("Unhandled exception when trying to connect to the server.");
            //e.printStackTrace();
            queue.postMessage(new JoinMessage(false));
        }
    }

    /**
     * Disconnect by stopping the connected loop and closing the socket
     */
    public void disconnect() {
        connected = false;
        if(socket!=null) {
            try {
                socket.close();
            }catch(IOException e) {
                throw new Error(e);
            }
        }
    }

    /**
     * Send a message to the server
     */
    public void sendMessage(OutboundMessage message) {
        message.send(outputStream);
    }

    /**
     * Runs while the client is still connected
     */
    public void readInput() {
        try {
            //process input
            while(connected) {
                char c = inputStream.readChar();
                processInput(c);
            }
        }
        //EOFException occurs if the server closes
        catch(EOFException e) {}
        //usually occurs if you terminate the server - check if one of the expected errors first
        catch(SocketException e) {
            if(!e.getMessage().equals("Socket closed") && !e.getMessage().equalsIgnoreCase("Connection reset")) {
                System.err.println("Unhandled Exception when communicating with server...");
                e.printStackTrace();
            }
        }
        catch(IOException e) {
            System.err.println("Unhandled Exception when communicating with server...");
            e.printStackTrace();
        }
        //disconnect
        finally {
            queue.postMessage(new JoinMessage(false));
            connected = false;
        }
    }

    /**
     * Process an inbound message from the server and post to the message queue
     */
    public void processInput(char c) throws IOException {
        //Server info
        if(c=='i') {
            int id = inputStream.readInt();
            int maxPlayers = inputStream.readInt();
            this.id = id;
            queue.postMessage(new InfoMessage(id,maxPlayers));
        }
        //Add/remove other players
        else if(c=='J') {
            int id = inputStream.readInt();
            boolean join = inputStream.readBoolean();
            queue.postMessage(new JoinMessage(id,join));
        }
        //Player selected everyone ready
        else if(c=='r') {
            int id = inputStream.readInt();
            boolean ready = inputStream.readBoolean();
            queue.postMessage(new InboundReadyMessage(id,ready));
        }
    }

}
