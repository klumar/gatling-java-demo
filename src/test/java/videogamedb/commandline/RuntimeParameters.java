package videogamedb.commandline;

import java.time.Duration;

import javax.annotation.Nonnull;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * Run Using: mvn gatling:test -Dgatling.simulationClass=videogamedb.commandline.RuntimeParameters
 */
public class RuntimeParameters extends Simulation {
    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    Integer userCount = Integer.parseInt(System.getProperty("users", "5"));
    Integer rampDuration = Integer.parseInt(System.getProperty("ramp.duration", "10"));
    Integer testDuration = Integer.parseInt(System.getProperty("test.duration", "30"));

    ScenarioBuilder scenarioBuilder = scenario("Runtime Parameters Simulation")
            .forever().on(
                    exec(getAllVideoGames())
            );

    {
        setUp(
                scenarioBuilder.injectOpen(
                                nothingFor(5),
                                rampUsers(userCount).during(Duration.ofSeconds(rampDuration))
                        )
                        .protocols(httpProtocolBuilder)
        ).maxDuration(Duration.ofSeconds(testDuration));
    }

    @Override
    public void before() {
        System.out.println("Running tests with " + userCount + " users");
        System.out.println("Ramping users over " + rampDuration + " seconds");
        System.out.println("Total tests duration " + testDuration + " seconds");
    }

    private ChainBuilder getAllVideoGames() {
        return exec(
                http("Get all video games")
                        .get("/videogame")
        ).pause(5);
    }
}
