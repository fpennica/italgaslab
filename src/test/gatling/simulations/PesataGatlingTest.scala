import _root_.io.gatling.core.scenario.Simulation
import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * Performance test for the Pesata entity.
 */
class PesataGatlingTest extends Simulation {

    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    // Log all HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))
    // Log failed HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"))

    val baseURL = Option(System.getProperty("baseURL")) getOrElse """http://127.0.0.1:8080"""

    val httpConf = http
        .baseURL(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
        .connection("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")

    val headers_http = Map(
        "Accept" -> """application/json"""
    )

    val headers_http_authenticated = Map(
        "Accept" -> """application/json""",
        "X-CSRF-TOKEN" -> "${csrf_token}"
    )

    val scn = scenario("Test the Pesata entity")
        .exec(http("First unauthenticated request")
        .get("/api/account")
        .headers(headers_http)
        .check(status.is(401))
        .check(headerRegex("Set-Cookie", "CSRF-TOKEN=(.*); [P,p]ath=/").saveAs("csrf_token"))).exitHereIfFailed
        .pause(10)
        .exec(http("Authentication")
        .post("/api/authentication")
        .headers(headers_http_authenticated)
        .formParam("j_username", "admin")
        .formParam("j_password", "admin")
        .formParam("remember-me", "true")
        .formParam("submit", "Login")).exitHereIfFailed
        .pause(1)
        .exec(http("Authenticated request")
        .get("/api/account")
        .headers(headers_http_authenticated)
        .check(status.is(200))
        .check(headerRegex("Set-Cookie", "CSRF-TOKEN=(.*); [P,p]ath=/").saveAs("csrf_token")))
        .pause(10)
        .repeat(2) {
            exec(http("Get all pesatas")
            .get("/api/pesatas")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(10 seconds, 20 seconds)
            .exec(http("Create new pesata")
            .post("/api/pesatas")
            .headers(headers_http_authenticated)
            .body(StringBody("""{"id":null, "numPesata":"0", "pesoNetto":null, "tratt125Mm":null, "tratt63Mm":null, "tratt31V5Mm":null, "tratt16Mm":null, "tratt8Mm":null, "tratt6V3Mm":null, "tratt4Mm":null, "tratt2Mm":null, "tratt1Mm":null, "tratt0V5Mm":null, "tratt0V25Mm":null, "tratt0V125Mm":null, "tratt0V075Mm":null, "trattB100Mm":null, "trattB6V3Mm":null, "trattB2Mm":null, "trattB0V5Mm":null, "fondo":null}""")).asJSON
            .check(status.is(201))
            .check(headerRegex("Location", "(.*)").saveAs("new_pesata_url"))).exitHereIfFailed
            .pause(10)
            .repeat(5) {
                exec(http("Get created pesata")
                .get("${new_pesata_url}")
                .headers(headers_http_authenticated))
                .pause(10)
            }
            .exec(http("Delete created pesata")
            .delete("${new_pesata_url}")
            .headers(headers_http_authenticated))
            .pause(10)
        }

    val users = scenario("Users").exec(scn)

    setUp(
        users.inject(rampUsers(100) over (1 minutes))
    ).protocols(httpConf)
}
