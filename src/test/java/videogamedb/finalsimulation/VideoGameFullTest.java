package videogamedb.finalsimulation;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
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
import static io.gatling.javaapi.core.CoreDsl.listFeeder;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * Run Using: mvn gatling:test -Dgatling.simulationClass=videogamedb.finalsimulation.VideoGameFullTest -Denv=local
 */
public class VideoGameFullTest extends Simulation {

    private final List<Map<String, Object>> createGameMapList = IntStream.range(1, 21)
            .boxed()
            .map(i -> {
                Map<String, Object> feedMap = new HashMap<>();
                feedMap.put("id", i);
                feedMap.put("name", "name-" + i);
                feedMap.put("releaseDate", "2022-09-" + String.format("%01d", i));
                feedMap.put("reviewScore", 1);
                feedMap.put("category", "category-" + 1);
                feedMap.put("rating", "rating-" + i);

                return feedMap;
            }).toList();
    private final List<Map<String, Object>> specificGameMapList = IntStream.range(1, 6)
            .boxed()
            .map(i -> Map.of("id", (Object) i))
            .toList();

    private final FeederBuilder<Object> createGameMapListFeeder = listFeeder(createGameMapList).circular();
    private final FeederBuilder<Object> specificGameMapListFeeder = listFeeder(specificGameMapList).circular();

    private final int users;
    private final int rampTime;
    private final int testDuration;

    public VideoGameFullTest() {
        HttpProtocolBuilder httpProtocolBuilder = http
                .baseUrl("https://videogamedb.uk/api")
                .acceptHeader("application/json")
                .contentTypeHeader("application/json");

        // 1. get all games
        // 2. create new game (remember to authenticate)
        // 3. get details of single game
        // 4. delete a game
        ScenarioBuilder scenarioBuilder = scenario("Video Game Full Test Simulation")
                .forever().on(
                        exec(getAllVideoGames())
                                .exec(authenticate())
                                .exec(createNewGame())
                                .exec(getSpecificGame())
                                .exec(deleteGame())
                );

        // 1. Create simulation that has runtime params: users, ramp up time, test duration
        // 2. Have a feeder to generate the json for creating new game
        // 3. print out msgs before and after test
        var env = System.getProperty("env", "default");
        this.users = Integer.parseInt(ConfigUtil.getConfig(env, "users"));
        this.rampTime = Integer.parseInt(ConfigUtil.getConfig(env, "ramp.time"));
        this.testDuration = Integer.parseInt(ConfigUtil.getConfig(env, "test.duration"));
        setUp(
                scenarioBuilder.injectOpen(
                                nothingFor(Duration.ofSeconds(2)),
                                atOnceUsers(2),
                                rampUsers(users)
                                        .during(Duration.ofSeconds(rampTime))
                        )
                        .protocols(httpProtocolBuilder)
        ).maxDuration(Duration.ofSeconds(testDuration));
    }

    @Override
    public void before() {
        System.out.println("Running tests with " + users + " users");
        System.out.println("Ramping users over " + rampTime + " seconds");
        System.out.println("Total tests duration " + testDuration + " seconds");
    }

    @Override
    public void after() {
        System.out.println("Finished running the tests");
    }

    private ChainBuilder getAllVideoGames() {
        return exec(
                http("Get all video games")
                        .get("/videogame")
        ).pause(2);
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
        ).pause(2);
    }

    private ChainBuilder createNewGame() {
        return repeat(createGameMapList.size()).on(
                feed(createGameMapListFeeder)
                        .exec(
                                http("Create new game - #{name}")
                                        .post("/videogame")
                                        .header("Authorization", "Bearer #{jwtToken}")
                                        .body(ElFileBody("bodies/game-template-2.json")).asJson()
                                        .check(bodyString().saveAs("createNewGameResponseBody"))
                        )
                        .exec(
                                session -> {
                                    System.out.println((String) session.get("createNewGameResponseBody"));
                                    return session;
                                }
                        )
                        .pause(2)
        );
    }

    private ChainBuilder getSpecificGame() {
        return repeat(specificGameMapList.size()).on(
                feed(specificGameMapListFeeder)
                        .exec(
                                http("Get specific game - #{id}")
                                        .get("/videogame/#{id}")
                                        .check(bodyString().saveAs("getSpecificGameResponseBody"))
                        )
                        .exec(
                                session -> {
                                    System.out.println((String) session.get("getSpecificGameResponseBody"));
                                    return session;
                                }
                        )
                        .pause(2)
        );
    }

    private ChainBuilder deleteGame() {
        return repeat(specificGameMapList.size()).on(
                feed(specificGameMapListFeeder)
                        .exec(
                                http("Delete specific game - #{id}")
                                        .delete("/videogame/#{id}")
                                        .header("Authorization", "Bearer #{jwtToken}")
                                        .check(bodyString().saveAs("deleteSpecificGameResponseBody"))
                        )
                        .exec(
                                session -> {
                                    System.out.println((String) session.get("deleteSpecificGameResponseBody"));
                                    return session;
                                }
                        )
                        .pause(2)
        );
    }

}
