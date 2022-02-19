package transform_sink

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._


object spilt_select {

  case class sensor(id:String,timeStamp : Long,tempture : Double)

  def main(args: Array[String]): Unit = {


    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val inpath = "/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt"

    val inputStream: DataStream[String] = env.readTextFile(inpath)

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fields: Array[String] = data.split(",")

      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })

    val spiltedStream: SplitStream[sensor] = mappedStream.split(data => {

      if (data.tempture > 30.0) Seq("high") else Seq("low")

    })


    val highStream: DataStream[sensor] = spiltedStream.select("high")
    val lowStream: DataStream[sensor] = spiltedStream.select("low")
    //也可以多字段进行select选取
    val allStream: DataStream[sensor] = spiltedStream.select("high", "low")




    val highMappedStream: DataStream[(String, Double)] = highStream.map(data => {
      (data.id, data.tempture)
    })

    val connectedStream: ConnectedStreams[(String, Double), sensor] = highMappedStream.connect(lowStream)

    val connectmappedStream: DataStream[Product] = connectedStream.map(

      warningdata => (warningdata._1, warningdata._2, "warning"),
      lowdata => (lowdata.tempture, "healthy")
    )

    connectmappedStream.print()

    env.execute(this.getClass.getSimpleName)

  }

}
