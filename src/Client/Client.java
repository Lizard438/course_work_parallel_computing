package Client;

import Security.SecurityLayer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Client {

    Socket socket;
    SecurityLayer securityLayer;
    BufferedReader console;

    public static Client connect(String hostName, int portNumber) throws  IOException{
        Socket socket = null;
        try {
            socket = new Socket(hostName, portNumber);
            Client client = new Client(socket);
            client.establishSecurity(socket.getInputStream(), socket.getOutputStream());
            return client;
        } catch (IOException e) {
            if(socket != null){
                if(!socket.isClosed()){
                    socket.close();
                }
            }
            throw new IOException("Connection failed.", e);
        }
    }

    Client(Socket socket){
        this.socket = socket;
        securityLayer = new SecurityLayer();
        console = new BufferedReader( new InputStreamReader(System.in));
    }

    public void establishSecurity(InputStream in, OutputStream out){
        securityLayer.init(in, out);
        //client handshake
    }

    void downService() throws IOException {
        if(!socket.isClosed()){
            socket.close();
        }
    }

    void getMessage() throws IOException, ClassNotFoundException {
        byte[] data = securityLayer.receive();
        String[] message = deserializeMessage(data);
        for (String line : message){
            System.out.println(line);
        }
    }

    String readConsole() throws IOException {
        return console.readLine();
    }

    public String[] deserializeMessage(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        return (String[]) new ObjectInputStream(in).readObject();
    }

    public void sendMessage(String message) throws IOException {
        securityLayer.send(message.getBytes(StandardCharsets.UTF_8));
    }



    public static void main(String[] args){

        if(args.length != 2){
            System.err.println("Usage: java Client <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        try{
            Client client = connect(hostName, portNumber);
            try{
                while (true){
                    client.getMessage();

                    String request = client.readConsole();

                    client.sendMessage(request);

                    client.getMessage();
                    System.out.println("Press Enter to continue.");
                    client.readConsole();

                }
            }catch (IOException | ClassNotFoundException e){
                client.downService();
            }

        }catch(IOException | NullPointerException ignored){}

    }

}
