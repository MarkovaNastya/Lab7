import org.zeromq.ZContext;
import org.zeromq.*;
import org.zeromq.ZMQ.Socket;
import zmq.poll.Poller;

public class Server {
    public static void main(String[] args) {
        ZContext context = new ZContext();

        Socket frontend = context.createSocket(SocketType.ROUTER);
        Socket backend = context.createSocket(SocketType.ROUTER);

        ZMQ.Poller items = context.createPoller(2);
        items.register(frontend, ZMQ.Poller.POLLIN);
        items.register(backend, ZMQ.Poller.POLLIN);



    }
}
