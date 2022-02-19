package window

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time




object windowTest {

  def main(args: Array[String]): Unit = {

    case class sensor(id:String,timeStamp : Long,tempture : Double)

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val inputStream: DataStream[String] = env.socketTextStream("localhost", 9000)

    val latelag = new OutputTag[sensor]("late")

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fields: Array[String] = data.split(",")

      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })
      .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[sensor]
    (Time.milliseconds(30)) {
      override def extractTimestamp(element: sensor): Long = {

        element.timeStamp * 1000

      }
    })


      val windowStream: DataStream[sensor] = mappedStream
        .keyBy("id")
        .timeWindow(Time.seconds(10))
        .allowedLateness(Time.minutes(1))
        .sideOutputLateData(latelag)
        .reduce((curdata, newdata) => {

          sensor(curdata.id, newdata.timeStamp, curdata.tempture.min(newdata.tempture))

        })


    windowStream.print()
    windowStream.getSideOutput(latelag)


    env.execute(this.getClass.getSimpleName)


  }

}
