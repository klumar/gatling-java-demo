package videogamedb.scriptfundamentals;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CodeReuse extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioBuilder = scenario("Code Reuse")
            .exec(getAllVideoGames())
            .pause(5)
            .exec(getSpecificGame())
            .pause(5)
            .repeat(2).on(getAllVideoGames());

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocolBuilder);
    }

    private ChainBuilder getAllVideoGames() {
        return repeat(3)
                .on(
                        exec(
                                http("Get all video games")
                                        .get("/videogame")
                                        .check(status().is(200))
                        )
                );
    }

    private ChainBuilder getSpecificGame() {
        return repeat(5, "counter")
                .on(
                        exec(
                                http("Get specific game with id: #{counter}")
                                        .get("/videogame/#{counter}")
                                        .check(status().gte(200), status().lte(210))
                        )
                );
    }

}
