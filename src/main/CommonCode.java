import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.HashMap;

import static java.lang.String.valueOf;

public class CommonCode {
    public void readMessage(String[] component, HashMap<String, ByteBuf> componentMap, String addMsg) {
        StringBuilder builder = new StringBuilder();
        builder.append(addMsg);

        for (String i : component) {
            ByteBuf buf = componentMap.get(i);

            if (i.equals("Stx") || i.equals("HeaderFiller")) {
                for (int j = 0; j < buf.readableBytes(); j++) {
                    int k = (int) buf.getByte(j);
                    String str = valueOf(k);
                    builder.append(str);
                }
            } else {
                builder.append(buf.toString(Charset.defaultCharset()));
            }
        }

        builder.append("]");
        System.out.println(builder.toString());
    }
}
