import org.zeromq.*;
import org.zeromq.ZMQ.Socket;

import java.util.Scanner;

public class Storage {

    private final static String mainData = "abcdefghklmnoprstufxasldhbasldhbasdaslhbasfadvldashbdashgdvasljdvjasdbasdal";
    private static int left;
    private static int right;
    private static Socket responder;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        left = in.nextInt();
        right = in.nextInt();

        if (left > right || left > mainData.length() - 1 || right > mainData.length() - 1) {
            System.out.println("wrond borders");
        }

        String data = mainData.substring(left, right);

        ZMQ.Context context = ZMQ.context(1);
        responder = context.socket(SocketType.DEALER);
        responder.connect("tcp://localhost:5560");
        long start = System.currentTimeMillis();


        putMessageTogetherAndSend("NEW");

        while (true) {

            if (System.currentTimeMillis() - start > 5000) {
                start = System.currentTimeMillis();
                putMessageTogetherAndSend("I STILL ALIVE");
            }

            ZMsg msgReceive = ZMsg.recvMsg(responder);

            System.out.println("end sicle");




        }

    }

    private static void putMessageTogetherAndSend(String frame) {
        ZMsg msg = new ZMsg();
        msg.add(frame);
        msg.add(left + "//" + right);
        msg.send(responder);
    }
}
