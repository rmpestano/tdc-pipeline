
package com.github.rmpestano.tdc.cars.perf

import java.util.UUID

import com.google.gson.{JsonPrimitive, JsonObject}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

class CarRestSimulation extends Simulation {

  val printSession =
    exec(session => {
      println(session)
      session
    })

  var totalUsersPerScenario = 10
  var initialUsersPerScenario = 1
  var scenarioDurationInSeconds = 10 //2 users per second
  var expectedMaxResponseTime = 850
  var expected95thPercentileResponseTime=500
  var expected99thPercentileResponseTime=1200
  var expectedPercentileResponseTime=120
  var expectedMeanResponseTime = 80
  var expectedRequestPerSecond = 18

  var baseUrl = System.getProperty("APP_CONTEXT")

   if (baseUrl == null) {
       baseUrl = "http://localhost:8080/tdc-pipeline/";
    }


  val httpProtocol = http
    .baseURL(baseUrl)
    .acceptHeader("application/json;charset=utf-8")
    /*.connection( """keep-alive""")*/
    .contentTypeHeader("application/json; charset=UTF-8")

  val rnd = scala.util.Random

  val carIdsCheck = jsonPath("$..id").findAll.saveAs("carIds") //used by delete scenario

  val eTagCheck = header("ETag").saveAs("eTag")

  val countCheck = jsonPath("$..*").ofType[Int].saveAs("count")

  val idsFeed = csv("car-ids.csv").circular


  val countCarsRequest = {
    http("count cars") //<1> //stores the request in a local variable
    .get("rest/cars/count")
    .check(status.is(200), countCheck)
  }


  val listCarsRequest = http("list cars") //<1> //stores the request in a local variable
    .get("rest/cars/")
    .queryParam("start","${page}")//random page was saved in count request
    .queryParam("max",10)
    .check(status.is(200),carIdsCheck) //<2> request assertion

  val findCarRequest = http("find car")
    .get("rest/cars/${carIds.random()}")
    .check(status.in(200,404))// 404 = car deleted by concurrent user


  val addCarRequest = http("add car")
    .post("rest/cars/")
    .body(ElFileBody("cars.json")).asJSON
    .check(status.is(201))

  val deleteCarRequest = http("remove car")
    .delete("rest/cars/${carIds.random()}")
    .header("user","admin")
    .check(status.in(204,404)) //404 - a concurrent user deleted before

  val saveCount = exec(session => {
    val count = session("count").as[Int]
    session.set("page", new java.util.Random().nextInt(count)-1)//save a random db page based on the number of cars
  })

  val listCarsScenario = scenario("List cars scenario")
    .exec(countCarsRequest)
    .pause(80 milliseconds)
    .exec(saveCount)
    .pause(80 milliseconds)
    .exec(listCarsRequest)
    //.exec(printSession)
    .pause(50 milliseconds)// users don't click buttons at the same time

  val findCarsScenario = scenario("Find cars scenario")
    //.feed(carIds) will find always the same cars
    .exec(countCarsRequest)
    .pause(80 milliseconds)
    .exec(saveCount)
    .pause(80 milliseconds)
    .exec(listCarsRequest) //saves a list of random ids in carIds session variable
    .pause(80 milliseconds)
    .exec(findCarRequest)
    .pause(50 milliseconds)

  val addCarsScenario = scenario("Add cars scenario")
    .exec(session =>
    session.set("userName",UUID.randomUUID().toString)
    )
    .exec(addCarRequest)
    .pause(50 milliseconds)

  val deleteCarsScenario = scenario("Delete cars scenario")
    .exec(countCarsRequest)
    .pause(80 milliseconds)
    .exec(saveCount)
    .pause(80 milliseconds)
    .exec(listCarsRequest) //save first car id in gatling session (tá salvando o mesmo id 25 vezes
    .pause(80 milliseconds)
    .exec(deleteCarRequest)
    .pause(50 milliseconds)

  setUp(
    listCarsScenario.inject(
      rampUsers(2) over(5 seconds),
      rampUsersPerSec(initialUsersPerScenario) to (totalUsersPerScenario) during(scenarioDurationInSeconds seconds)
      //constantUsersPerSec(500) during (1 minutes))
     ),
    findCarsScenario.inject(
      atOnceUsers(3),
      rampUsers(5) over(8 seconds),
      rampUsersPerSec(initialUsersPerScenario) to (totalUsersPerScenario) during(scenarioDurationInSeconds seconds)
    ),
    addCarsScenario.inject(
      atOnceUsers(5),
      rampUsers(5) over(8 seconds),
      rampUsersPerSec(initialUsersPerScenario) to (totalUsersPerScenario) during(scenarioDurationInSeconds seconds)
    ),
    deleteCarsScenario.inject(
      atOnceUsers(5),
      rampUsers(5) over(5 seconds),
      rampUsersPerSec(initialUsersPerScenario) to (totalUsersPerScenario) during(scenarioDurationInSeconds seconds)
    )

  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(95),
      global.responseTime.mean.lessThan(expectedMeanResponseTime),
      global.responseTime.percentile1.lessThan(expectedPercentileResponseTime),
      global.responseTime.percentile2.lessThan(expectedPercentileResponseTime),
      global.responseTime.percentile3.lessThan(expected95thPercentileResponseTime),
      global.responseTime.percentile4.lessThan(expected99thPercentileResponseTime),
      global.requestsPerSec.greaterThan(expectedRequestPerSecond)

    )

}