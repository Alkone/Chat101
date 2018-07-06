package ru.devalkone.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.devalkone.User;
import ru.devalkone.exception.DuplicateUsernameException;
import ru.devalkone.message.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static ru.devalkone.message.MessageType.*;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private static final HashMap<String, User> names = new HashMap<>();
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    private static ArrayList<User> users = new ArrayList<>();
    private final int PORT;

    public Server(int port) {
        this.PORT = port;
    }

    public void start() throws IOException {
        logger.info("The chat server inputStream running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        listener.close();
    }

    public class Handler extends Thread {
        private Logger logger;
        private String name;
        private Socket socket;
        private User user;
        private OutputStream outputStream;
        private InputStream inputStream;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;


        public Handler(Socket socket) {
            logger = LoggerFactory.getLogger(Handler.class);
            this.socket = socket;
        }

        public void run() {
            logger.info("Attempting to connect a user...");
            try {
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectInputStream = new ObjectInputStream(inputStream);
                Message firstMessage = (Message) objectInputStream.readObject();
                checkDuplicateUsername(firstMessage);
                writers.add(objectOutputStream);
                sendNotification(firstMessage);
                addToList();

                //Слушаем входящий поток
                while (socket.isConnected()) {
                    Message inputmsg = (Message) objectInputStream.readObject();
                    if (inputmsg != null) {
                        logger.info(inputmsg.getMessageType() + " - " + inputmsg.getMessageOwnerName() + ": " + inputmsg.getMessage());
                        switch (inputmsg.getMessageType()) {
                            case MESSAGE:
                                write(inputmsg);
                                break;
                            case CONNECTED:
                                addToList();
                                break;
                            case DISCONNECTED:
                                closeConnections();
                                break;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (DuplicateUsernameException e) {
                e.printStackTrace();
            }

        }

        /*
        Проверяет на никнейм на уникальность
         */
        private synchronized void checkDuplicateUsername(Message firstMessage) throws DuplicateUsernameException {
            logger.info(firstMessage.getMessageOwnerName() + " inputStream trying to connect");
            if (!names.containsKey(firstMessage.getMessageOwnerName())) {
                this.name = firstMessage.getMessageOwnerName();
                user = new User();
                user.setName(firstMessage.getMessageOwnerName());
                users.add(user);
                names.put(name, user);
                logger.info(name + " has been added to the list");
            } else {
                logger.error(firstMessage.getMessageOwnerName() + " inputStream already connected");
                throw new DuplicateUsernameException(firstMessage.getMessageOwnerName() + " inputStream already connected");
            }
        }

        private void write(Message msg) throws IOException {
            for (ObjectOutputStream writer : writers) {
                msg.setUserlist(names);
                msg.setUsers(users);
                msg.setOnlineCount(names.size());
                writer.writeObject(msg);
                writer.flush();
            }
        }

        private void sendNotification(Message firstMessage) throws IOException {
            String text = "has joined the chat.";
            Message msg = new Message(firstMessage.getMessageOwnerName(), NOTIFICATION, text);
            write(msg);
        }

        private void addToList() throws IOException {
            String text = "Welcome, You have now joined the server!";
            Message msg = new Message("SERVER", CONNECTED, text);
            write(msg);
        }

        private void removeFromList() throws IOException {
            logger.debug("removeFromList() method Enter");
            String text = "has left the chat.";
            Message msg = new Message("SERVER", DISCONNECTED, text);
            msg.setUserlist(names);
            write(msg);
            logger.debug("removeFromList() method Exit");
        }

        /*
        Закрывает открытые потоки и удаляет из списка юзеров
         */
        private synchronized void closeConnections() {
            logger.debug("closeConnections() method Enter");
            logger.info("HashMap names:" + names.size() + " writers:" + writers.size() + " usersList size:" + users.size());
            if (name != null) {
                names.remove(name);
                logger.info("User: " + name + " has been removed!");
            }
            if (user != null) {
                users.remove(user);
                logger.info("User object: " + user + " has been removed!");
            }
            if (objectOutputStream != null) {
                writers.remove(objectOutputStream);
                logger.info("Writer object: " + user + " has been removed!");
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                removeFromList();
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("HashMap names:" + names.size() + " writers:" + writers.size() + " usersList size:" + users.size());
            logger.debug("closeConnections() method Exit");
        }
    }
}
