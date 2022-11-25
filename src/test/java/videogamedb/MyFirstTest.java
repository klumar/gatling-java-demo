package videogamedb;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class MyFirstTest extends Simulation {

    // HTTP Config
    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    // Scenario Def
    ScenarioBuilder scenarioBuilder = scenario("My First Test")
            .exec(http("Get all games").get("/videogame"));

    // Load Scenario
    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocolBuilder);
    }
}
