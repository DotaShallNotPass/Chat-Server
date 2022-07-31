import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;

public class OneClassToRuleThemAll {
    public static void main(String[] args) throws Exception {
    new Starter1().start();
    new Starter2().start();
    new Starter3().start();
    }
    private static class Starter1 extends Thread{

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Server's port == 6679");
            try(ServerSocket serverSocket = new ServerSocket(6679)) {
                System.out.println(Inet4Address.getLocalHost().toString().split("/")[1]);
                ConsoleHelper.writeMessage("Server is running");
                while(true){
                    Server.Handler handler = new Server.Handler(serverSocket.accept());
                    handler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static class Starter2 extends Thread{

        @Override
        public void run() {

            Client client = new BotClient();
            client.run();
        }
    }
    private static class Starter3 extends Thread {

        @Override
        public void run() {
            ClientGuiController clientGUI = new ClientGuiController();
            clientGUI.run();
        }
    }
}
