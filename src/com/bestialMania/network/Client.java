package com.bestialMania.network;

import com.bestialMania.network.message.OutboundMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client{
    private final String ip;
    private final int port;
    private ServerListener listener;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean connected = false;
    private int id = -1;


    /**
     * Open up a socket connection
     * @param ip IP address
     * @param port port
     * @param listener listener object which handles server messages
     */
    public Client(final String ip, final int port, ServerListener listener) {
        this.ip = ip;
        this.port = port;
        this.listener = listener;

        //open up a new thread and connect to the server
        new Thread(new Runnable() {
            public void run() {
                //connect to the server
                connect();
                readInput();
                //close the socket
                if(socket!=null) {
                    try {
                        socket.close();
                        System.out.println("Connection to server terminated");
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
     * Connect to the server
     */
    public void connect() {
        //connect to server
        try {
            socket = new Socket(ip,port);
            listener.establishedConnection();
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            connected = true;
            System.out.println("Connected to server successfully");
        }catch(UnknownHostException e) {
            //System.err.println("Unknown host");
            listener.lostConnection();
        }catch(IOException e) {
            //System.err.println("Unhandled exception when trying to connect to the server.");
            //e.printStackTrace();
            listener.lostConnection();
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
        finally {
            listener.lostConnection();
            connected = false;
        }
    }

    public void processInput(char c) throws IOException {
        //Get the client's ID
        if(c=='i') {
            int id = inputStream.readInt();
            this.id = id;
            System.out.println("ID = " + id);
        }
        //Add/remove other players
        else if(c=='J') {
            int newID = inputStream.readInt();
            boolean join = inputStream.readBoolean();
            if(listener!=null) {
                if(join) listener.addClient(newID);
                else listener.removeClient(newID);
            }
        }
    }

}
