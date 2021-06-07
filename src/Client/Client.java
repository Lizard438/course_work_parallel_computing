package Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    String hostName;
    int portNumber;

    Socket socket;
    ObjectInputStream in;
    BufferedReader console;
    BufferedWriter out;

    Client(int portNumber, String hostName){

        this.hostName = hostName;
        this.portNumber = portNumber;

        try {
            socket = new Socket(hostName, portNumber);

            in = new ObjectInputStream(socket.getInputStream());
            console = new BufferedReader( new InputStreamReader(System.in));

            out = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));


        } catch (IOException e){
            if(socket == null){
                System.out.println("Can not connect.");
            }
            downService();
        }
    }

    void downService(){
        try{
            if(socket != null){
                if(in != null){
                    in.close();
                }
                if(out != null){
                    out.close();
                }
                if(!socket.isClosed()){
                    socket.close();
                }
            }
        }catch(IOException ignored){}
    }

    void getMessage() throws IOException{

        try{
            String msg = (String) in.readObject();
            System.out.println(msg);

        }catch (ClassNotFoundException e){
            System.out.println("Bad result type.");
        }
    }

    void getResult() throws IOException{

        try{

            ArrayList<String> result = (ArrayList<String>) in.readObject();

            for(String line : result){
                System.out.println("\n"+line);
            }

        }catch (ClassNotFoundException e){
            System.out.println("Bad result type.");
        }
    }


    public static void main(String[] args){

        if(args.length != 2){
            System.err.println("Usage: java Client <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        Client client = new Client(portNumber, hostName);

        try{
            while (true){
                client.getMessage();

                String request = client.console.readLine();

                client.out.write(request+"\n");
                client.out.flush();

                if(request.equals("Q")||request.equals("q")){
                    break;
                }

                client.getResult();
                System.out.println("Press Enter to continue.");
                client.console.readLine();

            }
        }catch(IOException | NullPointerException ignored){}
        finally {
            client.downService();
        }

    }

}
