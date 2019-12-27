import org.zeromq.ZContext;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;
import zmq.poll.Poller;

public class Server {
    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        Socket frontend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://localhost:5559");

        Socket backend = context.socket(SocketType.ROUTER);
        backend.bind("tcp://localhost:5559");

        ZMQ.Poller items = context.poller(2);
        items.register(frontend, ZMQ.Poller.POLLIN);
        items.register(backend, ZMQ.Poller.POLLIN);

        boolean more = false;
        byte[] message;

        while (!Thread.currentThread().isInterrupted()) {
            items.poll();

            if (items.pollin(0)) {
                while (true) {
                    message = frontend.recv(0);
                    more = frontend.hasReceiveMore();


                    if (!more) {
                        break;
                    }
                }

            }

            if (items.pollin(1)) {
                message = backend.recv(0);
                more = backend.hasReceiveMore();


                if (!more) {
                    break;
                }

            }


        }



    }
}
