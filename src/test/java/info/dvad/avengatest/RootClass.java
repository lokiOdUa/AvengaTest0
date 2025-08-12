package info.dvad.avengatest;

import io.restassured.RestAssured;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import io.github.cdimascio.dotenv.Dotenv;

public class RootClass {
    protected Faker faker = new Faker();
    private static final Logger logger = LoggerFactory.getLogger(RootClass.class);

    @BeforeSuite
    public void setup() {
        Dotenv dotenv =  Dotenv.configure()
                                .ignoreIfMissing()
                                .load();
        if (dotenv.get("BASE_URL") == null) {
            throw new IllegalStateException("Environment variable BASE_URL should be set in order to execute tests");
        }
        else {
            RestAssured.baseURI = dotenv.get("BASE_URL");
            logger.info("Base URL: {}", RestAssured.baseURI);
        }
    }
}
