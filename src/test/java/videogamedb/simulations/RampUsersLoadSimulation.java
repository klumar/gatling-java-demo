package videogamedb.simulations;

import java.time.Duration;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class RampUsersLoadSimulation extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioBuilder = scenario("Ramp Users Load Simulation")
            .exec(getAllVideoGames())
            .pause(5)
            .exec(getSpecificGame())
            .pause(5)
            .exec(getAllVideoGames());

    {
        setUp(
                scenarioBuilder.injectOpen(
                                nothingFor(Duration.ofSeconds(5)),
                                constantUsersPerSec(10).during(Duration.ofSeconds(10)),
                                rampUsersPerSec(1).to(5).during(Duration.ofSeconds(20))
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
