package videogamedb.simulations;

import java.time.Duration;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class BasicLoadSimulation extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioBuilder = scenario("Basic Load Simulation")
            .exec(getAllVideoGames())
            .pause(5)
            .exec(getSpecificGame())
            .pause(5)
            .exec(getAllVideoGames());

    {
        setUp(
                scenarioBuilder.injectOpen(
                                nothingFor(Duration.ofSeconds(5)),
                                atOnceUsers(5),
                                rampUsers(10).during(Duration.ofSeconds(10))
                        )
                        .protocols(httpProtocolBuilder)
        );
    }

    private ChainBuilder getAllVideoGames() {
        return exec(
                http("Get all video games")
                        .get("/videogame")
        );
    }

    private ChainBuilder getSpecificGame() {
        return exec(
                http("Get specific game")
                        .get("/videogame/2")
        );
    }
}
