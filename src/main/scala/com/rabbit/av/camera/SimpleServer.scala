package com.rabbit.av.camera





import scala.concurrent.Future

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_core.CvSize

import com.rabbit.av.manager.dashboardManager
import com.rabbit.av.ui.UI
import com.rabbit.av.util.MediaEditor
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Tcp
import akka.stream.scaladsl.Tcp.IncomingConnection
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.util.ByteString
import org.bytedeco.javacpp.opencv_core.Mat
import java.awt.image.DataBufferByte
import java.nio.ByteBuffer
import org.bytedeco.javacv.CanvasFrame
import java.io.File
import java.awt.image.BufferedImage
import javax.imageio.stream.ImageInputStream
import javax.imageio.ImageReader
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import javax.imageio.ImageReadParam
import java.awt.Image
import java.awt.Graphics2D


class SimpleServer() {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  lazy val config = ConfigFactory.load()
  val beginOfFrame = ByteString(0xff, 0xd8)
  val endOfFrame = ByteString(0xff, 0xd9)
 val width = config.getInt("imageWidth")
  val height = config.getInt("imageHeight")
  
  println("Creating new instance of Simple server")
  val host: String = "10.122.44.31"
  val port: Int = 8080
  val imageLength: Int = 320 * 240 * 3  
  
  var byteBuffer: ByteString = ByteString()
  
//  val canvas = new CanvasFrame("Webcam")

  
  val connections: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind(host, port)
    
  connections runForeach { connection ⇒
    println(s"New connection from: ${connection.remoteAddress}")
      
//    val echo = Flow[ByteString]
//      .via(Framing.delimiter(
//        ByteString("\n"),
//        maximumFrameLength = 256,
//        allowTruncation = true))
//      .map(_.utf8String)
//      .map(_ + "!!!\n")
//      .map(ByteString(_))
//  
      
   val appFlow = Flow[ByteString]
      //.via(Framing.delimiter(endOfFrame, maximumFrameLength = 1024))
      //.via(new FrameChunker(beginOfFrame, endOfFrame))
      //.viaMat(KillSwitches.single)(Keep.right)
      .map(processInput)
//      .map(MediaEditor.frameToBuffered)
//      .map(dashboardManager.setCurrentNode) 
//      .map(top.displayVideoFrame)
      .map((x:Any) => "")
      .map(ByteString(_))
      
    connection.handleWith(appFlow)
    
  }
  
   def processInput(bytes: ByteString) = {
    println(s"Got bytes of size ${bytes.size}")
    
//    if (bytes.size == 4) {
//      println(s"${bytes} ${bytes.toString()} ${bytes.utf8String}")
////      val bb = ByteBuffer.wrap(bytes.toArray).getLong
//      try {
//      val bb2 = bytes.iterator.getLong(ByteOrder.LITTLE_ENDIAN)
//      println(s"Byte as long ${bb2}")
//      } catch {
//        case e: Exception => println(s"Error ${e.getMessage}")
//      }
//      
//    }
    
    println(s"bytes buffer size before addition ${byteBuffer.size}")
    byteBuffer = byteBuffer ++ bytes
    println(s"updated bytes buffer size after addition ${byteBuffer.size}")
    val beginOfFrame = ByteString(0xff, 0xd8) //
    val endOfFrame = ByteString(0xff, 0xd9)//
    println(s"beginOfFrame ${beginOfFrame.utf8String} ${endOfFrame.utf8String}")

    val innerIndex = byteBuffer.indexOfSlice(beginOfFrame)
    val outerIndex = byteBuffer.indexOfSlice(endOfFrame)
    
    println(s"innerIndex = ${innerIndex} outerIndex=${outerIndex}")
    var chunk: Option[ByteString] = None
    if (innerIndex != -1 && outerIndex != -1) {
      println(s"updated bytes buffer size ${byteBuffer.size}")
      chunk = Some(bytes.slice(innerIndex , outerIndex + endOfFrame.size))
      println(s"chunk length ${chunk.get.size}")
      byteBuffer =  byteBuffer.slice(outerIndex + endOfFrame.size, byteBuffer.size)
      println(s"Buffer size after dropping ${byteBuffer.size}")
      
//      val tm = new Mat(chunk.get.toArray, true)
      
//      val bp : BytePointer = new BytePointer(chunk.get.getByteBuffers())
//      val m: Mat = new Mat(
//          height, 
//          width,
//          opencv_core.CV_8UC3,
//          bp)
//      
//      println(s"Mat Image  ${m.cvSize().height()} ${m.cvSize().width()} ${m.channels()}")
//      println(m)
//      val image: IplImage = new IplImage(m);
//      
//      val directory : File = new File("D:\\Autonomous-prj\\temp")
//      cvSave(new File(directory.getAbsolutePath,"newfile.png").getAbsolutePath, image)
//    
////     val frameSize = new CvSize(width, height)
////      val image: IplImage = opencv_core.cvCreateImage(frameSize, opencv_core.CV_8UC3, 3)
////      image.imageData(new BytePointer(chunk.get.toArray: _*))
//      
//      println(s"image  ${image.cvSize().height()} ${image.cvSize().width()} ${image.depth()}")
//      println(s"ipl Iamge size ${image.sizeof()}")
//     
//      println(s"Converting image from ipl to frame ${image}")
      
//      val frame = MediaEditor.iplImageToFrame(image)
//      val frame = MediaEditor.matToFrame(m)
//      println(s"frame : ${frame.imageWidth} ${frame.imageHeight} ${frame.imageChannels}")
      
      
      println("Displaying image")
//      canvas.showImage(frame)
     getBufferedIamge(chunk.get.toArray)
//      val buffImg = MediaEditor.frameToBuffered(frame)
//      println(s"buffImg : ${buffImg.getHeight}X${buffImg.getWidth}X${buffImg.getType}")
//      
//      println("setting current node")
//      
//      dashboardManager.setCurrentNode(buffImg)
//      println("pusing to display")
     
    } else {
      println("No Chunk")
    }
  }
   
   
   def getBufferedIamge(bytes: Array[Byte]) = {
       try {
        println("Converting byes to bufferedIamge: Start")
        val bis: ByteArrayInputStream  = new ByteArrayInputStream(bytes);
        val readers = ImageIO.getImageReadersByFormatName("jpg")
        println("1")
        val reader: ImageReader = readers.next()
        val iis:ImageInputStream = ImageIO.createImageInputStream(bis)
        reader.setInput(iis, true)
        val param: ImageReadParam = reader.getDefaultReadParam()
        println(s"2  ${param}")
        val image: Image = reader.read(0, param)
        println(s"3 ${image} ${image.getWidth(null)} ${image.getHeight(null)}")
        val bufferedImage: BufferedImage = 
          new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB)
        println("Converting byes to bufferedIamge: End")
        bis.close()
        
        val g2: Graphics2D = bufferedImage.createGraphics()
        g2.drawImage(bufferedImage, null, null)
        println(s"4 ${bufferedImage.getColorModel}")
        
//        UI.displayVideoFrame(bufferedImage)
       } catch {
         case e: Exception => println(s"Error converting to buffered Image ${e.getMessage()}")
         
       }
       
   }
}