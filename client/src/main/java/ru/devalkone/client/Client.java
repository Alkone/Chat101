package ru.devalkone.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.devalkone.User;
import ru.devalkone.message.Message;
import ru.devalkone.message.MessageType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private InetAddress hostname;
    private int port;
    private User user;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public Client(InetAddress hostname, int port, User user) {
        this.hostname = hostname;
        this.port = port;
        this.user = user;
    }

    public void start() {
        connect();
        new ClientHandler().start();
        logger.info("Sockets <in> and <out> ready!");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String text = reader.readLine();
            switch (text) {
                case "/disconnect":
                    disconnect();
                    break;
                case "/connect":
                    connect();
                    break;
                default:
                    if(!text.isEmpty()) {
                        send(new Message(user, MessageType.MESSAGE, text));
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void send(Message message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            socket = new Socket(hostname, port);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectInputStream = new ObjectInputStream(inputStream);

            objectOutputStream.flush();
            logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            logger.error("Could not Connect");
        }
        Message createMessage = new Message(user, MessageType.CONNECTED);
        send(createMessage);
    }

    public synchronized void disconnect() {
        if (!socket.isConnected()) {
            logger.error("The client inputStream not connected to the server");
        } else {
            Message createMessage = new Message(user, MessageType.DISCONNECTED);
            send(createMessage);
            closeAllConnections();
        }

    }

    public void closeAllConnections() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket.isConnected()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class ClientHandler extends Thread {
        @Override
        public void run() {
            logger.info(this.getClass().getName() + " inputStream started in " + Thread.currentThread());
            try {
                while (true) {
                    Message message = null;
                    message = (Message) objectInputStream.readObject();
                    if (message != null) {
                        System.out.println(message.getMessageType() + "__Message recieved from " + message.getMessageOwnerName() + ": " + message.getMessage());

                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
