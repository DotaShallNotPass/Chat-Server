
import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected;

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server's address:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server's port:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter your nickname:");
        return ConsoleHelper.readString();
    }

    public class SocketThread extends Thread {
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.NAME_REQUEST) {
                    String name = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, name));

                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;

                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (MessageType.USER_ADDED == message.getType()) {
                    informAboutAddingNewUser(message.getData());
                } else if (MessageType.USER_REMOVED == message.getType()) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }
        public void run(){
            try {
                connection = new Connection(new Socket(getServerAddress(), getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
             notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("Member '" + userName + "' joined to JChat.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Member '" + userName + "' has left JChat.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Failed to send message");
            clientConnected = false;
        }
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        // Помечаем поток как daemon
        socketThread.setDaemon(true);
        socketThread.start();

        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("An error occurred while the client was running.");
            return;
        }

        if (clientConnected)
            ConsoleHelper.writeMessage("Connection established. Type 'exit' to exit.");
        else
            ConsoleHelper.writeMessage("An error occurred while the client was running.");

        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equalsIgnoreCase("exit"))
                break;

            if (shouldSendTextFromConsole())
                sendTextMessage(text);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
