package ru.geekbrains.chat_server.server;

import ru.geekbrains.april_chat.common.ChatMessage;
import ru.geekbrains.april_chat.common.MessageType;
import ru.geekbrains.chat_server.auth.AuthService;
import ru.geekbrains.chat_server.auth.DatabaseAuthService;
import ru.geekbrains.chat_server.auth.PrimitiveInMemoryAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int PORT = 12256;
    private List<ClientHandler> listOnlineUsers;
    private AuthService authService;

    public ChatServer() {
        this.listOnlineUsers = new ArrayList<>();
        this.authService = new DatabaseAuthService();
    }

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            authService.start();

            while (true) {
                System.out.println("Waiting for connection");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new ClientHandler(socket, this).handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
        }
    }

    private synchronized void sendListOnlineUsers() {
        ChatMessage msg = new ChatMessage();
        msg.setMessageType(MessageType.CLIENT_LIST);
        msg.setOnlineUsers(new ArrayList<>());
        for (ClientHandler user : listOnlineUsers) {
            msg.getOnlineUsers().add(user.getCurrentName());
        }
        for (ClientHandler user : listOnlineUsers) {
            user.sendMessage(msg);
        }
    }

    public synchronized void sendBroadcastMessage(ChatMessage message) {
        for (ClientHandler user : listOnlineUsers) {
            user.sendMessage(message);
        }
    }

    public void sendPrivateMessage(ChatMessage message) {
        for (ClientHandler user : listOnlineUsers) {
          if (user.getCurrentName().equals(message.getTo())) user.sendMessage(message);
        }
    }


    public synchronized boolean isUserOnline(String username) {
        for (ClientHandler user : listOnlineUsers) {
            if (user.getCurrentName().equals(username)) return true;
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        listOnlineUsers.add(clientHandler);
        sendListOnlineUsers();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        listOnlineUsers.remove(clientHandler);
        sendListOnlineUsers();
    }

    public AuthService getAuthService() {
        return authService;
    }

}
