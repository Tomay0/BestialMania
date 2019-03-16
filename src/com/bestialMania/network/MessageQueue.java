package com.bestialMania.network;

import com.bestialMania.network.message.inbound.InboundMessage;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {
    private Queue<InboundMessage> messages = new LinkedList<>();//inbound messages to read in order

    /**
     * Returns if there are any messages in the queue
     */
    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    /**
     * read the latest message in the queue
     */
    public InboundMessage readMessage() {
        return messages.poll();
    }

    /**
     * Add a message to the queue
     */
    public void postMessage(InboundMessage message) {
        messages.offer(message);
    }
}
