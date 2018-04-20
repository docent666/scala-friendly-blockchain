package com.docent666.corda

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort

object CordaClient extends App {
  val client = new CordaRPCClient(new NetworkHostAndPort("localhost", 10006))
  //user and password and their permissions need to be setup in the node config
  val blah = client.start("user1", "test")
  //illegal cyclic reference
  //println(blah.getProxy.nodeInfo())
}
