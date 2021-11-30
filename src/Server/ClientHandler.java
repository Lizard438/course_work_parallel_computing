package Server;

import Index.Index;
import Security.SecurityLayer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread{

    Socket socket;
    Index index;
    SecurityLayer securityLayer;

    ClientHandler(Socket socket, Index index)  {
        this.socket = socket;
        this.index = index;
        securityLayer = new SecurityLayer();
        //start();
    }


    @Override
    public void run() {

        try(
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        ){
            securityLayer.init(in, out);
            //serverHandshake
            String request;
            ArrayList<String> result;

            while (true){
                //out.writeObject("Search: ");
                try{
                    request = getRequest();
                    result = index.find(request);
                    sendResult(result);
                }catch(NullPointerException e){
                    result = new ArrayList<>();
                    result.add("Request error occurred.");
                    sendResult(result);
                }
            }
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

    public String getRequest() throws IOException {
        byte[] data = securityLayer.receive();
        return new String(data);
    }

    public void sendResult(ArrayList<String> result) throws IOException{
        byte[] data = serializeResult(result);
        securityLayer.send(data);
    }

    private byte[] serializeResult(ArrayList<String> result) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteStream);
        out.writeObject(result);
        out.flush();
        return byteStream.toByteArray();
    }

}
