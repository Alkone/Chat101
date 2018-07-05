package ru.devalkone.message;

import ru.devalkone.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {
    private static final long serialVersionUID = 32423538388L;
    private String messageOwnerName;
    private MessageType type;
    private String msg;
    private int count;
    private ArrayList<User> list;
    private ArrayList<User> users;


    public Message(String messageOwnerName, MessageType type) {
        this.messageOwnerName = messageOwnerName;
        this.type = type;
    }

    public Message(String messageOwnerName, MessageType type, String msg) {
        this.messageOwnerName = messageOwnerName;
        this.type = type;
        this.msg = msg;
    }

    public Message(User messageOwner, MessageType type) {
        this(messageOwner.getName(), type);
    }

    public Message(User messageOwner, MessageType type, String msg) {
        this(messageOwner.getName(), type, msg);
    }

    public String getMessageOwnerName() {
        return messageOwnerName;
    }

    public void setMessageOwnerName(String messageOwnerName) {
        this.messageOwnerName = messageOwnerName;
    }

    public MessageType getMessageType() {
        return type;
    }

    public void setMessageType(MessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }

    public int getOnlineCount() {
        return count;
    }

    public void setOnlineCount(int count){
        this.count = count;
    }

    public ArrayList<User> getUserlist() {
        return list;
    }

    public void setUserlist(HashMap<String, User> userList) {
        this.list = new ArrayList<>(userList.values());
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }
}
