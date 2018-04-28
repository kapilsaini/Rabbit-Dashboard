package com.rabbit.av.camera

import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

import com.rabbit.av.manager.dashboardManager
import com.rabbit.av.ui.UI

import akka.actor.Actor
import akka.stream.ActorMaterializer
import javax.imageio.ImageIO
import com.typesafe.config.ConfigFactory


class StreamingServer(top: UI) extends Actor {
  implicit val materializer = ActorMaterializer()(context.system)
  var socketServer: ServerSocket = null
  var server: Socket = null

  lazy val config = ConfigFactory.load()
  
	val port: Int = config.getInt("streamingPort")
	
	private val MAX_IMG = 1024 * 1024 * 1024
	
	def receive = {
    case "INIT" => run()
    case "TERMINATE" => shutdown()
  }
  def run() {
    socketServer = new ServerSocket(port)
    while(true) { 
      try {
        server = socketServer.accept()
        if (server.isConnected()){
          val inStream = new BufferedInputStream(server.getInputStream)
          while(true){
            inStream.mark(MAX_IMG)
            val imgStream = ImageIO.createImageInputStream(inStream)
            val imgIterator = ImageIO.getImageReaders(imgStream)
            if (!imgIterator.hasNext()) {
            } else {
              val reader = imgIterator.next()
              reader.setInput(imgStream)
              val img: BufferedImage = reader.read(0)
              dashboardManager.setCurrentNode(img)
              top.displayVideoFrame(img)
              val bytesRead = imgStream.getStreamPosition()
              inStream.reset()
              inStream.skip(bytesRead)
            }
          }
        }
      } catch {
        case se: SocketTimeoutException =>
          println("Socket timed out!")
        case e: Exception =>
          e.printStackTrace()
      }
    }
  }
  def shutdown(){
    if (server != null && !server.isClosed())
      server.close()
    if (socketServer != null && !socketServer.isClosed())
      socketServer.close()
  }
}