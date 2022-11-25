package videogamedb.scriptfundamentals;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class AddPauseTime extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioBuilder = scenario("Video Game DB - 3 calls")
            .exec(
                    http("Get all video games - 1st call").get("/videogame")
            )
            .pause(5)
            .exec(
                    http("Get specific game").get("/videogame/1")
            )
            .pause(1, 10)
            .exec(
                    http("Get all video games - 2nd call").get("/videogame")
            )
            .pause(Duration.ofMillis(3_000));


    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocolBuilder);
    }

}
