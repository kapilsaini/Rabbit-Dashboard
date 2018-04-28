package com.rabbit.av.manager
import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer

case class ControlMoves(moves: List[Char])
class ControlRequestManager extends Actor {
  
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config = ConfigFactory.load()
  val http = Http(context.system)
  lazy val remoteControllerHost = config.getString("remoteControllerHost")
    
  def receive = {
    case controlMoves: ControlMoves =>
      sendControlCommand(controlMoves)
  }
  
  def sendControlCommand(controlMoves: ControlMoves) = {
    val queryParams = s"/${controlMoves.moves.mkString(",")}"
    val reqUri = s"${remoteControllerHost}${queryParams}"
    println(s"Triggring http request ${reqUri}")
    http.singleRequest(HttpRequest(uri = reqUri))
  }
}
object ControlRequestManager {
  val props = Props[ControlRequestManager]
}