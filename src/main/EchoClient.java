import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public final class EchoClient {
	public static Bootstrap b = new Bootstrap();
	public static void main(String[] args) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			b.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					System.out.println("in try");
					p.addLast(new EchoClientOutboundHandler());
					p.addLast(new EchoClientInboundHandler());
					p.addFirst(new ReadTimeoutHandler(10));
				}
			});

			ChannelFuture f = b.connect("localhost", 8888).sync();
			f.channel().closeFuture().sync();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		finally {
			group.shutdownGracefully();
		}
	}
}

