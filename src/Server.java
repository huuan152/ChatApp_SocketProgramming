import java.net.*;
import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Server implements Runnable {

    Socket socket;

    public static ArrayList<BufferedWriter> client = new ArrayList<>();

    public Server(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            client.add(writer);

            while(true){
                String data = reader.readLine().trim();
                System.out.println("Received " + data);

                for(int i = 0; i < client.size(); i++){
                    try{
                        client.get(i).write(data);
                        client.get(i).write("\r\n");
                        client.get(i).flush();
                    } catch(Exception e){ }
                }
            }
        } catch(Exception e){ }

    }

    public static void main(String[] args) throws Exception{
        ServerSocket s = new ServerSocket(2001);

        while(true) {
            Socket socket = s.accept();
            Server server = new Server(socket);
            Thread thread = new Thread(server);
            thread.start();
        }
    }
}
