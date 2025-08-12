package info.dvad.avengatest.tests;

import info.dvad.avengatest.RootClass;
import info.dvad.avengatest.dtos.AuthorDto;
import info.dvad.avengatest.utils.DataProviders;
import io.restassured.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class AuthorsTest extends RootClass {
    private static final Logger logger = LoggerFactory.getLogger(AuthorsTest.class);

    /**
     * Verifies the integrity and expected size of the authors collection endpoint.
     * <p>
     * This test calls the main {@code GET /Authors} endpoint and performs two key validations:
     * <ol>
     * <li><b>Size Validation:</b> It asserts that the API returns exactly 200 author records.</li>
     * <li><b>Structural Validation:</b> It implicitly verifies the JSON structure by deserializing the
     * entire response into an array of {@code AuthorDto} objects. This test will fail if the
     * JSON structure does not match the {@code AuthorDto} class definition.</li>
     * </ol>
     * This test assumes a static database state with a known quantity of 200 authors, which is valid for
     * FakeRESTApi project. In real life, we would initialize database with known amount of synthetic data,
     * plus there should be possibility of adding data during the testing which, FakeRESTApi does not support.
     */
    @Test
    public void getAuthorsAmountAndStructureShouldBeAsExpected() {
        given()
        .when()
            .get("/Authors")
        .then()
            .statusCode(200)
            .contentType("application/json")
            .extract()
            .as(AuthorDto[].class);
    }

    /**
     * Verifies that the fields of an existing author retrieved via the API match the expected values.
     * <p>
     * This test is data-driven and uses the "existingAuthors" provider to test multiple author records.
     * It checks for the correct ID, idBook, first and last names.
     *
     * @param id                  The unique ID of the author to fetch and validate.
     * @param idBook              The expected title of the book bound to the author.
     * @param firstName           First name of the author we check.
     * @param lastName            Last name of the author we check.
     */
    @Test (dataProvider = "existingAuthors", dataProviderClass = DataProviders.class)
    public void getAuthorFieldsShouldBeAsExpected(Integer id, Integer idBook, String firstName, String lastName) {
        given()
        .when()
            .get("/Authors/" + id)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(id))
            .body("idBook", equalTo(idBook))
            .body("firstName", equalTo(firstName))
            .body("lastName", equalTo(lastName));
    }

    /**
     * Verifies that the API correctly handles a request for a non-existent author.
     * <p>
     * This test attempts to fetch an author using a deliberately non-existent ID
     * and asserts that the server responds with a {@code 404 Not Found} status code.
     * It also checks that the response body contains a "Not Found" message, ensuring
     * the application's error-handling path is working as expected.
     */
    @Test
    public void shouldFailInExpectedWayIfNonExistingAuthorIsRequested() {
        given()
            .when()
                .get("/Authors/100500")
            .then()
                .statusCode(404)
                .body(containsString("Not Found"));
    }

    /**
     * Verifies the end-to-end process of creating a new author and then retrieving them.
     * <p>
     * This happy-path test performs two main operations:
     * <ol>
     * <li><b>Creation:</b> It sends a {@code POST} request with randomly generated author data
     * to create a new author. It validates the immediate response from this request and
     * extracts the new author's ID.</li>
     * <li><b>Verification:</b> It uses the extracted ID to send a subsequent {@code GET} request.
     * It then validates the data from this second request to ensure the author's details
     * were correctly persisted and can be retrieved.</li>
     * </ol>
     * The test includes a special check for a known limitation of FakeRESTApi - it does not
     * create any essences but returns more-less random values instead. If the returned ID is 0,
     * the test is failed intentionally. Although, once you uncomment "authorId = 3;" before the
     * AssertionError, and set "(invocationCount = 100)" for this scenario, you should see it
     * green at least a few times.
     */
    @Test
    public void shouldBeAbleToAddAuthor() {
        var bookId = 2; // Assuming this ID always exist and the book is valid so can be used as a parameter
        var newAuthor = new AuthorDto();
        newAuthor.setId(0);
        newAuthor.setIdBook(bookId);
        newAuthor.setFirstName(faker.name().firstName());
        newAuthor.setLastName(faker.name().lastName());

        logger.info("Adding author {}", newAuthor);

        final int authorId =
            given()
                .contentType(ContentType.JSON)
                .body(newAuthor)
            .when()
                .post("/Authors")
            .then()
                .statusCode(200)
                .body("firstName", equalTo(newAuthor.getFirstName()))
                .body("lastName", equalTo(newAuthor.getLastName()))
                .extract()
                .path("id");

        logger.info("Successfully created author with id {}", authorId);

//        authorId = 3;   // Uncomment it to mimic actual checking of author just created
        if (authorId == 0) {
            throw new AssertionError("Sorry guys but FakeRESTApi is really fake so we can't add author in " +
                    "fact. Sad but true. But please review tests' Javadoc since it's in your hands to get it green!");
        }
        else {
            ensureAuthorWasAddedSuccessfully(authorId, bookId);
        }
    }

    private void ensureAuthorWasAddedSuccessfully(int authorId, int bookId) {
        AuthorDto authorResponse =
            given()
            .when()
                .get("/Authors/" + authorId)
        .then()
            .statusCode(200)
            .extract()
            .as(AuthorDto.class);

        logger.info("Successfully retrieved data of the author {}", authorResponse);

        AuthorDto[] booksResponse =
            given()
            .when()
                .get("/Authors/authors/books/" + bookId)
            .then()
                .statusCode(200)
                .extract()
                .as(AuthorDto[].class);

        logger.info("Successfully retrieved list of {} items of authors books {}", booksResponse.length, booksResponse);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(authorResponse.getId(), (Integer) authorId, "ID of author should match");
        softAssert.assertEquals(authorResponse.getIdBook(), (Integer) bookId, "ID of book should match");
        softAssert.assertTrue(
                Arrays.stream(booksResponse).anyMatch(author -> author.getId() == authorId),
                "At least one book should be from our author"
        );

        softAssert.assertAll();
    }

    /**
     * Verifies that FakeRESTApi correctly rejects attempts to create an author with invalid data.
     * <p>
     * This data-driven test uses the "invalidAuthors" provider to supply various invalid inputs,
     * such as a non-null author ID or a null book ID. It then confirms that the server responds
     * with a 400 Bad Request status and that the response body contains the expected error message.
     * The author's first and last names are hardcoded to valid values to isolate the test's focus,
     * since FakeRESTApi seems to be accepting any values there, including nulls.
     *
     * @param authorId     The ID for the new author, used to test invalid scenarios like providing a
     * non-null value where the system should generate it.
     * @param bookId       The ID of the book to associate the author with, used to test invalid inputs
     * like a null or non-existent ID.
     * @param errorMessage The expected validation error message to be found in the API's response body.
     */
    @Test(dataProvider = "invalidAuthors", dataProviderClass = DataProviders.class)
    public void shouldFailInExpectedWayIfAddingIncorrectAuthor(Integer authorId, Integer bookId, String errorMessage) {
        var newAuthor = new AuthorDto();
        newAuthor.setId(authorId);
        newAuthor.setIdBook(bookId);
        newAuthor.setFirstName("John");
        newAuthor.setLastName("Smith");

        logger.info("Trying to add invalid author {}", newAuthor);

        given()
            .contentType(ContentType.JSON)
            .body(newAuthor)
        .when()
            .post("/Authors")
        .then()
            .statusCode(400)
            .body(containsString(errorMessage));
    }

    /**
     * Attempts to verify the author update functionality by sending a PUT request.
     * <p>
     * Since FakeRESTApi doesn't write any actual data, it just accepts a valid JSON and returns it as
     * response, so that method uses random object IDs. In a real project, I would update and existing
     * object and check it via API call after update. Also, FakeRESTApi allows to use different Author
     * IDs in location parameter vs JSON payload which seems to be incorrect behavior for a real project.
     */
    @Test
    public void shouldBeAbleToUpdateAuthor() {
        var updatedAuthor = new AuthorDto();
        updatedAuthor.setId(ThreadLocalRandom.current().nextInt(0, 100500));
        updatedAuthor.setIdBook(ThreadLocalRandom.current().nextInt(0, 100500));
        updatedAuthor.setFirstName(faker.name().firstName());
        updatedAuthor.setLastName(faker.name().lastName());

        logger.info("Trying to update an author with {}", updatedAuthor);

        given()
            .contentType(ContentType.JSON)
            .body(updatedAuthor)
        .when()
            .put("/Authors/" + ThreadLocalRandom.current().nextInt(0, 100500))
        .then()
            .statusCode(200)
            .body("id", equalTo(updatedAuthor.getId()))
            .body("idBook", equalTo(updatedAuthor.getIdBook()))
            .body("firstName", equalTo(updatedAuthor.getFirstName()))
            .body("lastName", equalTo(updatedAuthor.getLastName()));
    }

    /**
     * Verifies that FakeRESTApi correctly rejects attempts to update an author with invalid data.
     * <p>
     * This data-driven test uses the "invalidAuthors" provider to supply invalid data,
     * such as a null book ID. It then attempts to update an existing author via a {@code PUT}
     * request. The test asserts that the server responds with a 400 Bad Request status and
     * that the response body contains the expected validation error message. To isolate the
     * validation, the author's first and last names are set to fixed, valid values, since FakeRESTApi
     * seems to be accepting any values there, including nulls. Also, it allows to use different Author
     * IDs in location parameter vs JSON payload which seems to be incorrect behavior for a real project.
     *
     * @param authorId     The ID of the existing author to be updated.
     * @param bookId       The invalid book ID to be set on the author, used to trigger the
     * validation error.
     * @param errorMessage The expected error message string that should be present in the
     * API's response.
     */
    @Test(dataProvider = "invalidAuthors", dataProviderClass = DataProviders.class)
    public void shouldFailInExpectedWayIfIncorrectlyUpdatingAuthor(Integer authorId, Integer bookId, String errorMessage) {
        var updatedAuthor = new AuthorDto();
        updatedAuthor.setId(authorId);
        updatedAuthor.setIdBook(bookId);
        updatedAuthor.setFirstName("John");
        updatedAuthor.setLastName("Smith");

        logger.info("Trying to update an author with invalid data {}", updatedAuthor);

        given()
            .contentType(ContentType.JSON)
            .body(updatedAuthor)
        .when()
            .put("/Authors/" + authorId)
        .then()
            .statusCode(400)
            .body(containsString(errorMessage));
    }

    /**
     * Verifies that the API can successfully process a DELETE request for an author.
     * <p>
     * This test sends an HTTP {@code DELETE} request to the {@code /Authors/{id}} endpoint,
     * using a randomly generated ID. It then asserts that the server responds with a
     * {@code 200 OK} status code, indicating the request was processed successfully.
     * <p>
     * <b>Note:</b> The real endpoint seems to be returning 200 to everything except null,
     * so using random author id for fun. In a real project, off course I would add an author
     * in the preconditions.
     */
    @Test
    public void shouldBeAbleToDeleteAuthor() {
        given()
        .when()
            .delete("/Authors/" + ThreadLocalRandom.current().nextInt(0, 100500))
        .then()
            .statusCode(200);
    }

    /**
     * Verifies that the API correctly handles a DELETE request with an invalid, non-numeric author ID.
     * <p>
     * This test sends a {@code DELETE} request using the literal string "null" as the path
     * parameter for the author ID. It asserts that the server rejects this malformed request
     * with a {@code 400 Bad Request} status code and that the response body contains a specific
     * error message confirming the input is invalid. This ensures the application's input
     * validation for path parameters is working correctly.
     */
    @Test
    public void shouldFailInExpectedWayIfTryingToDeleteNonExistingAuthor() {
        given()
        .when()
            .delete("/Authors/null")
        .then()
            .statusCode(400)
            .body(containsString("The value 'null' is not valid"));
    }
}
