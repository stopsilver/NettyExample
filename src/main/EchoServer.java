import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

public class EchoServer {
	
	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		System.out.println("line 18");
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) {
					ChannelPipeline p = ch.pipeline();
					System.out.println("line 27");
//					p.addLast("frameDecoder",
//							new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, 0, 1, 0, 1, true));

					p.addLast(new EchoServerInboundHandler());
					p.addLast(new EchoServerOutboundHandler());
//					p.addLast("lengthFieldBasedFrameDecoder",
//							new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 30, 9, 9, 0, 9, true));
					System.out.println("line 33");

				}
	
			});
			System.out.println("line 39");
			ChannelFuture f = b.bind(8888).sync();
			System.out.println("line 41");
			f.channel().closeFuture().sync();
			System.out.println("line 43");
			
		}
		
		finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
