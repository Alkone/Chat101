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
    private static final HashMap<String, User> names = new HashMap<>();
    static Logger logger = LoggerFactory.getLogger(Server.class);
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    private static ArrayList<User> users = new ArrayList<>();
    private final int PORT;

    public Server(int port) {
        this.PORT = port;
    }

    public void start() throws IOException {
        logger.info("The chat server is running.");
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
        private Logger logger = LoggerFactory.getLogger(Handler.class);
        private String name;
        private Socket socket;
        private User user;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private InputStream is;
        private OutputStream os;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            logger.info("Attempting to connect a user...");
            try {
                os = socket.getOutputStream();
                is = socket.getInputStream();
                oos = new ObjectOutputStream(os);
                ois = new ObjectInputStream(is);


                Message firstMessage = (Message) ois.readObject();
                checkDuplicateUsername(firstMessage);
                writers.add(oos);
                sendNotification(firstMessage);
                addToList();

                while (socket.isConnected()) {
                    Message inputmsg = (Message) ois.readObject();
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
                                addToList();
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


        private synchronized void checkDuplicateUsername(Message firstMessage) throws DuplicateUsernameException {
            logger.info(firstMessage.getMessageOwnerName() + " is trying to connect");
            if (!names.containsKey(firstMessage.getMessageOwnerName())) {
                this.name = firstMessage.getMessageOwnerName();
                user = new User();
                user.setName(firstMessage.getMessageOwnerName());
                users.add(user);
                names.put(name, user);
                logger.info(name + " has been added to the list");
            } else {
                logger.error(firstMessage.getMessageOwnerName() + " is already connected");
                throw new DuplicateUsernameException(firstMessage.getMessageOwnerName() + " is already connected");
            }
        }

        private void write(Message msg) throws IOException {
            for (ObjectOutputStream writer : writers) {
                msg.setUserlist(names);
                msg.setUsers(users);
                msg.setOnlineCount(names.size());
                writer.writeObject(msg);
                writer.reset();
            }
        }

        private Message sendNotification(Message firstMessage) throws IOException {
            String text = "has joined the chat.";
            Message msg = new Message(firstMessage.getMessageOwnerName(), NOTIFICATION, text);
            write(msg);
            return msg;
        }

        //For displaying that a user has joined the server
        private Message addToList() throws IOException {
            String text = "Welcome, You have now joined the server!";
            Message msg = new Message("SERVER", CONNECTED, text);
            write(msg);
            return msg;
        }

        private Message removeFromList() throws IOException {
            logger.debug("removeFromList() method Enter");
            String text = "has left the chat.";
            Message msg = new Message("SERVER", DISCONNECTED, text);
            msg.setUserlist(names);
            write(msg);
            logger.debug("removeFromList() method Exit");
            return msg;
        }

        //Once a user has been disconnected, we close the open connections and remove the writers
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
            if (oos != null) {
                writers.remove(oos);
                logger.info("Writer object: " + user + " has been removed!");
            }
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                if (ois != null) {
                    ois.close();
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
