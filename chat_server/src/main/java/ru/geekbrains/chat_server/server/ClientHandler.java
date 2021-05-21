package ru.geekbrains.chat_server.server;

import ru.geekbrains.april_chat.common.ChatMessage;
import ru.geekbrains.april_chat.common.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private static final long AUTH_TIMEOUT = 20_000;
    private Socket socket;
    private ChatServer chatServer;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String currentUsername;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        try {
            this.chatServer = chatServer;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client handler created!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        new Thread(() -> {
            try {
                handleClientMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleClientMessages() throws IOException {
        while (!Thread.currentThread().isInterrupted() || socket.isConnected()) {
            String msg = inputStream.readUTF();
            ChatMessage message = ChatMessage.unmarshall(msg);

            switch (message.getMessageType()) {
                case PUBLIC:
                case PRIVATE:
                    readMessage(message);
                    break;
                case SEND_AUTH:
                    authenticate(message);
                    break;
                case SEND_REGISTER:
                    register(message);
                    break;
                case CHANGE_USERNAME:
                    updateLogin(message);
                    break;
                case CHANGE_PASSWORD:
                    updatePwd(message);
                    break;
            }
        }
    }

    private void readMessage(ChatMessage message) throws IOException {
        try {
                message.setFrom(this.currentUsername);
                switch (message.getMessageType()) {
                    case PUBLIC:
                        chatServer.sendBroadcastMessage(message);
                        break;
                    case PRIVATE:
                        chatServer.sendPrivateMessage(message);
                        break;
                }
        }  finally {
            closeHandler();
        }
    }

    public void sendMessage(ChatMessage message) {
        try {
            outputStream.writeUTF(message.marshall());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentName() {
        return this.currentUsername;
    }

    private void register(ChatMessage msg) {
        System.out.println("Started client  register");

        if(chatServer.getAuthService().registerUser(msg.getLogin(), msg.getPassword()))
            return;

        ChatMessage response = new ChatMessage();
        response.setMessageType(MessageType.ERROR);
        response.setBody("Error while register");
        sendMessage(response);

    }

    private void updatePwd(ChatMessage msg) {
        System.out.println("Started client update pwd");

        if(chatServer.getAuthService().changePassword(msg.getLogin(), msg.getPassword()))
            return;

        ChatMessage response = new ChatMessage();
        response.setMessageType(MessageType.ERROR);
        response.setBody("Error while creating");
        sendMessage(response);

    }

    private void updateLogin(ChatMessage msg) {
        System.out.println("Started client update login");

        ChatMessage response = new ChatMessage();
        response.setMessageType(MessageType.ERROR);
        response.setBody("Currently unsupproted");
        sendMessage(response);
    }

    private void authenticate(ChatMessage msg) {
        System.out.println("Started client  auth...");

        String username = chatServer.getAuthService().getUsernameByLoginAndPassword(msg.getLogin(), msg.getPassword());
        ChatMessage response = new ChatMessage();

        if (username == null) {
            response.setMessageType(MessageType.ERROR);
            response.setBody("Wrong username or password!");
            System.out.println("Wrong credentials");
        } else if (chatServer.isUserOnline(username)) {
            response.setMessageType(MessageType.ERROR);
            response.setBody("Double auth!");
            System.out.println("Double auth!");
        } else {
            response.setMessageType(MessageType.AUTH_CONFIRM);
            response.setBody(username);
            currentUsername = username;
            chatServer.subscribe(this);
            System.out.println("Subscribed");
            sendMessage(response);
        }
        sendMessage(response);
    }

    public void closeHandler() {
        try {
            chatServer.unsubscribe(this);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
