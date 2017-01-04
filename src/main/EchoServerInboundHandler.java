import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import static java.lang.String.valueOf;

public class EchoServerInboundHandler extends ChannelInboundHandlerAdapter {

    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        System.out.println("in channelRead0");
    }

    public void channelActive(ChannelHandlerContext ctx) {
//        String sendMessage = "You are connected to Server";
//
//        ByteBuf messageBuffer = Unpooled.buffer();
//
//        messageBuffer.writeBytes(sendMessage.getBytes());
//
////        StringBuilder builder = new StringBuilder();
////        builder.append("전송한 문자열 [");
////        builder.append(sendMessage);
////        builder.append("]");
////        System.out.println(builder.toString());
//        ctx.writeAndFlush(messageBuffer);
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("channel inactivate");
    }
    // 채널을 읽을 때 동작할 코드를 정의 합니다.
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
        Object[] Type = {};

        int num = 0;
        for (int i=0; i<CHComposition.length; i++) {
            ByteBuf slice = CommunicationHeader.slice(num, CHlength[i]);
            InputValue.put(CHComposition[i], slice);
            num += CHlength[i];
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Server got: [");

        for (String i: CHComposition) {
            ByteBuf buf = InputValue.get(i);

            if (i.equals("Stx") || i.equals("HeaderFiller")) {
                for (int j = 0; j < buf.readableBytes(); j++) {
                    int k = (int) buf.getByte(j);
                    String str = valueOf(k);
                    System.out.println(str);
                    builder.append(str);
                }
            }
            else {
                builder.append(InputValue.get(i).toString(Charset.defaultCharset()));
            }
        }

        builder.append("]");
        System.out.println(builder.toString());


        ByteBuf TRcode = InputValue.get("TRcode");
        ByteBuf SendingTimeBuf = InputValue.get("SendingTime");

        System.out.println("60");

        if (TRcode.hasArray()) {
            if (Arrays.equals(TRcode.array(), "LINK".getBytes())) {
                TRcode.clear();
                TRcode.writeBytes("LIOK".getBytes());
                InputValue.put("Stx", TRcode);
            }
        }

        System.out.println("80");
        SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMddHHmmssSSSSSS", Locale.KOREA );
        String SendingTime = formatter.format(new Date());
        SendingTimeBuf.clear();
        SendingTimeBuf.writeBytes(SendingTime.getBytes());
        InputValue.put("SendingTime", SendingTimeBuf);

        CommunicationHeader.clear();

        System.out.println("90");
        for (String i: CHComposition) {
            ByteBuf Bytebuf = InputValue.get(i);
            CommunicationHeader.writeBytes(Bytebuf);
        }

        System.out.println("100");

//        for (ByteBuf i: InputValue.values()) {
//            String j;
//            int a = i.getInt(0);
//
//            if (a == 0x02) {
//                j = Integer.toHexString(a);
//            }
//            else {
//                j = i.toString(Charset.defaultCharset());
//            }
//            System.out.println(j);
//        }
        StringBuilder builder1 = new StringBuilder();
        builder1.append("Server sent: [");

        for (String i: CHComposition) {
            ByteBuf buf1 = InputValue.get(i);

            if (i.equals("Stx") || i.equals("HeaderFiller")) {
                for (int j = 0; j < buf1.readableBytes(); j++) {
                    int k = (int) buf1.getByte(j);
                    String str = valueOf(k);
                    System.out.println(str);
                    builder1.append(str);
                }
            }
            else {
                builder1.append(InputValue.get(i).toString(Charset.defaultCharset()));
            }
        }

        builder1.append("]");
        System.out.println(builder1.toString());

        ctx.write(CommunicationHeader);
        ctx.write(DataHeader);
        ctx.write(DataBody);
        ctx.writeAndFlush(Filler);
    }

    // 채널 읽는 것을 완료했을 때 동작할 코드를 정의 합니다.
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        ctx.flush(); // 컨텍스트의 내용을 플러쉬합니다.
    }

    // 예외가 발생할 때 동작할 코드를 정의 합니다.
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("in exceptionCaught");
        cause.printStackTrace(); // 쌓여있는 트레이스를 출력합니다.
        ctx.close(); // 컨텍스트를 종료시킵니다.
    }
}