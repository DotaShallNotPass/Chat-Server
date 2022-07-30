import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

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
            if (split.length != 2) return;

            String messageWithoutUserName = split[1];
            String userName = split[0];

            String base64 = "";
            int counter = 0;
            if(messageWithoutUserName.length() > 50)
            {
                base64 += message;
                counter++;
                System.out.println("base64+= | counter == "+counter);
            }
        }
    }
}
/*
if (counter +1 >= 2)
                {
                    System.out.println("count >= 2");
                    Base64.Decoder b64 = Base64.getDecoder();
                    byte[] decodedByteArray = b64.decode(base64);
                    BufferedImage image;
                    ByteArrayInputStream bis = new ByteArrayInputStream(decodedByteArray);
                    System.out.println("before try");
                    try {
                        image = ImageIO.read(bis);
                        File outputfile = new File("image.jpeg");
                        System.out.println("before imageio");
                        ImageIO.write(image, "jpeg", outputfile);
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                    base64 = "";
                    counter = 0;
                }
 */