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
    private Logger logger = LoggerFactory.getLogger(Client.class);
    private InetAddress hostname;
    private int port;
    private User user;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;


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
                        send(new Message(user, MessageType.MESSAGE, text));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    public synchronized void send(Message message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* This method is used to connect to server and send a connecting message */
    public void connect() {
        try {
            socket = new Socket(hostname, port);
            os = socket.getOutputStream();
            is = socket.getInputStream();
            oos = new ObjectOutputStream(os);
            ois = new ObjectInputStream(is);

            oos.flush();
            logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            logger.error("Could not Connect");
        }
        Message createMessage = new Message(user, MessageType.CONNECTED);
        send(createMessage);
    }

    /* This method is used to send a disconnecting message */
    public synchronized void disconnect() {
        if (!socket.isConnected()) {
            logger.error("The client is not connected to the server");
        } else {
            Message createMessage = new Message(user, MessageType.DISCONNECTED);
            send(createMessage);
            closeAllConn();
        }

    }

    public void closeAllConn() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (socket.isConnected()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public class ClientHandler extends Thread {
        private Logger logger = LoggerFactory.getLogger(ClientHandler.class);
        @Override
        public void run() {
            logger.info(this.getClass().getName() + " is started in " + Thread.currentThread());
            try {
                while (true) {
                    Message message = null;
                    message = (Message) ois.readObject();
                    if (message != null) {
                        logger.debug("Message recieved:" + message.getMessage() + " MessageType:" + message.getMessageType() + "Name:" + message.getMessageOwnerName());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
