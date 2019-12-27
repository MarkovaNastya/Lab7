import org.zeromq.ZContext;
import org.zeromq.*;

import java.util.Scanner;


public class Client {

    private final static String FRONTEND_ADRESS = "tcp://localhost:5559";
    private final static String GET = "GET";
    private final static String STOP = "/STOP";
    private final static String PUT = "PUT";
    private final static String SPACE = " ";

    public static void main(String[] args) {


        ZContext context = new ZContext();

        ZMQ.Socket frontend = context.createSocket(SocketType.REQ);
        frontend.connect(FRONTEND_ADRESS);

        Scanner in = new Scanner(System.in);

        while (!Thread.currentThread().isInterrupted()) {

            String command = in.nextLine();

            if (command.startsWith(STOP)) {
                break;
            }

            String[] commandSplit = command.split(SPACE);

            if (commandSplit[0].equals(GET) || commandSplit[0].equals(PUT)) {
                ZMsg msg = new ZMsg();
                for (int i = 0; i < commandSplit.length; i++) {
                    msg.add(commandSplit[i]);
                }
                msg.send(frontend);

                ZMsg response = ZMsg.recvMsg(frontend);
                System.out.println(response.pop().toString());

                response.destroy();
            }


        }

    }
}
