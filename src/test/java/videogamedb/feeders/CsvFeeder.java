package videogamedb.feeders;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CsvFeeder extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    FeederBuilder.Batchable<String> csvFeeder = csv("data/game-csv-file.csv")
            .circular();

    ScenarioBuilder scenarioBuilder = scenario("Csv Feeder Test")
            .exec(getSpecificVideoGame());

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocolBuilder);
    }

    private ChainBuilder getSpecificVideoGame() {
        return repeat(10).on(
                feed(csvFeeder)
                        .exec(
                                http("Get video game with name - #{gameName}")
                                        .get("/videogame/#{gameId}")
                                        .check(jsonPath("$.name").isEL("#{gameName}"))
                                        .check(status().is(200))
                        )
                        .pause(1)
        );
    }

}
