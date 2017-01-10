import io.netty.channel.ChannelOutboundHandlerAdapter;


public class EchoClientOutboundHandler extends ChannelOutboundHandlerAdapter {
    public void bind() {
    }

    public void connect() {
        System.out.println("client connecting.....");
    }

    public void read() {
        System.out.println("client reading.....");
    }
}
