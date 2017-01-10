import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import static java.lang.String.valueOf;

public class EchoServerInboundHandler extends ChannelInboundHandlerAdapter {

    public void channelActive(ChannelHandlerContext ctx) {
        Integer timeGap = 1000;

        Timer t = new Timer(true);
        t.schedule(new sendPoll(ctx), 0, timeGap);
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("channel inactivate");
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("in channelRead");

        ByteBuf input = (ByteBuf) msg;

        ByteBuf CommunicationHeader = input.copy(0, 60);
        ByteBuf DataHeader = input.copy(60, 160);
        ByteBuf DataBody = input.copy(160, 300);
        ByteBuf Filler = input.copy(460, 100);

        HashMap<String, ByteBuf> InputValue = new HashMap<String, ByteBuf>();

        String[] CHComposition = {"Stx", "Version", "Len", "ApType", "SendingTime", "TRcode", "SeqNo", "DataCnt", "HeaderFiller"};
        Integer[] CHlength = {1, 4, 7, 6, 20, 4, 11, 3, 4};

        int num = 0;
        for (int i=0; i<CHComposition.length; i++) {
            ByteBuf slice = CommunicationHeader.slice(num, CHlength[i]);
            InputValue.put(CHComposition[i], slice);
            num += CHlength[i];
        }

        readMessage(CHComposition, InputValue, "Server got: [");


        ByteBuf TRcode = InputValue.get("TRcode");
        ByteBuf SendingTimeBuf = InputValue.get("SendingTime");

        System.out.println("60");

        byte[] TRcodeArray = BytebufToArray(TRcode);
        if (Arrays.equals(TRcodeArray, "LINK".getBytes())) {
            TRcode.clear();
            TRcode.writeBytes("LIOK".getBytes());
            InputValue.put("TRcode", TRcode);
        }

        SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMddHHmmssSSSSSS", Locale.KOREA );
        String SendingTime = formatter.format(new Date());
        SendingTimeBuf.clear();
        SendingTimeBuf.writeBytes(SendingTime.getBytes());
        InputValue.put("SendingTime", SendingTimeBuf);

        CommunicationHeader.clear();

        for (String i: CHComposition) {
            ByteBuf putBuf = InputValue.get(i);
            CommunicationHeader.writeBytes(BytebufToArray(putBuf));
        }

        readMessage(CHComposition, InputValue, "Server sent: [");

        ctx.write(CommunicationHeader);
        ctx.write(DataHeader);
        ctx.write(DataBody);
        ctx.writeAndFlush(Filler);
    }


    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("in exceptionCaught");
        cause.printStackTrace();
        ctx.close();
    }

    public byte[] BytebufToArray(ByteBuf a) {
        int len = a.readableBytes();
        byte[] byteArray = new byte[len];
        a.getBytes(a.readerIndex(), byteArray);

        return byteArray;
    }

    private void readMessage(String[] component, HashMap<String, ByteBuf> componentMap, String addMsg) {
        StringBuilder builder1 = new StringBuilder();
        builder1.append(addMsg);

        for (String i: component) {
            ByteBuf buf1 = componentMap.get(i);

            if (i.equals("Stx") || i.equals("HeaderFiller")) {
                for (int j = 0; j < buf1.readableBytes(); j++) {
                    int k = (int) buf1.getByte(j);
                    String str = valueOf(k);
                    builder1.append(str);
                }
            }
            else {
                builder1.append(buf1.toString(Charset.defaultCharset()));
            }
        }

        builder1.append("]");
        System.out.println(builder1.toString());
    }
}


class sendPoll extends TimerTask {
    private ChannelHandlerContext ctx;

    public sendPoll(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void run() {
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

        SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMddHHmmssSSSSSS", Locale.KOREA );
        String SendingTime = formatter.format(new Date());

        String Trcode = "POLL";

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
