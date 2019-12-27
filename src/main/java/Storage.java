import org.zeromq.ZContext;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;
import zmq.poll.Poller;

import java.util.Scanner;

public class Storage {

    private final static String data = "abcdefghklmnoprstufxasldhbasldhbasdaslhbasfadvldashbdashgdvasljdvjasdbasdal";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int left = in.nextInt();
        int right = in.nextInt();


        ZMQ.Context context = ZMQ.context(1);
        Socket responder = context.socket(SocketType.DEALER);
        responder.connect("tcp://localhost:5560");

        while (!Thread.currentThread().isInterrupted()) {

        }

    }
}
