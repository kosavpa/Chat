package Server;

import java.io.Serializable;

public class Message implements Serializable {
    //класс сообщений
    private final MessageType type;     //тип сообщения
    private final String data;          //само сообщение(его содержание)

    public Message(MessageType type){
        this.type = type;
        this.data = null;
    }

    public Message(MessageType type, String data){
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
