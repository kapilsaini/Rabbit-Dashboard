package com.rabbit.av.manager

import java.io.BufferedWriter
import java.io.OutputStreamWriter
import akka.actor.Props
import java.io.FileOutputStream
import com.rabbit.av.util._
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.stream.ActorMaterializer
import com.rabbit.av.util.Terminate
import java.io.Closeable
import com.rabbit.av.ui.UI

class DataDumper extends Actor {
	implicit val materializer = ActorMaterializer()(context.system)
  var csvFileWriter: BufferedWriter  = null
  lazy val config = ConfigFactory.load()
  val imageDir = config.getString("dataDir")
	def receive = {
    case data: TrainingNode =>
	    if(csvFileWriter == null)
	      initalize
      val dumpStr = s"${imageDir}, ${data.imgName},[${data.steeringDirection.mkString(",")}]\n"
      csvFileWriter.write(dumpStr)
      csvFileWriter.flush()
      sender ! None
	  case Terminate =>
	    csvFileWriter.close()
	  case _ =>
	    println("recieved wrong message")
  }    
	
	def initalize() = {
    val dataDir = config.getString("dataDir")
    val csvFile = s"${dataDir}\\DATA_${System.currentTimeMillis()}.csv"
    csvFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile)))
	}
	
  def using[T <: Closeable, R](resource: T)(block: T => R): R = {
    try { block(resource) }
    finally { resource.close() }
  }
}

object DataDumper{
  val props = Props[DataDumper]
}