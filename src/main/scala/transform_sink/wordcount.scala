package transform_sink

import org.apache.flink.streaming.api.scala._
import window.sensor

object wordcount {

  def main(args: Array[String]): Unit = {


    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val inputStream: DataStream[String] = env.socketTextStream("localhost", 5678)

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fields: Array[String] = data.split(",")

      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })


    mappedStream.print()

    env.execute()

  }




}
