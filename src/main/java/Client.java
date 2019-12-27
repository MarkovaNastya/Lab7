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

            if (commandSplit[0].equals("GET") || commandSplit[0].equals("SET")) {
                ZMsg msg = new ZMsg();
                for (int i = 0; i < commandSplit.length; i++) {
                    msg.add(commandSplit[i]);
                }
                msg.send(responder);
            }

            ZMsg response = ZMsg.recvMsg(responder);
            System.out.println(response);

        }

    }
}
