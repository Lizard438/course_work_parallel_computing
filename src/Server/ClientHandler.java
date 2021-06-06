package Server;

import Index.Index;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread{

    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    Index index;

    ClientHandler(Socket socket, Index index) throws IOException {
        this.socket = socket;
        this.index = index;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String query;
        ArrayList<String> result;
        try{
            send("Type 'Q' to quite.");

            while (true){

                send("\nSearch: ");
                query = in.readLine();
                if(query.equals("Q")||query.equals("q")){
                    break;
                }
                result = index.find(query);

                for(String line : result){
                    send(line);
                }

            }
        }catch(IOException e){

        }
    }

    void send(String msg){
        try{
            out.write(msg + "\n");
            out.flush();
        }catch(IOException ignored){}
    }

}
