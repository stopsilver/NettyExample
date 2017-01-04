import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jieun on 2017. 1. 2..
 */
public class EchoClientInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        ByteBuf CommunicationHeader = Unpooled.buffer(60);
        ByteBuf DataHeader = Unpooled.buffer(100);
        ByteBuf DataBody = Unpooled.buffer(300);
        ByteBuf Filler = Unpooled.buffer(100);

        DataHeader.setZero(0, DataHeader.writableBytes());
        DataBody.setZero(0, DataBody.writableBytes());
        Filler.setZero(0, Filler.writableBytes());


        byte[] Stx = {0x02};
        String Version = "v1.0";
        String MsgLen = "000160";
        String ApType = "C115TR";

        SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMddHHmmssSSSSSS", Locale.KOREA );
        String SendingTime = formatter.format(new Date());

        String Trcode = "LINK";

        String SeqNo = "00000000000";
        String DataCnt = "000";

        CommunicationHeader.writeBytes(Stx);
        CommunicationHeader.writeBytes(Version.getBytes());
        CommunicationHeader.writeBytes(MsgLen.getBytes());
        CommunicationHeader.writeBytes(ApType.getBytes());
        CommunicationHeader.writeBytes(SendingTime.getBytes());
        CommunicationHeader.writeBytes(Trcode.getBytes());
        CommunicationHeader.writeBytes(SeqNo.getBytes());
        CommunicationHeader.writeBytes(DataCnt.getBytes());

        CommunicationHeader.setZero(CommunicationHeader.readableBytes(), CommunicationHeader.capacity() - CommunicationHeader.readableBytes());
        System.out.println(CommunicationHeader.toString(Charset.defaultCharset()));

//        sendMessage.addComponents(CommunicationHeader, DataHeader, DataBody, Filler);
//
//        for (ByteBuf buf: sendMessage) {
//            System.out.println(buf.toString(Charset.defaultCharset()));
//        }

//        String Message = "Hello";
//        ByteBuf sendMessage = Unpooled.buffer();
//        sendMessage.writeBytes(Message.getBytes());

        StringBuilder builder = new StringBuilder();
        builder.append("Client sent: [");
        String StxString = Integer.toHexString(Stx[0]);
//        builder.append(Message);
        builder.append(StxString + Version + MsgLen + ApType + SendingTime + Trcode + SeqNo + DataCnt);
        builder.append("]");
        System.out.println(builder.toString());
        ctx.write(CommunicationHeader);
        ctx.write(DataHeader);
        ctx.write(DataBody);
        ctx.writeAndFlush(Filler);
//        ctx.writeAndFlush(sendMessage);
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