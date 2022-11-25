package videogamedb.scriptfundamentals;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class CheckResponseBodyAndExtract extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioBuilder = scenario("Check with JSON Path")
            .exec(
                    http("Get specific game")
                            .get("/videogame/1")
                            .check(jsonPath("$.name").is("Resident Evil 4"))
            )
            .exec(
                    http("Get all video games")
                            .get("/videogame")
                            .check(jsonPath("$[1].id").saveAs("gameId"))
            )
            .exec(
                    session -> {
                        System.out.println(session);
                        return session;
                    }
            )
            .exec(
                    http("Get specific game")
                            .get("/videogame/#{gameId}")
                            .check(jsonPath("$.name").is("Gran Turismo 3"))
                            .check(bodyString().saveAs("responseBody"))
            )
            .exec(
                    session -> {
                        System.out.println((String) session.get("responseBody"));
                        return session;
                    }
            );

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocolBuilder);
    }

}
