  package com.rabbit.av.camera

import akka.actor.{ DeadLetterSuppression, Props, ActorSystem, ActorLogging }
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import akka.stream.scaladsl.Source
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacv.{ FrameGrabber, Frame }
import org.bytedeco.javacv.FrameGrabber.ImageMode


private class WebcamFramePublisher(
  deviceId: Int,
  imageWidth: Int,
  imageHeight: Int,
  bitsPerPixel: Int,
  imageMode: ImageMode) extends ActorPublisher[Frame] {

  private implicit val ec = context.dispatcher

  private val grabber = buildGrabber(
    deviceId = deviceId,
    imageWidth = imageWidth,
    imageHeight = imageHeight,
    bitsPerPixel = bitsPerPixel,
    imageMode = imageMode
  )

  private def buildGrabber(
    deviceId: Int,
    imageWidth: Int,
    imageHeight: Int,
    bitsPerPixel: Int,
    imageMode: ImageMode): FrameGrabber = synchronized {
      println("Creating frameGrabber")
      val g = FrameGrabber.createDefault(deviceId)
      println("Frame grabber created")
      println()
      g.setImageWidth(imageWidth)
      g.setImageHeight(imageHeight)
      g.setBitsPerPixel(bitsPerPixel)
      g.setImageMode(imageMode)
      g.start()
      g
  }
  def receive: Receive = {
    case _: Request => emitFrames()
    case Continue => emitFrames()
    case Cancel => onCompleteThenStop()
  }

  private def emitFrames(): Unit = {
    if (isActive && totalDemand > 0) {
      grabFrame().foreach(onNext)
      if (totalDemand > 0) {
        self ! Continue
      }
    }
  }

  private def grabFrame(): Option[Frame] = {
    Option(grabber.grab())
  }
}

private case object Continue extends DeadLetterSuppression