import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;

public class EchoServerOutboundHandler extends ChannelOutboundHandlerAdapter {

    public void bind() {
        System.out.println("server binding.....");
    }

    public void connect() {
        System.out.println("server connecting.....");
    }

    public void read() {
        System.out.println("server reading.....");
    }
}