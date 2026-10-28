package chatsystem;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class client {
    public static void main(String[] args) {
        try{
            SocketChannel client = SocketChannel.open();
            client.connect(new InetSocketAddress("127.0.0.1", 5000));
            Thread t = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            String name = sc.nextLine();
            while(true){
                String new_message = sc.nextLine();
                String complete = name + ": " + new_message;
                byte[] data = complete.getBytes(StandardCharsets.UTF_8);
                int length = data.length;
                ByteBuffer buff = ByteBuffer.allocate(4 + length);
                buff.putInt(length);
                buff.put(data);
                buff.flip();
                while (buff.hasRemaining()) {
                    try {
                        int read = client.write(buff);
                        if(read == -1){
                            client.close();
                            sc.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            });
            t.start();

            while(true){
                ByteBuffer length_buff = ByteBuffer.allocate(4);
                while(length_buff.hasRemaining()){
                    int n = client.read(length_buff);
                    if(n == -1){
                        break;
                    }
                }
                if(!length_buff.hasRemaining()){
                    length_buff.flip();
                    int length = length_buff.getInt();

                    ByteBuffer message_buffer = ByteBuffer.allocate(length);
                    while (message_buffer.hasRemaining()) {
                        int n = client.read(message_buffer);
                        if(n == -1){
                            client.close();
                            return ;
                        }
                    }
                    if(!message_buffer.hasRemaining()){
                        message_buffer.flip();
                        String message = StandardCharsets.UTF_8.decode(message_buffer).toString();
                        System.out.println(message);
                    }
                }
            }
        }
        catch(IOException e){
            e.getStackTrace();
        }
    }
}
