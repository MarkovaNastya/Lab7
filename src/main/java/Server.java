import javafx.util.Pair;
import org.zeromq.ZContext;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;
import zmq.poll.Poller;

import java.util.HashMap;

public class Server {

    private static HashMap<Pair<Integer, Integer>, Pair<ZFrame, Long>> storages;

    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        Socket frontend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://localhost:5559");

        Socket backend = context.socket(SocketType.ROUTER);
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


                    if (!more) {
                        break;
                    }
                }

            }

            //backend
            if (items.pollin(1)) {
                message = ZMsg.recvMsg(backend);
                more = backend.hasReceiveMore();

                ZFrame adress = message.pop();
                String command = message.popString();

                System.out.println(command);

                if (command.equals("NEW")) {
                    String[] interval = message.popString().split("//");
                    String left = interval[0];
                    String right = interval[1];

                    storages.put(
                            new Pair<>(Integer.parseInt(left), Integer.parseInt(right)),
                            new Pair<>(adress, System.currentTimeMillis())
                    );

                    System.out.println("new storage added");

                }







                if (!more) {
                    break;
                }

            }


        }



    }
}
