package Client;

import Server.*;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        new Client().run();
    }



    protected Connection connection;
    private volatile boolean clientConnected = false;

    public void run(){
        synchronized (this) {

            SocketThread clientSocketThread = getSocketThread();    //создается и стартуется новая вспомогательная "демон" - нить
            clientSocketThread.setDaemon(true);
            clientSocketThread.start();

            try {
                wait(); //"лочится" нить пока в вспомогательном классе не установиться соединение через сокет, не поменяется статус clientConnected и нить не "разлочится"
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage(e.getMessage());
                return;
            }

            if (clientConnected) {
                ConsoleHelper.writeMessage("Соединение установленно. Для выхода наберите команду 'exit'.");
            } else {
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            }

            while (clientConnected){                                            //обработка сообщений которые пользователь пишет в консоль
                String consoleLine = ConsoleHelper.readString();                //"читает" из консоли сообщения
                if(consoleLine.equals("exit")) break;
                if(shouldSendTextFromConsole()) sendTextMessage(consoleLine);   //отсылка сообщений из консоли на сервер
            }
        }
    }

    protected String getServerAddress(){
        //запрашивается адрес сокета
        ConsoleHelper.writeMessage("Введите адрес сервера.");
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        //запрашивается порт сокета
        ConsoleHelper.writeMessage("Введите порт сервера.");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        //запрашивается имя у пользователя
        ConsoleHelper.writeMessage("Введите имя пользователя.");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        //консольный флаг
        return true;
    }

    protected SocketThread getSocketThread(){
        //создание нового вспомогательноко класса
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        //отправляется через сокет новое сообщение на сервер типа - текст
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread{ //нить вспомогательного класса сокета клиента

        public void run(){

            try {

                Socket clientSocket = new Socket(Client.this.getServerAddress(), Client.this.getServerPort());  //создание нового сокет соединения
                Client.this.connection = new Connection(clientSocket);                                          //присваивание "соединению" клинтского класса сокет соединения
                clientHandshake();                                                                              //клиент и сервер "здороваются"
                clientMainLoop();                                                                               //обработка сообщений с сервера

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message){
        //пишет в консоль полученное сообщение
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            //сообщает о появление вв чате нового пользователя
            ConsoleHelper.writeMessage(userName + " теперь в чате!");
        }

        protected void informAboutDeletingNewUser(String userName){
            //сообщает что пользователь вышел из чата
            ConsoleHelper.writeMessage(userName + " вышел из чата!");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            //меняет статус о том что клинт в "сети" и разблокирует ожидабщую нить
            synchronized (Client.this){
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            //клиент и сервер "жмут руки"(знакомятся)
            while (true){
                Message message = Client.this.connection.receive();

                if(message.getType() == MessageType.NAME_REQUEST){
                    Client.this.connection.send(new Message(MessageType.USER_NAME, Client.this.getUserName()));
                } else if(message.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            //обработка сообщений с сервера
            while (true){
                Message message = Client.this.connection.receive();

                if(message.getType() == MessageType.TEXT){
                    processIncomingMessage(message.getData());
                } else if(message.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                } else if(message.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }
    }
}
