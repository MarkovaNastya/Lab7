import org.zeromq.ZContext;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;
import zmq.poll.Poller;

import java.util.Scanner;


public class Client {
    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);
        Socket responder = context.socket(SocketType.REQ);
        responder.connect("tcp://localhost:5559");

        Scanner in = new Scanner(System.in);

        while (!Thread.currentThread().isInterrupted()) {

            String command = in.nextLine();

            if (command.startsWith("/STOP")) {
                break;
            }

            String[] commandSplit = command.split(" ");

            if (commandSplit[0].equals("GET")) {
                ZMsg msg = new ZMsg();
                msg.add()
            }


            if (commandSplit[0].equals("SET")) {

            }

        }

    }
}
