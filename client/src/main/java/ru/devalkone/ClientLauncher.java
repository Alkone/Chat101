package ru.devalkone;

import ru.devalkone.client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class ClientLauncher {
    public static void main(String[] args) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("Enter your nickname: ");
        User user1 = new User(bufferedReader.readLine());
        Client client1 = new Client(InetAddress.getLocalHost(), 5001, user1);
        client1.start();
    }

}
