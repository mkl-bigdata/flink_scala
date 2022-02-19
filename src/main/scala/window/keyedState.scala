package window


import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._


object keyedState {

  case class sensor(id:String,timestamp : Long,tempture:Double)

  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val inputStream: DataStream[String] = env.socketTextStream("localhost", 7777)

    val mappedStream: DataStream[sensor] = inputStream.map(data => {

      val fields: Array[String] = data.split(",")

      sensor(fields(0), fields(1).toLong, fields(2).toDouble)

    })




  }

  class MyKeyed extends RichMapFunction[sensor,String]{

    var valueState : ValueState[Double] = _

    //新建liststate
    lazy val listState : ListState[Double] = getRuntimeContext
      .getListState(new ListStateDescriptor[Double]("liststate",classOf[Double]))
    //新建mapstate
    lazy val mapState : MapState[String,Double] = getRuntimeContext
      .getMapState(new MapStateDescriptor[String,Double]("mapstate",classOf[String],classOf[Double]))


    override def open(parameters: Configuration): Unit = {

      valueState = getRuntimeContext
        .getState(new ValueStateDescriptor[Double]("lastTemp",classOf[Double]))

    }

    override def map(in: sensor): String = {

      //获得状态
      val v: Double = valueState.value()

      //更新状态
      valueState.update(in.tempture)

      v.toString
    }


  }

}


