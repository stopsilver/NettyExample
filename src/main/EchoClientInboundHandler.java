import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import static java.lang.String.valueOf;

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
        String MsgLen = "0000160";
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

    // 채널을 읽을 때 동작할 코드를 정의 합니다.
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("in channelRead");

        ByteBuf input = (ByteBuf) msg;
        ByteBuf CommunicationHeader = input.copy(0, 60);

        HashMap<String, ByteBuf> InputValue = new HashMap<String, ByteBuf>();

        String[] CHComposition = {"Stx", "Version", "Len", "ApType", "SendingTime", "TRcode", "SeqNo", "DataCnt", "HeaderFiller"};
        Integer[] CHlength = {1, 4, 7, 6, 20, 4, 11, 3, 4};

        int num = 0;
        for (int i=0; i<CHComposition.length; i++) {
            ByteBuf slice = CommunicationHeader.slice(num, CHlength[i]);
            InputValue.put(CHComposition[i], slice);
            num += CHlength[i];
        }
        readMsg(CHComposition, InputValue, "Client got: [");
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