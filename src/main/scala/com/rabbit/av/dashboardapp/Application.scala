package com.rabbit.av.dashboardapp

import scala.swing.SimpleSwingApplication

import com.rabbit.av.camera.StreamingServer
import com.rabbit.av.manager.ControlRequestManager
import com.rabbit.av.manager.DataDumper
import com.rabbit.av.manager.ImageDumper
import com.rabbit.av.manager.dashboardManager
import com.rabbit.av.ui.UI
import com.rabbit.av.util.Terminate

import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.actor.Props
import akka.stream.ActorMaterializer

object Application extends SimpleSwingApplication {   
  implicit val system = ActorSystem("rabbit-dashboard")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  val top = new UI()
  
  val dataActor = system.actorOf(DataDumper.props)
  val imageActor = system.actorOf(ImageDumper.props)
  val controlRequestActor = system.actorOf(ControlRequestManager.props)
  val streamReciever = system.actorOf(Props(new StreamingServer(top)))
  
  dashboardManager.setdataActorRef(dataActor)
  dashboardManager.setImageActorRef(imageActor)
  dashboardManager.setHttpActorRef(controlRequestActor)
  
  streamReciever ! "INIT"

  
  def shutdownSystem() {
    dataActor ! Terminate
    dataActor ! PoisonPill
    imageActor ! PoisonPill
    controlRequestActor ! PoisonPill
    streamReciever ! "TERMINATE"
    system.terminate()
    sys.exit()
  }
}