package processFunction

import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import window.sensor

object keyedProcessFunction {

  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val inputStream: DataStream[String] = env.socketTextStream("localhost", 5678)

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fields: Array[String] = data.split(",")

      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })

    val result: DataStream[String] = mappedStream
      .keyBy(_.id)
      .process(new myKeyedProcessFunc(10000L))


    result.print("keyedProcessFunction")

    env.execute(this.getClass.getSimpleName)

  }

  class myKeyedProcessFunc(interval:Long) extends KeyedProcessFunction[String,sensor,String]{


    //上一次的温度值
    lazy val tempState: ValueState[Double] = getRuntimeContext
      .getState(new ValueStateDescriptor[Double]("last_temp", classOf[Double]))

    // 保存定时器的值
    lazy val timerState: ValueState[Long] = getRuntimeContext
      .getState(new ValueStateDescriptor[Long]("timer", classOf[Long]))


    override def processElement(value: sensor
                                , ctx: KeyedProcessFunction[String, sensor, String]#Context
                                , out: Collector[String]): Unit = {

        // 取出温度值和定时器的值，并更新
      val temp: Double = tempState.value()

      tempState.update(value.tempture)

      val timer: Long = timerState.value()

      if(temp <  value.tempture && timer == 0 ){

        //取当前时间
        val newTimer: Long = ctx.timerService().currentProcessingTime() + interval
        //注册定时器
        ctx.timerService().registerProcessingTimeTimer(newTimer)
        //添加定时器到状态中
        timerState.update(newTimer)

      }else if(value.tempture < temp){

        ctx.timerService().deleteProcessingTimeTimer(timer)

        timerState.clear()

      }


    }

    override def onTimer(timestamp: Long
                         , ctx: KeyedProcessFunction[String, sensor, String]#OnTimerContext
                         , out: Collector[String]): Unit ={

      out.collect("传感器" + ctx.getCurrentKey + "的温度连续" + interval/1000 + "秒连续上升")

      timerState.clear()


    }

  }

}
