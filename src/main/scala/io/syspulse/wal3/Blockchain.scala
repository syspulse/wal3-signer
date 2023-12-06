package io.syspulse.wal3

import scala.util.{Try,Success,Failure}
import scala.concurrent.Future
import scala.collection.immutable
import com.typesafe.scalalogging.Logger
import io.jvm.uuid._

import io.syspulse.skel.util.Util

import org.web3j.protocol.Web3j
import io.syspulse.skel.crypto.Eth

case class Blockchain(name:String,id:Long,rpcUri:String) 

class Blockchains(bb:Seq[String]) {

  override def toString():String = blockchains.toString

  protected var blockchains:Map[Long,Blockchain] = Map(
    11155111L -> Blockchain("sepolia",11155111L,"https://eth-sepolia.public.blastapi.io")
  )

  def ++(bb:Seq[String]):Blockchains = {
    val newBlockchains = bb.map(b =>{
      b.trim.split("=").toList match {
        case id :: name :: rpc :: _ => 
          ( id.toLong ->  Blockchain(name,id.toLong,rpc), id.toLong -> Eth.web3(rpc) )
        case id :: rpc :: Nil => 
          ( id.toLong ->  Blockchain(id.toString,id.toLong,rpc), id.toLong -> Eth.web3(rpc) )
        case rpc :: Nil => 
          ( 1L ->  Blockchain("mainnet",1L,rpc), 1L -> Eth.web3(rpc) )
      }
    })
    blockchains = blockchains ++ newBlockchains.map(_._1).toMap
    rpc = rpc ++ newBlockchains.map(_._2).toMap
    this
  }

  // map of connections
  var rpc:Map[Long,Web3j] = blockchains.values.map( b => {
    b.id -> Eth.web3(b.rpcUri)
  }).toMap

  def get(id:Long) = blockchains.get(id)
  def getByName(name:String) = blockchains.values.find(_.name == name.toLowerCase())
  def getWeb3(id:Long) = rpc.get(id) match {
    case Some(web3) => Success(web3)
    case None => Failure(new Exception(s"not found: ${id}"))
  }

  def all():Seq[Blockchain] = blockchains.values.toSeq

  this.++(bb)
}

object Blockchains {
  def apply(bb:Seq[String]) = new Blockchains(bb)
}