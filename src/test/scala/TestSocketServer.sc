import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Tcp
import akka.stream.scaladsl.Tcp.IncomingConnection
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.util.ByteString

object TestSocketServer {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  implicit val system = ActorSystem()             //> 13:05:00.262 [default-akka.actor.default-dispatcher-2] INFO  akka.event.slf4
                                                  //| j.Slf4jLogger - Slf4jLogger started
                                                  //| system  : akka.actor.ActorSystem = akka://default
  implicit val materializer = ActorMaterializer() //> materializer  : akka.stream.ActorMaterializer = ActorMaterializerImpl(akka:/
                                                  //| /default,ActorMaterializerSettings(4,16,,<function1>,StreamSubscriptionTimeo
                                                  //| utSettings(CancelTermination,5000 milliseconds),false,1000,1000,false,true),
                                                  //| akka.dispatch.Dispatchers@36b4fe2a,Actor[akka://default/user/StreamSuperviso
                                                  //| r-0#-1923314067],false,akka.stream.impl.SeqActorNameImpl@28975c28)
  implicit val executionContext = system.dispatcher
                                                  //> executionContext  : scala.concurrent.ExecutionContextExecutor = Dispatcher[a
                                                  //| kka.actor.default-dispatcher]
  
  val connection = Tcp().outgoingConnection("127.0.0.1", 8080)
                                                  //> connection  : akka.stream.scaladsl.Flow[akka.util.ByteString,akka.util.ByteS
                                                  //| tring,scala.concurrent.Future[akka.stream.scaladsl.Tcp.OutgoingConnection]] 
                                                  //| = Flow(FlowShape(IncomingTCP.in,Detacher.out), CompositeModule [50b5ac82]
                                                  //|   Name: unnamed
                                                  //|   Modules:
                                                  //|     (OutgoingConnection) GraphStage(TCP-to(127.0.0.1:8080)) [3fb1549b]
                                                  //|     (unnamed) [2b48a640] copy of GraphStage(Detacher) [1e683a3e]
                                                  //|   Downstreams: 
                                                  //|     IncomingTCP.out -> Detacher.in
                                                  //|   Upstreams: 
                                                  //|     Detacher.in -> IncomingTCP.out
                                                  //|   MatValue: Atomic(OutgoingConnection[3fb1549b]))

	val replParser =
	  Flow[String].takeWhile(_ != "q")
	    .concat(Source.single("BYE"))
	    .map(elem => ByteString(s"$elem\n"))  //> replParser  : akka.stream.scaladsl.Flow[String,akka.util.ByteString,akka.Not
                                                  //| Used] = Flow(FlowShape(TakeWhile.in,Map.out), CompositeModule [434a63ab]
                                                  //|   Name: unnamed
                                                  //|   Modules:
                                                  //|     (takeWhile) GraphStage(TakeWhile) [6e0f5f7f]
                                                  //|     (unnamed) [2805d709] copy of CompositeModule [3ee37e5a]
                                                  //|       Name: unnamed
                                                  //|       Modules:
                                                  //|         (unnamed) [2ea41516] copy of GraphStage(SingleSource(BYE)) [3a44431a
                                                  //| ]
                                                  //|         (unnamed) [3c7f66c4] copy of CompositeModule [194bcebf]
                                                  //|           Name: unnamed
                                                  //|           Modules:
                                                  //|             (unnamed) [17497425] copy of GraphStage(Concat(2)) [0f0da945]
                                                  //|             (unnamed) [4803b726] copy of GraphStage(Detacher) [1e683a3e]
                                                  //|             (unnamed) [0ffaa6af] copy of GraphStage(Detacher) [1e683a3e]
                                                  //|           Downstreams: 
                                                  //|             Detacher.out -> Concat.in0
                                                  //|             Detacher.out -> Concat.in1
                                                  //|           Upstreams: 
                                                  //|             Concat.in0 -> Detacher.out
                                                  //|             Concat.in1
                                                  //| Output exceeds cutoff limit.
	
	val repl = Flow[ByteString]
	  .via(Framing.delimiter(
	    ByteString("\n"),
	    maximumFrameLength = 256,
	    allowTruncation = true))
	  .map(_.utf8String)
	  .map(text => println("Server: " + text))
	  .map(_ => readLine("> "))
	  .via(replParser)                        //> repl  : akka.stream.scaladsl.Flow[akka.util.ByteString,akka.util.ByteString
                                                  //| ,akka.NotUsed] = Flow(FlowShape(DelimiterFramingStage.in,Map.out), Composit
                                                  //| eModule [4c1909a3]
                                                  //|   Name: unnamed
                                                  //|   Modules:
                                                  //|     (unnamed) [4659191b] copy of GraphStage(Map(<function1>)) [59505b48]
                                                  //|     (delimiterFraming) CompositeModule [47d9a273]
                                                  //|       Name: delimiterFraming
                                                  //|       Modules:
                                                  //|         (delimiterFraming) GraphStage(DelimiterFraming) [4efac082]
                                                  //|       Downstreams: 
                                                  //|       Upstreams: 
                                                  //|       MatValue: Ignore
                                                  //|     (unnamed) [4b0d79fc] copy of CompositeModule [434a63ab]
                                                  //|       Name: unnamed
                                                  //|       Modules:
                                                  //|         (takeWhile) GraphStage(TakeWhile) [6e0f5f7f]
                                                  //|         (unnamed) [2805d709] copy of CompositeModule [3ee37e5a]
                                                  //|           Name: unnamed
                                                  //|           Modules:
                                                  //|             (unnamed) [2ea41516] copy of GraphStage(SingleSource(BYE)) [3a4
                                                  //| 4431a]
                                                  //|             (unnamed) [3c7f66c4] copy of CompositeModu
                                                  //| Output exceeds cutoff limit.
	  
  connection.join(repl).run()                     //> res0: scala.concurrent.Future[akka.stream.scaladsl.Tcp.OutgoingConnection] 
                                                  //| = List()
}
