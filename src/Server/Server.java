package Server;


import Index.Index;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args){

        if(args.length != 1){
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        Index index;
        try{
            System.out.println("Loading...");
            index = Index.loadIndex("./index.txt") ;
        }catch (IOException | ClassNotFoundException e){
            return;
        }

        try{
            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

                System.out.println("Server is running.");

                while (true) {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(socket, index).start();
                }
            }


        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
