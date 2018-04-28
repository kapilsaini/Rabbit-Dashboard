package com.rabbit.av.ui

import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

import scala.swing.Panel
import scala.swing.event.KeyPressed
import scala.swing.event.KeyReleased
import scala.swing.MainFrame
import com.typesafe.config.ConfigFactory

class ViewFrame() extends Panel {
  
  private var bufferedImage: BufferedImage = null
  val config = ConfigFactory.load()
  val width = config.getInt("imageWidth")
  val height = config.getInt("imageHeight")
  
  val affineTransformation = new AffineTransform(1f,0f,0f,1f,10, 10)
  preferredSize = new Dimension(width, height)
  override def paint(g: Graphics2D) {
    var x: Boolean = false
    g.drawImage(bufferedImage, affineTransformation , null)
    g.dispose()
  }
  
  def displayBufferedImage(buffer: BufferedImage){
    bufferedImage = buffer
    this.repaint()
  }
}