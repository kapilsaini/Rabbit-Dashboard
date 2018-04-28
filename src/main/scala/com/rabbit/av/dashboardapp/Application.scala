package com.rabbit.av.dashboardapp

import scala.swing.SimpleSwingApplication

import com.rabbit.av.camera._
import com.rabbit.av.manager.DataDumper
import com.rabbit.av.manager.ImageDumper
import com.rabbit.av.manager.dashboardManager
import com.rabbit.av.ui._
import com.rabbit.av.util._
import com.rabbit.av.util.FrameDimensions
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.rabbit.av.manager.ControlRequestManager
import akka.stream.KillSwitches
import akka.stream.scaladsl.Keep
import scala.concurrent.Future
import akka.actor.PoisonPill
import akka.actor.Props

object Application extends SimpleSwingApplication {   
  implicit val system = ActorSystem("rabbit-dashboard")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  val top = new UI()
  //val sserver = new NewServer(top)
  //val sserver = new ServerAgain()
  lazy val config = ConfigFactory.load()
  
  val width = config.getInt("imageWidth")
  val height = config.getInt("imageHeight")
  val remoteCamera = config.getString("remoteCameraHost")

//  val webcamSource = Future{Webcam.source(0, FrameDimensions(width, height))}
 // val webcamSource = RemoteWebcam(remoteCamera, FrameDimensions(width, height))
  
  val dataActor = system.actorOf(DataDumper.props)
  val imageActor = system.actorOf(ImageDumper.props)
  val controlRequestActor = system.actorOf(ControlRequestManager.props)
  val listenerActor = system.actorOf(Props(new NewServer(top)))
  
  dashboardManager.setdataActorRef(dataActor)
  dashboardManager.setImageActorRef(imageActor)
  dashboardManager.setHttpActorRef(controlRequestActor)
  
  listenerActor ! "INIT"
//  val appFlow = webcamSource
//    .map(
//    _.viaMat(KillSwitches.single)(Keep.right)
//    .map(MediaEditor.frameToBuffered)
//    .map(dashboardManager.setCurrentNode)
//    .map(top.displayVideoFrame)
//    .to(Sink.ignore))
//  
//  val appFlowController = appFlow.map(_.run())
  
  def shutdownStreaming() {
    println("Shutting down Streaming")
//    appFlowController.map(
//        _.shutdown())
  }
  
  def killActors() {
    dataActor ! Terminate
    dataActor ! PoisonPill
    imageActor ! PoisonPill
    controlRequestActor ! PoisonPill
    system.terminate()
  }
  
  def shutdownApplication() {
    shutdownStreaming()
    killActors()
  }
}