package com.rabbit.av.camera

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.CvSize
import org.bytedeco.javacv.Frame

import com.rabbit.av.util.MediaEditor

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.rabbit.av.util.FrameDimensions

object RemoteWebcam {
    
  implicit val system = ActorSystem
  implicit val timeout = Timeout(5.seconds)
  val beginOfFrame = ByteString(0xff, 0xd8)
  val endOfFrame = ByteString(0xff, 0xd9)
  val config = ConfigFactory.load()
  

  def apply(
    remoteCameraURI: String,
    dimensions: FrameDimensions)
      (implicit system: ActorSystem, mat: Materializer): Future[Source[Frame, Any]] = {
  implicit val ec = system.dispatcher
  val httpRequest = HttpRequest(uri = remoteCameraURI)
  val eventualChunks: Future[Source[ByteString, Any]] = 
    Http()
      .singleRequest(httpRequest)
        .map(_.entity.dataBytes)
        
  eventualChunks.map(
      _.via(new FrameChunker(beginOfFrame, endOfFrame))
       .via(bytesToFrame(dimensions)))
  }

  def bytesToFrame(dimensions: FrameDimensions): Flow[ByteString, Frame, NotUsed] = {
    Flow[ByteString]
      .map(_.toArray)
      .map { bytes =>
        val frameSize = new CvSize(dimensions.width, dimensions.height)
        val image = opencv_core.cvCreateImage(frameSize, opencv_core.CV_8UC3, 3)
        image.imageData(new BytePointer(bytes: _*))
        image
      }
      .map(MediaEditor.iplImageToFrame)
  }
}