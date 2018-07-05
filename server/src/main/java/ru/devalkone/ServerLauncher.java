package ru.devalkone;

import ru.devalkone.server.Server;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        Server server = new Server(5001);
        server.start();
    }

}
