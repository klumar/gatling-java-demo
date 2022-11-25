package videogamedb.feeders;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class BasicCustomFeeder extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    Iterator<Map<String, Object>> idNumbersMapIterator = IntStream.range(1, 11)
            .boxed()
            .map(i -> Map.of("gameId", (Object) i))
            .iterator();

    ScenarioBuilder scenarioBuilder = scenario("Basic Custom Feeder")
            .exec(getSpecificVideoGame());

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocolBuilder);
    }

    private ChainBuilder getSpecificVideoGame() {
        return repeat(10).on(
                feed(idNumbersMapIterator)
                        .exec(
                                http("Get video game with id - #{gameId}")
                                        .get("/videogame/#{gameId}")
                                        .check(status().is(200))
                        )
                        .pause(1)
        );
    }

}
