import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;

import java.nio.charset.Charset;

/**
 * Created by Jieun on 2017. 1. 2..
 */
public class EchoClientOutboundHandler extends ChannelOutboundHandlerAdapter {
    public void bind() {
        System.out.println("client binding.....");
    }

    public void connect() {
        System.out.println("client connecting.....");
    }

    public void read() {
        System.out.println("client reading.....");
    }
}
