package com.rabbit.av.manager

import java.io.File

import com.rabbit.av.util.TrainingNode
import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.stream.ActorMaterializer
import javax.imageio.ImageIO
import akka.actor.Props


class ImageDumper extends Actor {
  implicit val materializer = ActorMaterializer()(context.system)
  val config = ConfigFactory.load()
  val imageDir = config.getString("dataDir")
	def receive = {
    case data: TrainingNode =>
      val absFilePath = s"${imageDir}\\${data.imgName}"
      ImageIO.write(data.image, "png", new File(absFilePath))
  }    
}

object ImageDumper{
  val props = Props[ImageDumper]
}