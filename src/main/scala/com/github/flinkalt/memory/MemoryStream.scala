package com.github.flinkalt.memory

import cats.data.State
import cats.instances.vector._
import cats.syntax.traverse._
import com.github.flinkalt._
import com.github.flinkalt.time.Instant

case class MemoryStream[+T](elems: Vector[DataOrWatermark[T]]) {
  def toData: Vector[DataAndWatermark[T]] = {
    val convertToData: DataOrWatermark[T] => State[Instant, Vector[DataAndWatermark[T]]] = {
      case JustData(time, value) => State(watermark => (watermark, Vector(DataAndWatermark(time, watermark, value))))
      case JustWatermark(watermark) => State(_ => (watermark, Vector.empty))
    }

    elems.flatTraverse(convertToData).runA(Instant.minValue).value
  }
}

object MemoryStream {
  implicit def memoryDStream: DStream[MemoryStream] = MemoryDStream
  implicit def memoryStateful: Stateful[MemoryStream] = MemoryStateful
  implicit def memoryWindowed: Windowed[MemoryStream] = MemoryWindowed

  def fromData[T](inputData: Vector[DataAndWatermark[T]]): MemoryStream[T] = {
    val elems = inputData.flatMap {
      case DataAndWatermark(time, watermark, value) => List(JustData(time, value), JustWatermark(watermark))
    }

    MemoryStream(elems)
  }
}
