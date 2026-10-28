package chatsystem;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class newserver {
    public static void main(String[] args) {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(5000));
            // server.configureBlocking(false);

            Worker[] workers = new Worker[10];
            for(int i = 0 ; i < 10 ; i++){
                Worker worker = new Worker(i);
                workers[i] = worker;
            }

            for(int i = 0 ; i < 10 ; i++){
                Scheduler schedule = new Scheduler(workers, i);
            }
            int count = 10;
            while(true){
                SocketChannel client = server.accept();
                if(client != null){
                    client.configureBlocking(false);

                    int id = (count - 10) % 10;
                    count++;
                    workers[id].register(client);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
