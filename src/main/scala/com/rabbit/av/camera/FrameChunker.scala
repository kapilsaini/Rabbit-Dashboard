package com.rabbit.av.camera

import akka.stream.{ Attributes, FlowShape, Inlet, Outlet }
import akka.stream.stage._
import akka.util.ByteString
import com.rabbit.av.transforms.Implicits

class FrameChunker(
    val beginOfFrame: ByteString,
    val endOfFrame: ByteString) 
      extends GraphStage[FlowShape[ByteString, ByteString]] {
  val in = Inlet[ByteString]("Chunker.in")
  val out = Outlet[ByteString]("Chunker.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(
      inheritedAttributes: Attributes): 
    GraphStageLogic = new GraphStageLogic(shape) {
    private var buffer = ByteString.empty

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        println("Pulling chuncks")
        if (isClosed(in)) emitChunk()
        else pull(in)
      }
    })
    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        println("Pushing chuncks")
        val elem = grab(in)
        buffer ++= elem
        emitChunk()
      }

      override def onUpstreamFinish(): Unit = {
        if (buffer.isEmpty) completeStage()
        // elements left in buffer, keep accepting downstream pulls
        // and push from buffer until buffer is emitted
      }
    })

    private def emitChunk(): Unit = {
      println("Emitting chuncks")
      if (buffer.isEmpty) {
        println("Buffer is empty")
        if (isClosed(in)) completeStage()
        else pull(in)
      } else {
        import Implicits._
        buffer.sliceChunk(beginOfFrame, endOfFrame) match {
          case Some((chunk, remaining)) =>
            print(s"${remaining.size} chuncks of ${chunk.size} remaining")
            buffer = remaining
            push(out, chunk)

          case None =>
            pull(in)
        }
      }
    }
  }
}