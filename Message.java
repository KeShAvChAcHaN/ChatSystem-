package chatsystem;

import java.nio.ByteBuffer;

public class Message {
    ByteBuffer body;
    int id;

    Message(ByteBuffer worker , int id){
        this.body = worker;
        this.id = id;
    }
}
