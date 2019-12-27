import org.zeromq.*;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.Scanner;

public class Storage {

    private final static String mainData = "abcdefghklmnoprstufxasldhbasldhbasdaslhbasfadvldashbdashgdvasljdvjasdbasdal";

    private static int left;
    private static int right;
    private static ZMQ.Socket responder;

    private final static String BACKEND_ADRESS = "tcp://localhost:5560";
    private final static String NEW = "NEW";
    private final static String GET = "GET";
    private final static String SET = "SET";
    private final static String CHANGED = "Character changed";
    private final static String STILL_ALIVE = "STILL ALIVE";
    private final static String DOUBLE_TRAIT = "//";
    private final static int TIMEOUT_MS = 5000;



    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        left = in.nextInt();
        right = in.nextInt();

        if (left > right || left > mainData.length() - 1 || right > mainData.length() - 1) {
            System.out.println("wrong borders");
            return;
        }

        StringBuilder data = new StringBuilder(mainData.substring(left, right));
        System.out.println(data);

        ZContext context = new ZContext();

        responder = context.createSocket(SocketType.DEALER);
        responder.connect(BACKEND_ADRESS);

        long start = System.currentTimeMillis();

        ZMQ.Poller poller = context.createPoller(1);
        poller.register(responder, ZMQ.Poller.POLLIN);

        putServerMessageTogetherAndSend(NEW);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll(1);

            if (System.currentTimeMillis() - start > TIMEOUT_MS) {
                start = System.currentTimeMillis();
                putServerMessageTogetherAndSend(STILL_ALIVE);
            }

            if (poller.pollin(0)) {
                ZMsg msgReceive = ZMsg.recvMsg(responder);

                //GET
                if (msgReceive.size() == 2) {
                    ZMsg msg = new ZMsg();
                    int index = Integer.parseInt(msgReceive.pollLast().toString());

                    msg.add(GET);
                    ZFrame adress = msgReceive.pop();
                    msg.add(adress);
                    msg.add("" + data.charAt(index - left));
                    System.out.println(msg);
                    msg.send(responder);

                } else if (msgReceive.size() == 3) { //SET
                    ZMsg msg = new ZMsg();
                    String value = msgReceive.pollLast().toString();
                    int index = Integer.parseInt(msgReceive.pollLast().toString());
                    msg.add(SET);
                    ZFrame adress = msgReceive.pop();
                    msg.add(adress);
                    data.setCharAt(index - left, value.charAt(0));
                    msg.add(CHANGED);
                    System.out.println(msg);
                    msg.send(responder);

                }
            }


        }

    }

    private static void putServerMessageTogetherAndSend(String frame) {
        ZMsg msg = new ZMsg();
        msg.add(frame);
        msg.add(left + DOUBLE_TRAIT + right);
        msg.send(responder);
    }



}
