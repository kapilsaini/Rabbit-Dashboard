package com.rabbit.av.util

import java.awt.image.BufferedImage

case class TrainingNode(
    image: BufferedImage,
    imgName: String,
    var steeringDirection: List[Char] = List('F'))
case class Initialize()
case class Terminate()