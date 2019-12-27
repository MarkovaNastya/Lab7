import org.zeromq.*;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
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


        putServerMessageTogetherAndSend("NEW");

        while (true) {
            if (System.currentTimeMillis() - start > 5000) {
                start = System.currentTimeMillis();
                putServerMessageTogetherAndSend("I STILL ALIVE");
            }

            ZMsg msgReceive = ZMsg.recvMsg(responder);

            ZFrame adress = msgReceive.pop();

            //GET
            if (msgReceive.size() == 1) {
                int indexInteger = Integer.parseInt(String.valueOf(msgReceive.pop()));

                ArrayList<ZFrame> frames = new ArrayList<>();
                frames.add(adress);
                frames.add(data.charAt(indexInteger));



            }




        }

    }

    private static void putServerMessageTogetherAndSend(String frame) {
        ZMsg msg = new ZMsg();
        msg.add(frame);
        msg.add(left + "//" + right);
        msg.send(responder);
    }

    private static void putCommandMessageTogetherAndSend(ArrayList<ZFrame> frames) {
        ZMsg msg = new ZMsg();

        for (ZFrame frame : frames) {
            msg.add(frame);
        }

        msg.send(responder);
    }

}
