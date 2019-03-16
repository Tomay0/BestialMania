package com.bestialMania.server;

import com.bestialMania.server.message.InfoMessage;
import com.bestialMania.server.message.OutboundMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client implements Runnable{
    private Server server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private int id;
    private String clientName;
    private ClientListener listener = null;
    /**
     * Create a client
     * @param server Server connected to
     * @param id The client's ID as given by the server
     * @param socket Socket object for the client
     */
    public Client(Server server, int id, Socket socket) throws IOException{
        this.server = server;
        this.socket = socket;
        this.id = id;
        //test name for the client
        clientName = "C"+id;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println(clientName + ": connected");
        //tell the client their id
        sendMessage(new InfoMessage(this,Server.MAX_PLAYERS));
    }

    /**
     * Set the listener which handles inbound messages from the client
     */
    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    /**
     * Get the client's ID
     */
    public int getId() {return id;}

    /**
     * Repeatedly check the input from the client
     */
    @Override
    public void run() {
        try {
            //repeatedly process inputs from the client
            while(true) {
                char c = inputStream.readChar();
                processInput(c);
            }
        }
        //disconnect occurs when EOFException is thrown
        catch(EOFException e) {
            System.out.println(clientName + ": disconnected");
        }
        //usually occurs if you terminate the server
        catch(SocketException e) {
            //only print an error if the issue is not the socket closing
            if(e.getMessage().equals("Socket closed")) {
                System.out.println(clientName + ": disconnected due to server close");
                return;
            }
            else if(e.getMessage().equals("Connection reset")) {
                System.out.println(clientName + ": disconnected");
            }
            else {
                System.err.println(clientName + ": Unhandled Exception");
                e.printStackTrace();
            }
        }
        //unhandled exception
        catch(IOException e) {
            System.err.println(clientName + ": Unhandled Exception");
            e.printStackTrace();
        }
        //close the socket and disconnect the client from the server
        finally {
            try {
                socket.close();
            }catch(IOException e) {
                throw new Error(e);
            }
            server.disconnect(this);
        }
    }

    /**
     * Close the socket when the client disconnects
     */
    public void closeSocket() throws IOException{
        socket.close();
    }

    /**
     * Process a message from the client
     */
    private void processInput(char c)  throws IOException{
        switch(c) {
            //Client pressing "Everyone is ready" button
            case 'r':
                boolean ready = inputStream.readBoolean();
                if(listener!=null)listener.everyoneReady(ready);
                break;
        }
    }

    /**
     * Send a message to the client
     */
    public void sendMessage(OutboundMessage message) {
        message.send(outputStream);
    }
}
