import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static void writeMessage(String message){
        System.out.println(message);
    }
    public static String readString(){
        String s="";
        boolean isRead = false;
        while (!isRead)  {
            try {
                s = reader.readLine();
                isRead = true;
            } catch (IOException e) {
                System.out.println("Text entering error. Please try again..");
            }
        }
        return s;
    }
    public static int readInt(){
        int i=0;
        try{
            i = Integer.parseInt(readString());
        }catch (NumberFormatException e){
            System.out.println("Text entering error. Please try again..");
            i = Integer.parseInt(readString());
        }
        return i;
    }
}
