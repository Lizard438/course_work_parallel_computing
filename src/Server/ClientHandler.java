package Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread{

    Socket socket;
    BufferedReader in;
    BufferedWriter out;

    ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

}
