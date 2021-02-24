import _root_.io.gatling.core.scenario.Simulation
import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * Performance test for the Campione entity.
 */
class CampioneGatlingTest extends Simulation {

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

    val scn = scenario("Test the Campione entity")
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
            exec(http("Get all campiones")
            .get("/api/campiones")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(10 seconds, 20 seconds)
            .exec(http("Create new campione")
            .post("/api/campiones")
            .headers(headers_http_authenticated)
            .body(StringBody("""{"id":null, "codiceCampione":"SAMPLE_TEXT", "siglaCampione":"0", "tipoCampione":null, "descrizioneCampione":"SAMPLE_TEXT", "fotoSacchetto":null, "fotoCampione":null, "dataAnalisi":"2020-01-01T00:00:00.000Z", "ripartizioneQuartatura":null, "essiccamento":null, "setacciaturaSecco":null, "lavaggioSetacciatura":null, "essiccamentoNumTeglia":"SAMPLE_TEXT", "essiccamentoPesoTeglia":null, "essiccamentoPesoCampioneLordoIniziale":null, "essiccamentoPesoCampioneLordo24H":null, "essiccamentoPesoCampioneLordo48H":null, "sabbia":null, "ghiaia":null, "materialeRisultaVagliato":null, "detriti":null, "materialeFine":null, "materialeOrganico":null, "elementiMagg125Mm":null, "detritiConglomerato":null, "argilla":null, "argillaMatAlterabile":null, "granuliCementati":null, "elementiArrotondati":null, "elementiSpigolosi":null, "sfabbricidi":null, "tipoBConforme":null, "cbSpessoreTappetino":null, "cbMassaFiller":null, "cbMassaMaterialeEstratto":null, "cbMassaInterti":null, "cbMassaMiscela":null, "cbMassaBitume":null, "cbPercBitumeRispInterti":null, "cbPercBitumeRispMiscela":null, "cbProcDetMassaVol":null, "cbProv1MassaSecca":null, "cbProv1LegantePerc":null, "cbProv1MassaVolLegante":null, "cbProv1MassaInertiPerc":null, "cbProv1MassaVolInerti":null, "cbProv1MassaVolParaffina":null, "cbProv1MassaVolMax":null, "cbProv1MassaVolBulk":null, "cbProv1PercVuoti":null, "cbProv2MassaSecca":null, "cbProv2LegantePerc":null, "cbProv2MassaVolLegante":null, "cbProv2MassaInertiPerc":null, "cbProv2MassaVolInerti":null, "cbProv2MassaVolParaffina":null, "cbProv2MassaVolMax":null, "cbProv2MassaVolBulk":null, "cbProv2PercVuoti":null, "cbProv3MassaSecca":null, "cbProv3LegantePerc":null, "cbProv3MassaVolLegante":null, "cbProv3MassaInertiPerc":null, "cbProv3MassaVolInerti":null, "cbProv3MassaVolParaffina":null, "cbProv3MassaVolMax":null, "cbProv3MassaVolBulk":null, "cbProv3PercVuoti":null, "cbPercMediaVuoti":null, "curva":null, "classificazioneGeotecnica":"SAMPLE_TEXT", "note":"SAMPLE_TEXT", "lavorazioneConclusa":null}""")).asJSON
            .check(status.is(201))
            .check(headerRegex("Location", "(.*)").saveAs("new_campione_url"))).exitHereIfFailed
            .pause(10)
            .repeat(5) {
                exec(http("Get created campione")
                .get("${new_campione_url}")
                .headers(headers_http_authenticated))
                .pause(10)
            }
            .exec(http("Delete created campione")
            .delete("${new_campione_url}")
            .headers(headers_http_authenticated))
            .pause(10)
        }

    val users = scenario("Users").exec(scn)

    setUp(
        users.inject(rampUsers(100) over (1 minutes))
    ).protocols(httpConf)
}
