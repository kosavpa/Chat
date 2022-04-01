package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт для соединения.");
        try(
                ServerSocket server = new ServerSocket(ConsoleHelper.readInt()); //создание серверного сокета
                ){
            System.out.println("Сервер запущен!");
            while (true) {
                Socket clientSocket = server.accept();                           //воссоздание сокета клиента на сервере
                new Handler(clientSocket).start();                               //стартуется новоя нить вспомогательного класса
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();   //"мапа" всех участников чата

    public static void sendBroadcastMessage(Message message){
        //отослать сообщение всем участникам
        for(String client : connectionMap.keySet()){
            try {
                connectionMap.get(client).send(message);
            } catch (IOException e) {
                System.out.println("Ошибка! К сожалению сообщение не было отправлено.");
            }
        }
    }

    private static class Handler extends Thread{
        //вспомогательный класс для взаимодействия серверов с "соединениями" клиентов пользователей
        private Socket socket;

        public Handler(Socket socket){
            this.socket = socket;
        }

        public void run(){

            ConsoleHelper.writeMessage("Было установлено соединение с удаленным адресом" + socket.getRemoteSocketAddress());
            String clientName = null;

            try {
                Connection serverToClientConnection = new Connection(socket);                   //создание "соединения" клиента с пользователем
                clientName = serverHandshake(serverToClientConnection);                         //клинт и сервер "знаомятся"
                Server.sendBroadcastMessage(new Message(MessageType.USER_ADDED, clientName));   //отсылает всем клиентам о присоединении нового
                notifyUsers(serverToClientConnection, clientName);                              //отсылает информацию новому клиенту о всех участниках чата
                serverMainLoop(serverToClientConnection, clientName);                           //"слушает" сокет клиента и отсылает сообщение всем участникам чата
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage(e.getMessage());
            }

            if(clientName != null){
                connectionMap.remove(clientName);
                Server.sendBroadcastMessage(new Message(MessageType.USER_REMOVED, clientName));
            }
            ConsoleHelper.writeMessage("Соединение с удаленным адресом " + socket.getRemoteSocketAddress() + "было разорвано!");
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            //сервер и клинт "жмут руки"
            //сервер запрашивает его уникальное имя клинта
            //после чего добавляет в "мапу" имя клиента и "соединение"
            String clientName;

            while (true){
                connection.send(new Message(MessageType.NAME_REQUEST, "Как вас завут?"));
                Message clientMessage = connection.receive();

                if(
                        clientMessage.getType() == MessageType.USER_NAME
                        && !clientMessage.getData().isEmpty()
                        && !connectionMap.containsKey(clientMessage.getData())
                ){
                    clientName = clientMessage.getData();
                    connectionMap.put(clientName, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED, "Ваше имя успешно принято."));
                    break;
                }
            }
            return clientName;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            //сообщает новому клиенту о других участниках
            for(String client : connectionMap.keySet()){
                if(!client.equals(userName)){
                    connection.send(new Message(MessageType.USER_ADDED, client));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            //отвечает за обработку сообщений типа - текст
            //если сообщения являются текстом то отсылает его всем пользователям
            while (true) {
                Message clientMessage = connection.receive();

                if (clientMessage.getType() == MessageType.TEXT) {
                    StringBuffer stringBuffer = new StringBuffer();
                    String bufferMessage = stringBuffer.append(userName).append(": ").append(clientMessage.getData()).toString();
                    Server.sendBroadcastMessage(new Message(MessageType.TEXT, bufferMessage));
                } else {
                    ConsoleHelper.writeMessage("Ошибка! Принятое сообщение не является типом - текст!");
                }
            }
        }
    }
}
