package Server;

import Index.Index;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread{

    Socket socket;
    BufferedReader in;
    ObjectOutputStream out;

    Index index;

    ClientHandler(Socket socket, Index index)  {
        this.socket = socket;
        this.index = index;

        //start();
        try{

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
            start();

        }catch (IOException e){
            downService();
        }

    }

    void downService(){
        try{
            if(!socket.isClosed()){
                socket.close();
                in.close();
                out.close();
            }
        }catch (IOException ignored){}
    }

    @Override
    public void run() {

        String request;
        ArrayList<String> result;

        try{

            while (true){

                out.writeObject("Search: ");

                request = in.readLine();

                if(request.equals("Q") || request.equals("q")){
                    break;
                }
                result = index.find(request);

                out.writeObject(result);

            }
        }catch(IOException e){
            this.downService();
        }
    }

}
