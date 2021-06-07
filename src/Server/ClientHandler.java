package Server;

import Index.Index;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread{

    Socket socket;
    Index index;

    ClientHandler(Socket socket, Index index)  {
        this.socket = socket;
        this.index = index;
        start();
    }


    @Override
    public void run() {

        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ){
            String request;
            ArrayList<String> result;

            while (true){

                out.writeObject("Search: ");

                request = in.readLine();

                if(request.equals("Q") || request.equals("q")){
                    break;
                }
                result = index.find(request);

                out.writeObject(result);

            }


        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
