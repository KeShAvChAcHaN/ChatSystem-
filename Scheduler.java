package chatsystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;

public class Scheduler implements Runnable{

    private Worker[] worker_arr;
    private int scheduler_id;
    Scheduler(Worker[] worker , int number){
        this.worker_arr = worker;
        this.scheduler_id = number;
        Thread scheduler_thread = new Thread(this);
        scheduler_thread.start();
    }

    @Override 
    public void run(){
        Worker worker = worker_arr[scheduler_id];
        while(true){
            try{
            Message message = worker.message.take();
            if(message != null){
                if(message.id == scheduler_id){
                    for(int i = 0 ; i < 10 ; i++){
                        if(i != scheduler_id){
                            worker_arr[i].message.offer(message);
                        }
                    }
                }
                HashSet<SelectionKey> snap = new HashSet<>(worker.selector.keys());
                for(SelectionKey key : snap){
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer body = message.body.duplicate();
                    try {
                        while(body.hasRemaining()){
                            int written = client.write(body);
                            if(written == -1){
                                client.close();
                                break;
                            }
                            if(written == 0){
                                break;
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            }
            catch(InterruptedException e){
                Thread.currentThread().interrupt();  
            }
        }
    }
}
