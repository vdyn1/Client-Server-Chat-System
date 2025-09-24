import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;

    public Client(String hostname, int port) throws IOException {
        this.socket = new Socket(hostname, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        CreateWindow();
        new Read().start();
    }

    private void CreateWindow() {
        frame = new JFrame("Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(new Color(230, 230, 230));
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JScrollPane messageScrollPane = new JScrollPane(messageArea);


        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(messageScrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(inputField, BorderLayout.SOUTH);
        frame.setVisible(true);
        inputField.requestFocusInWindow();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            if (message.equals("/exit")) {
                try {
                    socket.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                out.println(message);
                inputField.setText("");
            }
        }
    }

    private class Read extends Thread {
        private BufferedReader in;

        public Read() throws IOException {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String resFromServ;
                while ((resFromServ = in.readLine()) != null) {
                    addMessage(resFromServ);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addMessage(String message) {
        messageArea.append(message + "\n");
    }


    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 111;
        try {
            new Client(hostname, port);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
