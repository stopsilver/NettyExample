import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

/**
 * Created by Jieun on 2017. 1. 2..
 */
public class EchoClientInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String sendMessage = "Hey Server get this!";

        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());

        StringBuilder builder = new StringBuilder();
        builder.append("Client sent: [");
        builder.append(sendMessage);
        builder.append("]");
        System.out.println(builder.toString());
        ctx.writeAndFlush(messageBuffer);
    }

    // 채널을 읽을 때 동작할 코드를 정의 합니다.
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("in channelRead");
//		ByteBuf input = (ByteBuf) msg;
//		byte[] bytes = input.array();
//		System.out.println(bytes);
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());

        System.out.println("Client got: [" + readMessage + ']');

        if (!readMessage.equals("You are connected to Server")) {
            ctx.write("return from client: " + msg);
        }
    }

    // 채널 읽는 것을 완료했을 때 동작할 코드를 정의 합니다.
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        ctx.flush(); // 컨텍스트의 내용을 플러쉬합니다.
    }

//	@Override
//	public ChannelPipeline getPipeline() throws Exception {
//		ChannelPipeline pipeline=Channels.pipeline();
//		pipeline.addLast("lengthbasedframedecoder",new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH,0,4,0,4));
//		pipeline.addLast("mainhandler",this);
//		return pipeline;
//	}

    // 예외가 발생할 때 동작할 코드를 정의 합니다.
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("in exceptionCaught");
        cause.printStackTrace(); // 쌓여있는 트레이스를 출력합니다.
        ctx.close(); // 컨텍스트를 종료시킵니다.
    }

}