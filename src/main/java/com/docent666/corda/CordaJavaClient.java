package com.docent666.corda;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;


//demo of interaction with a Corda node
public class CordaJavaClient {

    public static void main(String[] args) {
        CordaRPCClient client = new CordaRPCClient(new NetworkHostAndPort("localhost", 10012));
        //user and password and their permissions need to be setup in the node config
        CordaRPCOps node = client.start("user1", "test").getProxy();
        System.out.println(node.nodeInfo());
    }
}
