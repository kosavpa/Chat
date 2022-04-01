package Client;

import Server.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client{

    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected SocketThread getSocketThread(){
        return new BotSockedThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    @Override
    protected String getUserName(){
        return "date_bot_" + (int) (Math.random() * 100);
    }

    public class BotSockedThread extends SocketThread{

        @Override
        protected void clientMainLoop(){
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            try {
                super.clientMainLoop();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
            String[] messageParam = message.split(": ");
            if (messageParam.length != 2) return;
            String format = null;
            switch (messageParam[1]) {
                case "дата":
                    format = "d.MM.YYYY"; break;
                case "день":
                    format = "d"; break;
                case "месяц":
                    format = "MMMM"; break;
                case "год":
                    format = "YYYY"; break;
                case "время":
                    format = "H:mm:ss"; break;
                case "час":
                    format = "H"; break;
                case "минуты":
                    format = "m"; break;
                case "секунды":
                    format = "s"; break;
            }

            if (format != null) {
                String answer = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
                BotClient.this.sendTextMessage("Информация для " + messageParam[0] + ": " + answer);
            }
        }
    }
}
