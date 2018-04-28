package com.rabbit.av.transforms

import akka.util.ByteString

object Implicits {
  implicit class ByteStringOps(bytes: ByteString) {
    def sliceChunk(inner: ByteString, outer: ByteString): Option[(ByteString, ByteString)] = {
      val innerIndex = bytes.indexOfSlice(inner)
      val outerIndex = bytes.indexOfSlice(outer)
      println(s"innerIndex = ${innerIndex} outerIndex=${outerIndex}")
      if (innerIndex != -1 && outerIndex != -1) {
        val chunk = bytes.slice(innerIndex, outerIndex + outer.size)
        val remaining = bytes.slice(outerIndex + outer.size, bytes.size)
        Some((chunk, remaining))
      } else None
    }
  }
}