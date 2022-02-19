package transform_sink

import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._


object fileSink {

  def main(args: Array[String]): Unit = {


    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    val inpath = "/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt"

    val inputStream: DataStream[String] = env.readTextFile(inpath)

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fields: Array[String] = data.split(",")
      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })

    val outPath = "/Users/mkl/IdeaProjects/flink_scala/data/out1.txt"

    mappedStream.addSink(StreamingFileSink.forRowFormat(

      new Path(outPath),
      new SimpleStringEncoder[sensor]()

    ).build())


    env.execute(this.getClass.getSimpleName)

  }

}
