import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

public final class EchoClient {	
	public static void main(String[] args) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap b = new Bootstrap();
			b.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
//					p.addLast(new LengthFieldBasedFrameDecoder(16, 0, 2, 0, 6));
//					p.addLast("lengthFieldBasedFrameDecoder",
//							new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, 9, 9, 0, 9, true));
					p.addLast(new EchoClientInboundHandler());
					p.addLast(new EchoClientOutboundHandler());
//					p.addLast("lengthFieldBasedFrameDecoder",
//							new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 30, 9, 9, 0, 9, true));

				}
			});
			
			ChannelFuture f = b.connect("localhost", 8888).sync();
			f.channel().closeFuture().sync();
			
		}
		
		finally {
			group.shutdownGracefully();
		}
	}
}

