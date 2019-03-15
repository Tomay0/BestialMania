package com.bestialMania.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client implements Runnable{
    private Server server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String clientName;

    /**
     * Create a client
     * @param server Server connected to
     * @param socket Socket object for the client
     */
    public Client(Server server, Socket socket) throws IOException{
        this.server = server;
        this.socket = socket;
        //test name for the client
        clientName = "CLIENT " + socket.getPort();
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println(clientName + ": connected");
    }

    /**
     * Repeatedly check the input from the client
     */
    @Override
    public void run() {
        try {
            //repeatedly process inputs from the client
            String data = (String)inputStream.readUTF();
            while(true) {
                processInput(data);
                data = (String) inputStream.readUTF();
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
     * Close the socket
     */
    public void closeSocket() throws IOException{
        socket.close();
    }

    /**
     * Process a message from the client
     */
    private void processInput(String data) {
        System.out.println(clientName + ": " + data);
    }

    /**
     * Send a string of data to the client
     */
    public void sendData(String data) {
        try {
            outputStream.writeUTF(data);
            outputStream.flush();
        }catch(IOException e) {
            throw new Error(e);
        }
    }
}
