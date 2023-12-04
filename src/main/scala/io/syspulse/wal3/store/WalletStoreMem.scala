package io.syspulse.wal3.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.wal3.{Wallet}
import io.syspulse.wal3.WalletSecret

class WalletStoreMem extends WalletStore {
  val log = Logger(s"${this}")
  
  var wallets: Map[String,WalletSecret] = Map()

  def all(oid:Option[UUID]):Seq[WalletSecret] = 
    if(oid==None) 
      wallets.values.toSeq 
    else 
      wallets.values.filter(_.oid == oid).toSeq

  def size:Long = wallets.size

  def +++(w:WalletSecret):Try[WalletSecret] = {     
    wallets = wallets + (w.addr -> w)    
    Success(w)
  }

  def del(addr:String,oid:Option[UUID]):Try[WalletSecret] = {         
    wallets.get(addr) match {
      case Some(w) if w.oid == oid =>
        wallets = wallets - addr
        Success(w)
      case Some(_) | None => 
        Failure(new Exception(s"not found: ${addr}"))
    }
  }

  def ???(addr:String,oid:Option[UUID]):Try[WalletSecret] = wallets.get(addr) match {
    case Some(w) if(w.oid == oid) => Success(w)
    case _ => Failure(new Exception(s"not found: ${addr}"))
  }
 
}
