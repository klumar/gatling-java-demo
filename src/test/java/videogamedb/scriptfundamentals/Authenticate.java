package videogamedb.scriptfundamentals;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class Authenticate extends Simulation {

    HttpProtocolBuilder httpProtocolBuilder = http
            .baseUrl("https://videogamedb.uk/api")
            .contentTypeHeader("application/json")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioBuilder = scenario("Authenticate")
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
        return exec(
                http("create new game")
                        .post("/videogame")
                        .header("Authorization", "Bearer #{jwtToken}")
                        .body(
                                StringBody(
                                        """
                                            {
                                                "category": "Platform",
                                                "name": "Mario",
                                                "rating": "Mature",
                                                "releaseDate": "2012-05-04",
                                                "reviewScore": 85
                                            }
                                        """
                                )
                        )
        );
    }

}
