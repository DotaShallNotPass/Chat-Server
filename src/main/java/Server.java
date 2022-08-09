import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Server's port == 6679");
        try(ServerSocket serverSocket = new ServerSocket(6679)) {
            System.out.println(Inet4Address.getLocalHost().toString().split("/")[1]);
            ConsoleHelper.writeMessage("Server is running");
            while(true){
                Handler handler = new Handler(serverSocket.accept());
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Sending text error: " + connection.getRemoteSocketAddress());
            }
        }
    }
    public static class Handler extends Thread{
    private Socket socket;
    Handler(Socket socket){
        this.socket = socket;

    }
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));

                Message message = connection.receive();
                if (message.getType() != MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("Received new message from " + socket.getRemoteSocketAddress().toString().split("/")[1] + ". Тип сообщения не соответствует протоколу.");
                    continue;
                }

                String userName = message.getData();

                if (userName.isEmpty()) {
                    ConsoleHelper.writeMessage("Attempted to connect to a server with an empty name from " + socket.getRemoteSocketAddress().toString().split("/")[1]);
                    continue;
                }

                if (connectionMap.containsKey(userName)) {
                    ConsoleHelper.writeMessage("An attempt was made to connect to a server with a name already in use from " + socket.getRemoteSocketAddress().toString().split("/")[1]);
                    continue;
                }
                connectionMap.put(userName, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return userName;
            }
        }
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : connectionMap.keySet()) {
                if (name.equals(userName))
                    continue;
                connection.send(new Message(MessageType.USER_ADDED, name));
            }
        }
        private void serverMainLoop(Connection connection, String userName)throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String data = message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + data));
                } else {
                    ConsoleHelper.writeMessage("Received new message from " + socket.getRemoteSocketAddress().toString().split("/")[1] + ". Тип сообщения не соответствует протоколу.");
                }
            }
        }
        public void run(){
            String userName = null;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            ConsoleHelper.writeMessage(String.format("["+LocalTime.now().format(dtf)+"] Connection from %s",socket.getRemoteSocketAddress().toString().split("/")[1]));
            try(Connection connection = new Connection(socket)){
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,userName));
                notifyUsers(connection,userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
            ConsoleHelper.writeMessage(String.format("A data transfer error: %s",socket.getRemoteSocketAddress()));
            }
            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage(String.format("Connection with %s was closed!",socket.getRemoteSocketAddress()));
        }

    }
}


