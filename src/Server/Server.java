package Server;


import Index.Index;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

    public static void main(String[] args){

        if(args.length != 1){
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        LinkedList<ClientHandler> clientsList = new LinkedList<>();
        Index index;
        try{
            index = Index.loadIndex("./index.txt") ;
        }catch (IOException | ClassNotFoundException e){
            return;
        }

        try{
            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

                System.out.println("Server is running.");

                while (true) {
                    Socket socket = serverSocket.accept();
                    try {
                        clientsList.add(new ClientHandler(socket, index));
                    } catch (IOException e) {
                        socket.close();
                    }
                }
            }


        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
