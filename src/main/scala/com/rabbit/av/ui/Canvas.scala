package com.rabbit.av.ui

import java.awt.Dimension
import scala.collection.mutable.ListBuffer
import scala.swing.Component
import scala.swing.event.KeyPressed
import scala.swing.event.KeyReleased
import com.typesafe.config.ConfigFactory

class Canvas() extends Component {
  lazy val config = ConfigFactory.load()
  val width = config.getInt("imageWidth")
  preferredSize = new Dimension(width, 10)
  focusable = true
  enabled = true
  listenTo(keys)
  var pressedKeys = new ListBuffer[Char]()
  reactions += {
    case KeyPressed(_,k,_,_) => {
      addToPressedKeys(getDirection(k.toString()))
      publish(SteeringEvent(pressedKeys.toList))
    }
    case KeyReleased(_,k,_,_) => {
      removeFromPressedKeys(getDirection(k.toString()))
    }
  }
  
  def addToPressedKeys(direction: Char) = {
    if (!pressedKeys.contains(direction)){
      if (direction == 'L' && pressedKeys.contains('R'))
        removeFromPressedKeys('R')
      else if (direction == 'R' && pressedKeys.contains('L'))
        removeFromPressedKeys('L')
      else if (direction == 'U' && pressedKeys.contains('D'))
        removeFromPressedKeys('D')
      else if (direction == 'D' && pressedKeys.contains('U'))
        removeFromPressedKeys('U')
      pressedKeys += direction
    }
  }
  
  def removeFromPressedKeys(direction: Char) = {
    if (pressedKeys.contains(direction)){
      pressedKeys -= direction
    }
  }
  
  def getDirection(key: String): Char = {
    key match {
      case "Left" => 'L'
      case "Right" => 'R'
      case "Down" => 'D'
      case _ => 'U'
    }
  }
}