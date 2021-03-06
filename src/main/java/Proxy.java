import javafx.util.Pair;
import org.zeromq.ZContext;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Proxy {

    private static HashMap<Pair<Integer, Integer>, Pair<ZFrame, Long>> storages = new HashMap<>();
    private static Socket frontend;
    private static Socket backend;

    private final static String BACKEND_ADRESS = "tcp://localhost:5560";
    private final static String FRONTEND_ADRESS = "tcp://localhost:5559";
    private final static String NEW = "NEW";
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String NOTIFY = "NOTIFY";
    private final static String NOT_FOUND = "Not found";
    private final static String DOUBLE_TRAIT = "//";
    private final static int TIMEOUT_MS = 5000;
    private final static int DOUBLE_TIMEOUT_MS = TIMEOUT_MS * 2;


    public static void main(String[] args) {

        ZContext context = new ZContext();

        frontend = context.createSocket(SocketType.ROUTER);
        frontend.bind(FRONTEND_ADRESS);

        backend = context.createSocket(SocketType.ROUTER);
        backend.bind(BACKEND_ADRESS);

        ZMQ.Poller items = context.createPoller(2);
        items.register(frontend, ZMQ.Poller.POLLIN);
        items.register(backend, ZMQ.Poller.POLLIN);

        boolean more = false;
        ZMsg message = new ZMsg();

        while (!Thread.currentThread().isInterrupted()) {
            items.poll();

            //frontend
            if (items.pollin(0)) {
                while (true) {
                    message = ZMsg.recvMsg(frontend);
                    more = frontend.hasReceiveMore();

                    ZFrame adress = message.pop();
                    ZFrame nullFrame = message.pop();

                    String command = message.popString();

                    if (command.equals(GET)) {
                        frontendMessageHandler(adress, message, GET);
                    } else if (command.equals(PUT)) {
                        frontendMessageHandler(adress, message, PUT);
                    }

                    if (!more) {
                        break;
                    }
                }

            }

            //backend
            if (items.pollin(1)) {
                while (true) {
                    message = ZMsg.recvMsg(backend);
                    more = backend.hasReceiveMore();

                    ZFrame adress = message.pop();
                    String command = message.popString();

                    if (command.equals(NEW)) {
                        String[] interval = message.popString().split(DOUBLE_TRAIT);

                        storages.put(
                                new Pair<>(Integer.parseInt(interval[0]), Integer.parseInt(interval[1])),
                                new Pair<>(adress, System.currentTimeMillis())
                        );

                    } else if (command.equals(NOTIFY)) {
                        String[] interval = message.popString().split(DOUBLE_TRAIT);

                        storages.replace(
                                new Pair<>(Integer.parseInt(interval[0]), Integer.parseInt(interval[1])),
                                new Pair<>(adress, System.currentTimeMillis())
                        );

                    } else {
                        message.wrap(message.pop());
                        message.send(frontend);
                    }

                    if (!more) {
                        break;
                    }
                }
            }
        }
    }

    private static boolean isAlive(Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage) {
        long now = System.currentTimeMillis();
        if (now - storage.getValue().getValue() > DOUBLE_TIMEOUT_MS) {
            storages.remove(storage);
            return false;
        }
        return true;
    }

    private static void putMessageTogetherAndSendToBackend(ArrayList<ZFrame> frames) {
        ZMsg msg = new ZMsg();
        for (ZFrame frame : frames) {
            msg.add(frame);
        }
        msg.send(backend);
    }

    private static void errorMsg(ZFrame adress) {
        ZMsg eMsg = new ZMsg();
        eMsg.wrap(adress);
        eMsg.add(NOT_FOUND);
        eMsg.send(frontend);
    }

    private static void frontendMessageHandler(ZFrame adress, ZMsg message, String command) {

            ZFrame index = message.pop();
            boolean detect = false;
            int indexInteger = Integer.parseInt(String.valueOf(index));

            ZFrame elem = null;
            if (command.equals(PUT)) {
                elem = message.pop();
            }

            for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage : storages.entrySet()) {
                if (indexInteger >= storage.getKey().getKey() && indexInteger < storage.getKey().getValue() && isAlive(storage)) {
                    detect = true;
                    ArrayList<ZFrame> frames = new ArrayList<>();
                    frames.add(storage.getValue().getKey().duplicate());
                    frames.add(adress);
                    frames.add(index);

                    if (command.equals(PUT)) {
                        frames.add(elem);
                    }

                    putMessageTogetherAndSendToBackend(frames);

                    if (command.equals(GET)) {
                        break;
                    }
                }
            }

            if (!detect) {
                errorMsg(adress);
            }


    }
}
