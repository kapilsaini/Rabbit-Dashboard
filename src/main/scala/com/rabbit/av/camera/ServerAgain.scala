package com.rabbit.av.camera

import java.io.DataInputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.lang.Long

class ServerAgain {

	println("Creating new ServerAgain")
	val host: String = "10.122.44.31"
	val port: Int = 8080
	val listener = new ServerSocket(port)
	var socket: Socket = null
  var dataBuffer: Array[Byte] = Array()
  
  val start = raw"x"
  val end = raw"y"
	try {
		while(true) {
			socket = listener.accept()
			
			val inStream = new DataInputStream(socket.getInputStream)
  	
			while (true){
			  val imgArray: Array[Byte] = new Array(100)
			  val imgSize = inStream.readFully(imgArray)
			  println(s"imgSize -> ${imgSize}")
			  for ( b <- imgArray){
			    print(b+" ")
			  }
			  println()
			  println(s"${new String(imgArray)}")
				dataBuffer = dataBuffer ++ imgArray
				println(s"data buffer after appending ${dataBuffer.size}")
			  println(s"${dataBuffer.contains(start)} ${dataBuffer.contains(end)}")
				val frameStart = dataBuffer.indexOf(start)
        val frameEnd = dataBuffer.indexOf(end)
        println(s"frameStart->${frameStart} frameEnd->${frameEnd}")
        if (frameStart != -1 && frameStart != -1) {
				  dataBuffer = dataBuffer.slice(frameEnd + 1, dataBuffer.size)
          println(s"data buffer size after slicing ${dataBuffer.size}")
        }
			}
		}
	} finally {
		socket.close()
		listener.close();
	}
	

	//  val Socket clientSocket = serverSocket.accept();
	//  val inStream: DataInputStream = new DataInputStream(socket.getInputStream)
	//  
	//  val dataSize = inStream.readInt()
	//    
	//  val dataArray: Array[Byte] = new Array(dataSize)
	//  
	//  val imgDataSize = inStream.read(dataArray)
	//  
	//  println(s"Recieved data size ${imgDataSize} ${dataSize}")
	//  
	//  } catch {
	//    case e : Exception => e.printStackTrace()
	//  }

}