package com.github.flinkalt.typeinfo

import java.io.{DataInput, DataOutput}

import com.github.flinkalt.typeinfo.serializer.{DeserializationState, RefSerializer, SerializationState}

import scala.collection.immutable.Seq
import scala.reflect.ClassTag

trait TypeInfo5_Injections extends TypeInfo6_Generic {
  implicit def injectionTypeInfo[T: ClassTag, U](implicit inj: Injection[T, U], typeInfo: TypeInfo[U]): TypeInfo[T] = new SerializerBasedTypeInfo[T] with RefSerializer[T] {
    override val nestedTypeInfos: Seq[TypeInfo[_]] = Seq(typeInfo)

    override def serializeNewValue(value: T, dataOutput: DataOutput, state: SerializationState): Unit = {
      typeInfo.serialize(inj(value), dataOutput, state)
    }

    override def deserializeNewValue(dataInput: DataInput, state: DeserializationState): T = {
      val u = typeInfo.deserialize(dataInput, state)
      inj.invert(u)
    }
  }
}
