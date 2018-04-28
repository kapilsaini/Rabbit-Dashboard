package com.rabbit.av.manager

import java.awt.image.BufferedImage

import org.bytedeco.javacv.Frame

import com.rabbit.av.util._

import akka.actor.ActorRef

object dashboardManager {
 
  var dataDumperActor: ActorRef = null
  var imageDumperActor: ActorRef = null
  var httpActor: ActorRef = null
  
  var frameCount: Long = 0
  var currentNode: Option[TrainingNode] = None
  
  def setdataActorRef(ref: ActorRef) = {
    dataDumperActor = ref
  }
  
  def setImageActorRef(ref: ActorRef) = {
    imageDumperActor = ref
  }
  
  def setHttpActorRef(ref: ActorRef) = {
    httpActor = ref
  }
  
  def setCurrentNode(image: BufferedImage): BufferedImage = {
    currentNode = Some(TrainingNode(image, getImageName()))
    image
  }
 
  def getCurrentFrameCount(): Long = {
    frameCount
  }
	
	def extractFrameCount(frame: Frame): Frame = {
    frameCount += 1
    frame
  }
	
	def setSteeringDirection(directions: List[Char]){
	  if (currentNode.isDefined) {
	     currentNode.get.steeringDirection = directions
	     dumpNode(currentNode.get)
	  }
	}
 
  def getImageName() = {
    s"IMG_${System.currentTimeMillis()}.png"
  }
  
  def dumpNode(currentNode: TrainingNode) = {
    val controlMoves = ControlMoves(moves = currentNode.steeringDirection)
     httpActor ! controlMoves
     dataDumperActor ! currentNode
     imageDumperActor ! currentNode
  }
  
  def dumpImage(image: BufferedImage) = {
    if (currentNode.isDefined) {
      try {
        dataDumperActor ! currentNode.get
      } catch {
        case e: Exception =>
          println(s"Error transmitting message ${e.getMessage}")
          e.printStackTrace()
      }
    } else {
      println("Current node is not defined")
    }
    
  }
}