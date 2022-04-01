package Client;

public class ClientGuiController extends Client {
    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);

    @Override
    protected SocketThread getSocketThread(){
        return new GuiSocketThread();
    }

    @Override
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.run();
    }

    @Override
    public String getServerAddress(){
        return view.getServerAddress();
    }

    @Override
    public int getServerPort(){
        return view.getServerPort();
    }

    @Override
    public String getUserName(){
        return view.getUserName();
    }

    public ClientGuiModel getModel() {
        return model;
    }

    public class GuiSocketThread extends SocketThread{

        @Override
        protected void processIncomingMessage(String message){
            model.setNewMessage(message);
            view.refreshMessages();
        }

        @Override
        protected void informAboutAddingNewUser(String userName){
            model.addUser(userName);
            view.refreshUsers();
            view.refreshMessages();
        }

        @Override
        protected void informAboutDeletingNewUser(String userName){
            model.deleteUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }

    public static void main(String[] args) {
        ClientGuiController client = new ClientGuiController();
        client.run();
    }
}
