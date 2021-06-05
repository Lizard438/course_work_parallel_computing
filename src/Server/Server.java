package Server;


import Index.Index;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

    public static void main(String[] args){
        final int port = 5000;
        LinkedList<ClientHandler> clientsList = new LinkedList<>();
        try{
            Index index = Index.loadIndex("./index.txt") ;
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        try{
            try (ServerSocket serverSocket = new ServerSocket(port)) {

                System.out.println("Server is running.");

                while (true) {
                    Socket socket = serverSocket.accept();
                    try {
                        clientsList.add(new ClientHandler(socket));
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
