package videogamedb.feeders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.ElFileBody;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ComplexCustomFeeder extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .contentTypeHeader("application/json")
            .acceptHeader("application/json");
    Random random = new Random();

    Iterator<Map<String, Object>> gameMapIterator = IntStream.range(1, 11)
            .boxed()
            .map(i -> {
                Map<String, Object> feedMap = new HashMap<>();
                feedMap.put("gameId", i);
                feedMap.put("name", "Game-" + randomString(5));
                feedMap.put("releaseDate", randomDate(LocalDate.now()));
                feedMap.put("reviewScore", random.nextInt(100));
                feedMap.put("category", "Category-" + randomString(6));
                feedMap.put("rating", "Rating-" + randomString(4));

                return feedMap;
            })
            .iterator();

    ScenarioBuilder scenarioBuilder = scenario("Complex Custom Feeder")
            .exec(authenticate())
            .exec(createNewGame());

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocolBuilder);
    }

    private ChainBuilder authenticate() {
        return exec(
                http("authenticate")
                        .post("/authenticate")
                        .body(
                                StringBody(
                                        """
                                            {
                                                "username": "admin",
                                                "password": "admin"
                                            }
                                        """
                                )
                        )
                        .check(jsonPath("$.token").saveAs("jwtToken"))
        );
    }

    private ChainBuilder createNewGame() {
        return repeat(10).on(
                feed(gameMapIterator)
                        .exec(
                                http("Create new game - #{name}")
                                        .post("/videogame")
                                        .header("Authorization", "Bearer #{jwtToken}")
                                        .body(ElFileBody("bodies/game-template.json")).asJson()
                                        .check(bodyString().saveAs("responseBody"))
                        )
                        .exec(
                                session -> {
                                    System.out.println((String) session.get("responseBody"));
                                    return session;
                                }
                        )
                        .pause(1)
        );
    }

    private String randomString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'

        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private String randomDate(LocalDate localDate) {
        return localDate
                .minusDays(random.nextInt(30))
                .format(DateTimeFormatter.ISO_DATE);
    }
}
