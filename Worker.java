package chatsystem;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Worker implements Runnable {
    public BlockingQueue<Message> message = new LinkedBlockingQueue<>(); 
    public Selector selector;
    public int worker_id;
    Worker(int id) throws IOException{
        this.worker_id = id;
        this.selector = Selector.open();
        Thread t = new Thread(this);
        t.start();
    }

    public void register(SocketChannel client) throws ClosedChannelException{
        client.register(selector , SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override 
    public void run(){
        while (true) {
            try {
                selector.select();

                for(SelectionKey key : selector.selectedKeys()){
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buff = ByteBuffer.allocate(4); // read the 4 byte to know the body bytes
                    int num_byte = client.read(buff);
                    if (num_byte > 0){
                        while(buff.hasRemaining()){
                            int n = client.read(buff);
                            System.out.println("dost");
                            if(n == -1){
                                client.close();
                                break;
                            }
                        }
                        if(!buff.hasRemaining()){
                            buff.flip();
                            int length = buff.getInt();
                    
                            ByteBuffer buffer = ByteBuffer.allocate(length); // read remaining bytes to complete read
                            int x = client.read(buffer);
                            while (buffer.hasRemaining()) {
                                x = client.read(buffer);
                                if(x == -1){
                                    client.close();
                                    break;
                                }
                            }
                               
                            if(!buffer.hasRemaining()){
                                buffer.flip();
                                ByteBuffer complete = ByteBuffer.allocate(4+length);
                                complete.putInt(length);
                                complete.put(buffer);
                                complete.flip();

                                Message body = new Message(complete , worker_id);

                                message.offer(body);
                            }
                            else{
                                client.close();
                            }
                        }
                        else{
                            client.close();
                        }
                    }
                    else{
                        client.close();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
