import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._


object checkPoint {

  def main(args: Array[String]): Unit = {


    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    //开启checkpoint
    env.enableCheckpointing(500)



    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,200))



    val inputStream: DataStream[String] = env.socketTextStream("localhost", 7777)

    inputStream.map(data => {

      data.toInt
    })

        .print()

    env.execute(this.getClass.getSimpleName)

  }

}
