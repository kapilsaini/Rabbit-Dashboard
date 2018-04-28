package com.rabbit.av.ui

import scala.swing.MainFrame
import java.awt.image.BufferedImage
import akka.http.scaladsl.model.headers.LinkParams.title
import com.rabbit.av.manager.dashboardManager
import scala.swing.Label
import scala.swing.Component
import scala.swing.Swing
import java.awt.Dimension
import scala.swing.BoxPanel
import java.awt.Color
import scala.swing.Orientation
import com.rabbit.av.dashboardapp.Application
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.awt.Font
import scala.swing.Alignment

class UI extends MainFrame {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val canvas = new Canvas()
  val vFrame = new ViewFrame()
  
  import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
  peer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
  
  println("Creating new UI")
  
  override def closeOperation() {
    println("Custom close operation")
    Application.shutdownApplication()
    super.closeOperation()
    sys.exit()
  } 
  title = "Rabbit Dashboard"    
  val steerLabel = new Label("")
  steerLabel.foreground = Color.GRAY
  steerLabel.font = new Font("Courier New", Font.BOLD, 12)
  steerLabel.xAlignment = Alignment.Center
  steerLabel.horizontalTextPosition = Alignment.Center
  resizable = false
  
  contents = new BoxPanel(Orientation.Vertical) {
    contents += vFrame
    contents += steerLabel
    contents += canvas
  }
  listenTo(canvas)
  reactions += {
    case SteeringEvent(keys) => 
      steer(keys)
  }

  def updateDisplay() {
    canvas.repaint()    
  }

  def steer(directions: List[Char]) {
    steerLabel.text = s"Moving ${directions}"
    dashboardManager.setSteeringDirection(directions)
    updateDisplay() 
  }
  
  def displayVideoFrame(buffer: BufferedImage): BufferedImage = {
    println("Displaying frame")
    vFrame.displayBufferedImage(buffer)
    buffer
  }
  
  def getFrame() = {
    vFrame
  }
}