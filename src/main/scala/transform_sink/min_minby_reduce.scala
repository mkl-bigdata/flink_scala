package transform_sink

import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

case class sensor(id:String,timeStamp : Long,tempture : Double)

object min_minby_reduce {

  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    val inpath = "/Users/mkl/IdeaProjects/flink_scala/data/sensor.txt"

    val dataStream: DataStream[String] = env.readTextFile(inpath)


    val mappedDataStream: DataStream[sensor] = dataStream.map(data => {

      val fields: Array[String] = data.split(",")

      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })
    /**
     * result 结果
    *sensor(sensor_1,1547718199,35.8)
    *sensor(sensor_6,1547718201,15.4)
    *sensor(sensor_7,1547718202,6.7)
    *sensor(sensor_10,1547718205,38.1)
    *sensor(sensor_1,1547718206,32.0)
    *sensor(sensor_1,1547718206,32.0)
    *sensor(sensor_1,1547718210,29.7)
    *sensor(sensor_1,1547718210,29.7)
     */

    val result: DataStream[sensor] = mappedDataStream
      .keyBy("id")
      .minBy("tempture")



    //实现reduce方法，取最小温度，最近时间

    /**
     * reduceResult 结果
      sensor(sensor_1,1547718199,35.8)
      sensor(sensor_6,1547718201,15.4)
      sensor(sensor_7,1547718202,6.7)
      sensor(sensor_10,1547718205,38.1)
      sensor(sensor_1,1547718206,32.0)
      sensor(sensor_1,1547718208,32.0)
      sensor(sensor_1,1547718210,29.7)
      sensor(sensor_1,1547718213,29.7)
     */
    val reduceResult: DataStream[sensor] = mappedDataStream
      .keyBy("id")
      .reduce((curdata, newdata) => {

        sensor(curdata.id, newdata.timeStamp, curdata.tempture.min(newdata.tempture))

      })

   // result.print()

    reduceResult.print()


    env.execute(this.getClass.getSimpleName)


  }

}




//另一种方法，自定义类，实现reducefunction接口，自定义内部的reduce方法
class myReducefunc  extends ReduceFunction[sensor]{
  override def reduce(curdata: sensor, newdata: sensor): sensor = {

    sensor(curdata.id, newdata.timeStamp, curdata.tempture.min(newdata.tempture))

  }

}