import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private int port;
    private String serverName;
    private String[] bannedWords;
    private HashMap<String, ServerChattingLogic> clients = new HashMap<>();

    public Server() {
        File file = new File("/Users/v.d.o_/IntellijIdeaProjects/Server_Client/config.bin");
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            port = ois.readInt();
            serverName = ois.readUTF();
            bannedWords = (String[]) ois.readObject();

            System.out.println("-=-=-=-=-=-=-=-");
            System.out.println( port);
            System.out.println(serverName);
            for (String word : bannedWords) {
                System.out.println(word);
            }
            System.out.println("-=-=-=-=-=-=-=-");

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is working  ");

            while (true) {
                Socket socket = serverSocket.accept();
                ServerChattingLogic Connection = new ServerChattingLogic(socket);
                new Thread(Connection).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ServerChattingLogic implements Runnable {
        private Socket socket;
        private String clientName;
        private BufferedReader in;
        private PrintWriter out;

        public ServerChattingLogic(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Write Your Name ");
                clientName = in.readLine();

                if (clients.containsKey(clientName)) {
                    out.println("This name is already in use.");
                    socket.close();
                    return;
                }
                out.println("Welcome " + clientName);

                clients.put(clientName, this);
                sendHelp();
                sendListOfClients();
                MessageForAll(clientName + " joined to the chat");


                String message;
                while ((message = in.readLine()) != null) {

                    if (banWord(message)) {
                        out.println("Your message contains banned words and will not be sent.");
                    } else if (message.equals("/help")) {
                        sendHelp();
                    } else if (message.equals("/clients")) {
                        sendListOfClients();
                    } else if (message.equals("/ban")) {
                        sendBannedWords();
                    } else if (message.startsWith("@")) {
                        PrivateMessage(message, clientName);
                    } else if (message.startsWith("-")) {
                        ExclusionMessage(message, clientName);
                    } else {
                        MessageForAll("< " + clientName + " > : " + message);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (clientName != null) {
                clients.remove(clientName);
                MessageForAll(clientName + " left the chat.");
            }

        }

        private void sendMessage(String message) {
            out.println(message);
        }


        private void MessageForAll(String message) {
            for (ServerChattingLogic client : clients.values()) {
                client.sendMessage(message);
            }
        }


        private void PrivateMessage(String message, String senderName) {
            int spaceIndex = message.indexOf(' ');
            if (spaceIndex > 1) {
                String targetNames = message.substring(1, spaceIndex);
                String privateMessage = message.substring(spaceIndex + 1);

                String[] targetUsers = targetNames.split(",");

                for (String targetName : targetUsers) {
                    targetName = targetName.trim();
                    ServerChattingLogic targetClient = clients.get(targetName);
                    if (targetClient != null) {
                        targetClient.sendMessage("< private message from " + senderName + " > : " + privateMessage);
                    }
                }
            }
        }

        private void ExclusionMessage(String message, String senderName) {
            int spaceIndex = message.indexOf(' ');
            if (spaceIndex > 1) {
                String excludedNames = message.substring(1, spaceIndex);
                String broadcastMessage = message.substring(spaceIndex + 1);
                String[] excludedUsers = excludedNames.split(",");

                for (String clientName : clients.keySet()) {
                    boolean shouldExclude = false;

                    for (String excludedUser : excludedUsers) {
                        if (clientName.equals(excludedUser.trim())) {
                            shouldExclude = true;
                            break;
                        }
                    }
                    if (!shouldExclude) {
                        ServerChattingLogic CurrentClient = clients.get(clientName);
                        CurrentClient.sendMessage("<" + senderName + "> : " + broadcastMessage);
                    }
                }
            }
        }


        private boolean banWord(String message) {
            for (String word : bannedWords) {
                if (message.contains(word)) {
                    return true;
                }
            }
            return false;
        }


        private void sendHelp() {
            out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            out.println("'/clients' to see all client names");
            out.println("'/ban' to see list of banned phrases.");
            out.println("'@username'  Send a message to a specific person using their username.");
            out.println("'@username1,username2' Send a message to multiple specific people.");
            out.println("'-username' Send a message to every other connected client, with exception to some people (specified by the client). ");
            out.println("'/exit' to exit the chat.");
            out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        }

        private void sendListOfClients() {
            out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            out.println("Online Users:");

            for (String name : clients.keySet()) {
                out.println(name);
            }
            out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        }

        private void sendBannedWords() {
            out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            out.println("Banned Words:");
            for (String word : bannedWords) {
                out.println(word);
            }
            out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        }

    }

    public static void main(String[] args) {
        Server Myserver = new Server();
        Myserver.start();
    }
}
