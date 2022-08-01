import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class BotClient extends Client {
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "Декодировщик";
    }

    public static void main(String[] args) {
        Client client = new BotClient();
        client.run();
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            super.clientMainLoop();
        }


        protected void processIncomingMessage(String message) {
            String userNameDelimiter = ": ";
            String[] split = message.split(userNameDelimiter);
            String userName = split[0];
            if (split.length != 2) return;
            File photos = new File("photos");
            if (!photos.exists()) {
                photos.mkdir();
            }
            File usersDir = new File("photos\\" + userName + " photos");
            if (!usersDir.exists()) {
                usersDir.mkdir();
            }

            String messageWithoutUserName = split[1];

            if (messageWithoutUserName.length() > 700) {
                byte[] byteArr = Base64.getDecoder().decode(messageWithoutUserName);
                BufferedImage image;
                ByteArrayInputStream bais = new ByteArrayInputStream(byteArr);
                try{
                    image = ImageIO.read(bais);
                    File result = new File("photos\\"+userName+" photos\\"+Math.random()*99999999+".jpeg");
                    ImageIO.write(image, "jpeg",result);
                }catch (Exception e){
                sendTextMessage("fatal: not a command");
                }
            }
        }
    }
}
