
import java.nio.charset.Charset;

import com.sun.tools.javac.util.ArrayUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class EchoClinetHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		String sendMessage = "Hello Netty!";
		
		ByteBuf messageBuffer = Unpooled.buffer();

		byte lengthbyte = (byte) sendMessage.length();

		byte[] body = sendMessage.getBytes();
		byte[] header = new byte[]{lengthbyte};


		byte[] combined = new byte[header.length + body.length];

		for (int i = 0; i < combined.length; ++i)
		{
			combined[i] = i < header.length ? header[i] : body[i - header.length];
		}

		messageBuffer.writeBytes(combined);
		System.out.println("combine to String");

		StringBuilder builder = new StringBuilder();
		builder.append("전송한 문자열 [");
		builder.append(sendMessage);
		builder.append("]");
		System.out.println(builder.toString());
		ctx.writeAndFlush(messageBuffer);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
//		System.out.println((ByteBuf) msg);
		String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());
		StringBuilder builder = new StringBuilder();
		builder.append("수신 문자열 [");
		builder.append(readMessage);
		builder.append("]");
		System.out.println(builder.toString());
	}
	
	public void ChannelReadComplete(ChannelHandlerContext ctx) {
		ctx.close();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	

}
