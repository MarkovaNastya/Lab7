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

        StringBuilder data = new StringBuilder(mainData.substring(left, right));
        System.out.println(data);

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

            String adress = msgReceive.popString();

            //GET
            if (msgReceive.size() == 1) {
                int indexInteger = Integer.parseInt(String.valueOf(msgReceive.pop()));

                ArrayList<String> frames = new ArrayList<>();
                frames.add("GET");
                frames.add(adress);
                frames.add(String.valueOf(data.charAt(indexInteger - left)));

                putCommandMessageTogetherAndSend(frames);

            } else if (msgReceive.size() == 2) { //SET
                int indexInteger = Integer.parseInt(String.valueOf(msgReceive.pop()));
                String setElem = msgReceive.popString();

                data.setCharAt(indexInteger - left, setElem.charAt(0));

                ArrayList<String> frames = new ArrayList<>();
                frames.add("SET");
                frames.add(adress);
                frames.add("Character changed");

                putCommandMessageTogetherAndSend(frames);
            }
        }

    }

    private static void putServerMessageTogetherAndSend(String frame) {
        ZMsg msg = new ZMsg();
        msg.add(frame);
        msg.add(left + "//" + right);
        msg.send(responder);
    }

    private static void putCommandMessageTogetherAndSend(ArrayList<String> frames) {
        ZMsg msg = new ZMsg();

        for (String frame : frames) {
            msg.add(frame);
        }

        msg.send(responder);
    }

}
