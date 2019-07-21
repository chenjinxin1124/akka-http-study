package http.scaladsl

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.HttpApp
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol.{jsonFormat1, jsonFormat2}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MessageEntity, StatusCodes}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
// for JSON serialization/deserialization following dependency is required:
// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import scala.concurrent.Future
import scala.util.Random._

object MinimalHttpServer extends SprayJsonSupport with DefaultJsonProtocol{

  case class Item2(
                   id: Int,
                   quantity: Int,
                   unitPrice: Double,
                   percentageDiscount: Option[Double]
                 )

  case class Order2(
                    id: String,
                    timestamp: Long,
                    items: List[Item2],
                    deliveryPrice: Double,
                    metadata: Map[String, String]
                  )

  case class GrandTotal(id: String, amount: Double)

  trait OrderJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val itemFormat2: RootJsonFormat[Item2] = jsonFormat4(Item2)
    implicit val orderFormat2: RootJsonFormat[Order2] = jsonFormat5(Order2)
    implicit val grandTotalFormat = jsonFormat2(GrandTotal)
  }
  implicit val itemFormat2: RootJsonFormat[Item2] = jsonFormat4(Item2)
  implicit val orderFormat2: RootJsonFormat[Order2] = jsonFormat5(Order2)
  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher

  var orders: List[Item] = Nil

  // domain model
  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])

  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Item]] = Future {
    orders.find(o => o.id == itemId)
  }

  def saveOrder(order: Order): Future[Done] = {
    orders = order match {
      case Order(items) => items ::: orders
      case _ => orders
    }
    Future {
      Done
    }
  }

  def main(args: Array[String]) {
    def route =
      pathPrefix("v1") {
        path("id" / Segment) { id =>
          get {
            println("server get " + id)
            complete(s"got get request")
          } ~
            post {
              entity(as[String]) { entity =>
                println("server get " + entity)
                complete(s"got post request")
              }
            }
        }
        path("json") {
          get {
            val or: Order2 =genRandomOrder()

            complete(or) // will render as JSON
          }
        }
      }

    def genRandomOrder() = {
      val items = (0 to nextInt(5)).map(i => {
        Item2(i, nextInt(100), 100 * nextDouble(),
          if (nextBoolean()) Some(nextDouble()) else None)
      }).toList
      Order2(nextString(4), System.currentTimeMillis(),items, 100 * nextDouble(), Map("notes" -> "random"))
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8099)
  }
}