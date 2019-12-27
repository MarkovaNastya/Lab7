import javafx.util.Pair;
import org.zeromq.ZContext;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;
import zmq.poll.Poller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static HashMap<Pair<Integer, Integer>, Pair<ZFrame, Long>> storages = new HashMap<>();
    private static Socket frontend;
    private static Socket backend;

    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        frontend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://localhost:5559");

        backend = context.socket(SocketType.ROUTER);
        backend.bind("tcp://localhost:5560");

        ZMQ.Poller items = context.poller(2);
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
                    System.out.println(message);

                    ZFrame adress = message.pop();
                    ZFrame nullFrame = message.pop();

                    String command = message.popString();

                    if (command.equals("GET")) {
                        ZFrame index = message.pop();
                        int indexInteger = Integer.parseInt(String.valueOf(index));

                        for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage : storages.entrySet()) {
                            if (indexInteger >= storage.getKey().getKey() && indexInteger < storage.getKey().getValue()) {

                                ArrayList<ZFrame> frames = new ArrayList<>();
                                frames.add(storage.getValue().getKey().duplicate());
                                frames.add(adress);
                                frames.add(index);
                                putMessageTogetherAndSendToBackend(frames);
                                break;

                            }
                        }

                    } else if (command.equals("SET")) {

                        ZFrame index = message.pop();
                        int indexInteger = Integer.parseInt(String.valueOf(index));
                        ZFrame elem = message.pop();

                        for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage : storages.entrySet()) {
                            if (indexInteger >= storage.getKey().getKey() && indexInteger < storage.getKey().getValue()) {

                                ArrayList<ZFrame> frames = new ArrayList<>();
                                frames.add(storage.getValue().getKey().duplicate());
                                frames.add(adress);
                                frames.add(index);
                                frames.add(elem);
                                putMessageTogetherAndSendToBackend(frames);
                                break;

                            }
                        }

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
                    System.out.println(message);

                    ZFrame adress = message.pop();
                    String command = message.popString();

                    if (command.equals("NEW")) {
                        String[] interval = message.popString().split("//");

                        storages.put(
                                new Pair<>(Integer.parseInt(interval[0]), Integer.parseInt(interval[1])),
                                new Pair<>(adress, System.currentTimeMillis())
                        );

                    } else if (command.equals("I STILL ALIVE")) {
                        String[] interval = message.popString().split("//");

                        storages.replace(
                                new Pair<>(Integer.parseInt(interval[0]), Integer.parseInt(interval[1])),
                                new Pair<>(adress, System.currentTimeMillis())
                        );

                    } else {
                        message.wrap(message.pop());
                        frontend.send(message.popString());
                    }

                    if (!more) {
                        break;
                    }
                }
            }
        }
    }


    private static void putMessageTogetherAndSendToBackend(ArrayList<ZFrame> frames) {
        ZMsg msg = new ZMsg();

        for (ZFrame frame : frames) {
            msg.add(frame);
        }

        msg.send(backend);
    }
}
