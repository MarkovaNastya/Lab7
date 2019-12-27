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
                        int index = Integer.parseInt(message.popString());

                        for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage : storages.entrySet()) {
                            if (index >= storage.getKey().getKey() && index < storage.getKey().getValue()) {

                                ArrayList<ZFrame> frames = new ArrayList<>();
                                frames.add(storage.getValue().getKey().duplicate());
                                frames.add(adress);
                                frames.add(index);

                            }
                        }

                    } else if (command.equals("SET")) {

                        int index = Integer.parseInt(message.popString());
                        String elem = message.popString();



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

                    }



                    if (!more) {
                        break;
                    }
                }


            }


        }



    }


    private static void putMessageTogetherAndSendToBackend(String[] frames) {
        ZMsg msg = new ZMsg();



        msg.send(backend);
    }
}
