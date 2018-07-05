package ru.devalkone;

import ru.devalkone.client.Client;

import java.io.IOException;
import java.net.InetAddress;

public class ClientLauncher {
    public static void main(String[] args) throws IOException {
        User user1 = new User("Test1");
        User user2 = new User("Test3");
        Client client1 = new Client(InetAddress.getLocalHost(), 5001, user2);
        client1.start();
    }

}
