package processFunction

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import window.sensor

object outSideStream {


  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val inputStream: DataStream[String] = env.socketTextStream("localhost", 7777)

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fields: Array[String] = data.split(",")

      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })

    val result: DataStream[sensor] = mappedStream
      .process(new myOutPutSide(30.0))

    result.print("main")
    result.getSideOutput(new OutputTag[Double]("output")).print("low")

    env.execute(this.getClass.getSimpleName)

  }

  class myOutPutSide(max: Double) extends ProcessFunction[sensor,sensor]{


    lazy val outputSide = new OutputTag[Double]("output")

    override def processElement(value: sensor, ctx: ProcessFunction[sensor, sensor]#Context, out: Collector[sensor]): Unit = {


      if(value.tempture > max){

        out.collect(value)

      }else{

        ctx.output(outputSide,value.tempture)

      }


    }

  }

}
