package com.rabbit.av.camera

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import com.rabbit.av.util.FrameDimensions
import org.bytedeco.javacpp.opencv_core.CV_8U
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.FrameGrabber.ImageMode
import scala.concurrent.duration._
import akka.stream.DelayOverflowStrategy

object Webcam {
  def source(
    deviceId: Int,
    dimensions: FrameDimensions,
    bitsPerPixel: Int = CV_8U,
    imageMode: ImageMode = ImageMode.COLOR
    )(implicit system: ActorSystem): Source[Frame, NotUsed] = {
    val props = Props(
      new WebcamFramePublisher(
        deviceId = deviceId,
        imageWidth = dimensions.width,
        imageHeight = dimensions.height,
        bitsPerPixel = bitsPerPixel,
        imageMode = imageMode
      )
    )
    val webcamActorRef = system.actorOf(props)
    val webcamActorPublisher = ActorPublisher[Frame](webcamActorRef)

    Source.fromPublisher(webcamActorPublisher)
  }
}

