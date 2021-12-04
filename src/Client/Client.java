package Client;

import Security.SecurityLayer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class Client {

    Socket socket;
    SecurityLayer securityLayer;
    BufferedReader console;

    public static Client connect(String hostName, int portNumber) throws IOException, GeneralSecurityException {
        Socket socket = null;
        try {
            socket = new Socket(hostName, portNumber);
            Client client = new Client(socket);
            client.establishSecurity(socket.getInputStream(), socket.getOutputStream());
            return client;
        } catch (IOException | GeneralSecurityException e) {
            if(socket != null){
                if(!socket.isClosed()){
                    socket.close();
                }
            }
            throw e;
        }
    }

    private Client(Socket socket){
        this.socket = socket;
        securityLayer = new SecurityLayer();
        console = new BufferedReader( new InputStreamReader(System.in));
    }

    private void establishSecurity(InputStream in, OutputStream out) throws IOException, GeneralSecurityException {
        securityLayer.init(in, out);
        securityLayer.clientHandshake();
    }

    public void downService() throws IOException {
        if(!socket.isClosed()){
            socket.close();
        }
    }

    public void getMessage() throws IOException, ClassNotFoundException, GeneralSecurityException {
        byte[] data = securityLayer.receive();
        String[] message = deserializeMessage(data);
        for (String line : message){
            System.out.println(line);
        }
    }

    public String readConsole() throws IOException {
        return console.readLine();
    }

    private String[] deserializeMessage(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        return (String[]) new ObjectInputStream(in).readObject();
    }

    public void sendMessage(String message) throws IOException, GeneralSecurityException {
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
                    System.out.println();
                }
            }catch (GeneralSecurityException e){
                e.printStackTrace();
                client.downService();
            }catch (IOException | ClassNotFoundException e){
                client.downService();
            }
        }catch(GeneralSecurityException e){
            e.printStackTrace();
        }catch(IOException | NullPointerException ignored){
        }
    }

}
