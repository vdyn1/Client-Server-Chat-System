import java.io.*;

public class Config {
    public static void main(String[] args) {

        int port = 111;
        String serverName = "MyServer";
        String[] bannedWords = {
                "badword1", "badword2",
                "badword3", "badword4",
                "badword5", "badword6",
                "bober"
        };

        try {
            FileOutputStream fos = new FileOutputStream("/Users/v.d.o_/IntellijIdeaProjects/Server_Client/config.bin");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(port);
            oos.writeUTF(serverName);
            oos.writeObject(bannedWords);
            oos.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
