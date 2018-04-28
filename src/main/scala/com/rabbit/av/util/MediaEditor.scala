package com.rabbit.av.util

import java.awt.image.BufferedImage
import java.util.function.Supplier

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.{OpenCVFrameConverter, Java2DFrameConverter}

import org.bytedeco.javacpp.opencv_core.{flip,IplImage, Mat}
import org.bytedeco.javacv.CanvasFrame

object MediaEditor {

	val frameToBufferedConverter = ThreadLocal.withInitial(new Supplier[Java2DFrameConverter] {
			def get(): Java2DFrameConverter = new Java2DFrameConverter
	})
		
  val frametoMatConverter = ThreadLocal.withInitial(new Supplier[OpenCVFrameConverter.ToMat] {
			def get(): OpenCVFrameConverter.ToMat  = new OpenCVFrameConverter.ToMat()
	})
	
	private val bytesToMatConverter = ThreadLocal.withInitial(new Supplier[OpenCVFrameConverter.ToIplImage] { 
    def get(): OpenCVFrameConverter.ToIplImage = new OpenCVFrameConverter.ToIplImage 
  }) 

	
  def frameToBuffered(frame: Frame): BufferedImage = {
	  println("Converting from frame to buffered")
	  val converted = new Java2DFrameConverter()
	  if (converted == null) {
	    println("converted is null")
	  } else if (frame.image == null) {
	    println("converted : Frame is null")
	  } else {
	    println("All ok")
	  }
	  try {
	    converted.convert(frame)
	  } catch {
	    case e: Exception =>  println(s"frameToBuffered exc ${e.getMessage}")
	    null
	  }
  }
	
	def matToFrame(mat: Mat): Frame = {
	  println("Converting from Mat to buffered")
	  try {
      frametoMatConverter.get().convert(mat)
	  } catch {
	    case e: Exception =>  println(s"matToFrame exc ${e.getMessage}")
	    null
	  }
  }
	
	
	def frameToMat(frame: Frame): Mat = {
	  try {
	  frametoMatConverter.get.convert(frame)
	  } catch {
	    case e: Exception =>  println(s"frametoMatConverter exc ${e.getMessage}")
	    null
	  }
	}
	
	def flipFrame(frame: Frame): Mat = {
	  val mat = frameToMat(frame)
    flip(mat, mat, 1)
    mat
  }
	
	def iplImageToFrame(image: IplImage): Frame = {
	  try {
	  bytesToMatConverter.get().convert(image)
	  } catch {
	    case e: Exception => println(s"Error in conversion ${e.getMessage}")
	    null
	  }
	}
}