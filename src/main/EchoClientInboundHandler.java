import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import static java.lang.String.valueOf;


public class EchoClientInboundHandler extends ChannelInboundHandlerAdapter {

    CommonCode a = new CommonCode();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("in channelActive");
        sendLINKMessage(ctx);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("in channelRead");

        ByteBuf input = (ByteBuf) msg;
        ByteBuf CommunicationHeader = input.copy(0, 60);

        HashMap<String, ByteBuf> InputValue = new HashMap<String, ByteBuf>();

        String[] CHComposition = {"Stx", "Version", "Len", "ApType", "SendingTime", "TRcode", "SeqNo", "DataCnt", "HeaderFiller"};
        Integer[] CHlength = {1, 4, 7, 6, 20, 4, 11, 3, 4};

        int num = 0;
        for (int i = 0; i < CHComposition.length; i++) {
            ByteBuf slice = CommunicationHeader.slice(num, CHlength[i]);
            InputValue.put(CHComposition[i], slice);
            num += CHlength[i];
        }
        a.readMessage(CHComposition, InputValue, "Client got: [");
        // difference btw input and output value('0000') is because of filler print
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("in exceptionCaught");
        System.out.println(cause);
        if (cause instanceof ReadTimeoutException) {
            EchoClient client = new EchoClient();
            Bootstrap b = client.b;
            ChannelFuture f = b.connect("localhost", 8888).sync();
            f.channel().closeFuture().sync();
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("in channelInactive");
    }

    private void sendLINKMessage(ChannelHandlerContext ctx) {
        ByteBuf CommunicationHeader = Unpooled.buffer(60);
        ByteBuf DataHeader = Unpooled.buffer(100);
        ByteBuf DataBody = Unpooled.buffer(300);
        ByteBuf Filler = Unpooled.buffer(100);

        DataHeader.setZero(0, DataHeader.writableBytes());
        DataBody.setZero(0, DataBody.writableBytes());
        Filler.setZero(0, Filler.writableBytes());

        byte[] Stx = {0x02};
        String Version = "v1.0";
        String MsgLen = "0000160";
        String ApType = "C115TR";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS", Locale.KOREA);
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


        StringBuilder builder = new StringBuilder();
        builder.append("Client sent: [");
        String StxString = Integer.toHexString(Stx[0]);
        builder.append(StxString + Version + MsgLen + ApType + SendingTime + Trcode + SeqNo + DataCnt);
        builder.append("]");
        System.out.println(builder.toString());
        ctx.write(CommunicationHeader);
        ctx.write(DataHeader);
        ctx.write(DataBody);
        ctx.writeAndFlush(Filler);
    }

}
