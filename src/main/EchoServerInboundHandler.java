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

        int num = 0;
        for (int i=0; i<CHComposition.length; i++) {
            ByteBuf slice = CommunicationHeader.slice(num, CHlength[i]);
            InputValue.put(CHComposition[i], slice);
            num += CHlength[i];
        }

        readMsg(CHComposition, InputValue, "Server got: [");


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

        readMsg(CHComposition, InputValue, "Server sent: [");

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

    public byte[] BytebufToArray(ByteBuf a) {
        int len = a.readableBytes();
        byte[] byteArray = new byte[len];
        a.getBytes(a.readerIndex(), byteArray);

        return byteArray;
    }

    private void readMsg(String[] component, HashMap<String, ByteBuf> componentMap, String addMsg) {
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