package window

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
/**
 * 需求 ： 对于温度传感器，当连续温度记录大于10度时，发出报警信息
 *
 * @param:
 * @return:
 * @author: makuolang
 * @date: 2022/2/15
 */

object tempState {

  def main(args: Array[String]): Unit = {



    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val inputStream: DataStream[String] = env.socketTextStream("localhost", 7777)

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fileds: Array[String] = data.split(",")

      sensor(fileds(0), fileds(1).toLong, fileds(2).toDouble)


    })

    val result: DataStream[(String, Double, Double)] = mappedStream
      .keyBy(_.id)
      //.flatMap(new myFlatmapFunc(10.0))
        .flatMapWithState[(String,Double,Double),Double]{

          case (in : sensor,None) => (List.empty,Some(in.tempture))

          case (in : sensor,lastTemp : Some[Double]) => {

            val diff: Double = (in.tempture - lastTemp.get).abs

            if(diff > 10){

              (List((in.id,lastTemp.get,in.tempture)),Some(in.tempture))

            }else{

              (List.empty,Some(in.tempture))

            }
          }

        }


    result.print("tempState")

    env.execute(this.getClass.getSimpleName)

  }


  class myFlatmapFunc(threshold: Double) extends RichFlatMapFunction[sensor,(String,Double,Double)]{

    lazy val tempState = getRuntimeContext
      .getState(new ValueStateDescriptor[Double]("tempAlert",classOf[Double]))


    lazy val flag = getRuntimeContext
      .getState(new ValueStateDescriptor[Boolean]("flag",classOf[Boolean],true))

    override def flatMap(in: sensor, collector: Collector[(String, Double, Double)]): Unit = {

      //得到累加器报警信息
      val temp: Double = tempState.value()

      if(flag.value().equals(true)){

        //得到flag的状态，如果是第一次，则更新，并把flag置为false，否则进行比较

        tempState.update(in.tempture)
        collector.collect(("第一次",0.0,0.0))
        flag.update(false)

      }else{

        if((in.tempture - temp).abs > threshold){

          collector.collect((in.id,temp,in.tempture))

        }

        tempState.update(in.tempture)

      }

    }

  }

}
