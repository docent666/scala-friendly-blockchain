package com.docent666.ethereum

import java.io.File

import com.docent666.ethereum.Helpers._
import com.micronautics.web3j.Web3JScala.{solc, wrapAbi}
import com.micronautics.web3j.{Address, Web3JScala}
import org.web3j.crypto.{RawTransaction, TransactionEncoder, WalletUtils}
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.core.{DefaultBlockParameter, DefaultBlockParameterName, RemoteCall}
import org.web3j.tx.{Contract, ManagedTransaction, Transfer}
import org.web3j.utils.Convert.Unit.{ETHER, WEI}

import scala.concurrent.ExecutionContext.Implicits.global

//will create a wallet. rename it then to wallet1 or wallet2 to play nicely with subsequent steps
object GenWallet extends App {
  val wallet2 = WalletUtils.generateNewWalletFile("pass",
    new File("src/main/resources/wallets"), false)
}

//requires the accounts to be setup in the test network. extract the private key and use it with e.g. ganache to populate the account with funds:
//ganache-cli --account="<WALLET_PRIVATE_KEY>,<BALANCE>"
object TransferFunds extends App {

  //  println(wallet2.getEcKeyPair.getPrivateKey.toString(16))

  private val wallet1Address = Address(wallet1.getAddress)
  private val wallet2Address = Address(wallet2.getAddress)
  println(s"balance before: ${balanceOf(wallet1Address).asEther}")
  val transactionReceipt: TransactionReceipt =
    Transfer.sendFunds(web3j, wallet1, wallet2.getAddress, BigDecimal.valueOf(1).bigDecimal, ETHER).send
  println(transactionReceipt)
  println(s"balance after: ${balanceOf(wallet1Address).asEther}")
  println(s"balance of second wallet: ${balanceOf(wallet2Address).asEther}")
}

object GenerateContract extends App {

  //to generate abi and bin files
  //needs solc to be setup in path
  solc("src/main/resources/contracts/Hello.sol")
  //needs web3j binary to be set up in PATH
  //instead you can run:
  //../web3jbin/bin/web3j solidity generate abi/Hello.bin abi/Hello.abi  -o src/main/scala -p com.docent666.ethereum
  wrapAbi("Hello")

}

//will deploy a contract (or connect to existing one) and transfer funds to it which contract should accept via fallback method
//subsequently will call a payable function on contract that should transfer the funds to the invoking account
//requires same network setup as TransferFunds
object InteractWithContract extends App {

  val contractPrep: RemoteCall[Hello] = Hello.deploy(web3j, wallet1, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT)
//  val contract = Hello.load(contractAddress, web3j, wallet1, web3jScala.sync.gasPrice.bigInteger, BigInt(100).bigInteger)
//  val contractAddress = "0x9a4e2643b40baf6ca1755fced699b6931dd66230"
  val contract: Hello = contractPrep.send
  val contractAddress = contract.getContractAddress

  //  observe(2)(web3j.blockObservable(false)) { ethBlock =>
//    println(s"block mined: $ethBlock")
//  }

  println(s"wallet balance before: ${balanceOf(Address(wallet1.getAddress)).asWei}")
  val ethGetTransactionCount = web3j.ethGetTransactionCount(wallet1.getAddress, DefaultBlockParameterName.LATEST).sendAsync.get
  val nonce = ethGetTransactionCount.getTransactionCount
  //send funds to contract
  //needs to use raw function as need to provide values to gas price and limit
  val rawTransaction = RawTransaction.createEtherTransaction(nonce, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, contractAddress, BigInt(1337).bigInteger)
  val signedMessage = TransactionEncoder.signMessage(rawTransaction, wallet1)
  val hexValue = signedMessage.map("%02X" format _).mkString
  //  val hexValue: String = javax.xml.bind.DatatypeConverter.printHexBinary(signedMessage)
  val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
  println(s"wallet balance after: ${balanceOf(Address(wallet1.getAddress)).asWei}")
  println(s"balance of contract: ${balanceOf(Address(contractAddress)).asWei}")
  private val receipt = contract.helloPayMe(BigInt(100).bigInteger)
  receipt.send()
  println(s"wallet balance after interacting with contract: ${balanceOf(Address(wallet1.getAddress)).asWei}")
  println(s"balance of contract: ${balanceOf(Address(contractAddress)).asWei}")
}

object Helpers {
  val wallet1 = WalletUtils.loadCredentials("pass", new File("src/main/resources/wallets/wallet1.json"))
  val wallet2 = WalletUtils.loadCredentials("pass", new File("src/main/resources/wallets/wallet2.json"))
  val web3j: Web3j = Web3JScala.fromHttp() // defaults to http://localhost:8545/
  val web3jScala: Web3JScala = new Web3JScala(web3j)

  def balanceOf(wallet: Address) = {
    web3jScala.sync.balance(wallet, DefaultBlockParameter.valueOf("latest"))
  }
}