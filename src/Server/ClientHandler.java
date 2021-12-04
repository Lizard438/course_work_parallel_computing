package Server;

import Index.Index;
import Security.SecurityLayer;

import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;

public class ClientHandler extends Thread{

    Socket socket;
    Index index;
    SecurityLayer securityLayer;

    ClientHandler(Socket socket, Index index)  {
        this.socket = socket;
        this.index = index;
        securityLayer = new SecurityLayer();
    }

    private void establishSecurity(InputStream in, OutputStream out) throws IOException, GeneralSecurityException {
        securityLayer.init(in, out);
        securityLayer.serverHandshake();
    }

    @Override
    public void run() {
        try(
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()
        ){
            establishSecurity(in, out);

            while (true){
                sendMessage("Search: ");
                try{
                    String request = getRequest();
                    String[] result = index.find(request);
                    sendMessage(result);
                }catch(NullPointerException e){
                    sendMessage("Request error occurred.");
                }
            }
        }catch (GeneralSecurityException e){
            e.printStackTrace();
        }catch (IOException ignored){
        }
        finally {
            try{
                if(!socket.isClosed()){
                    socket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public String getRequest() throws IOException, GeneralSecurityException {
        byte[] data = securityLayer.receive();
        return new String(data);
    }

    public void sendMessage(String... message) throws IOException, GeneralSecurityException {
        byte[] data = serializeMessage(message);
        securityLayer.send(data);
    }

    private byte[] serializeMessage(String[] message) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteStream);
        out.writeObject(message);
        out.flush();
        return byteStream.toByteArray();
    }

}
