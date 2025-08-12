package info.dvad.avengatest.tests;

import info.dvad.avengatest.RootClass;
import info.dvad.avengatest.dtos.BookDto;
import info.dvad.avengatest.utils.DataProviders;
import io.restassured.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static info.dvad.avengatest.utils.Generators.getDateDaysBefore;
import static info.dvad.avengatest.utils.Matchers.isCloseToNow;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class BooksTest extends RootClass {
    private static final Logger logger = LoggerFactory.getLogger(BooksTest.class);

    /**
     * Verifies the integrity and expected size of the book collection endpoint.
     * <p>
     * This test calls the main {@code GET /Books} endpoint and performs two key validations:
     * <ol>
     * <li><b>Size Validation:</b> It asserts that the API returns exactly 200 book records.</li>
     * <li><b>Structural Validation:</b> It implicitly verifies the JSON structure by deserializing the
     * entire response into an array of {@code BookDto} objects. This test will fail if the
     * JSON structure does not match the {@code BookDto} class definition.</li>
     * </ol>
     * This test assumes a static database state with a known quantity of 200 books, which is valid for
     * FakeRESTApi project. In real life, we would initialize database with known amount of synthetic data,
     * plus there should be possibility of adding data during the testing which, FakeRESTApi does not support.
     */
    @Test
    public void getBooksAmountAndStructureShouldBeAsExpected() {
        given()
        .when()
            .get("/Books")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", is(200))
            .extract()
            .as(BookDto[].class);
    }

    /**
     * Verifies that the fields of an existing book retrieved via the API match the expected values.
     * <p>
     * This test is data-driven and uses the "existingBooks" provider to test multiple book records.
     * It checks for the correct ID, title, description content, page count, and ensures the publish
     * date is within a specified proximity to the current date.
     *
     * @param id                  The unique ID of the book to fetch and validate.
     * @param title               The expected title of the book.
     * @param descriptionContains A case-insensitive substring expected to be in the book's description.
     * @param pageCount           The expected number of pages for the book.
     * @param distanceDays        The expected difference, in days, from the current date to the book's
     * publish date. This is used to validate the recency of the publishing date.
     */
    @Test (dataProvider = "existingBooks", dataProviderClass = DataProviders.class)
    public void getBooksFieldsShouldBeAsExpected(int id, String title, String descriptionContains, int pageCount, long distanceDays) {
        given()
        .when()
            .get("/Books/" + id)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(id))
            .body("title", equalTo(title))
            .body("description", containsStringIgnoringCase(descriptionContains))
            .body("pageCount", equalTo(pageCount))
            .body("excerpt", is(notNullValue()))
            .body("publishDate", isCloseToNow( distanceDays));
    }

    /**
     * Verifies that the API correctly handles a request for a non-existent book.
     * <p>
     * This test attempts to fetch a book using a deliberately non-existent ID
     * and asserts that the server responds with a {@code 404 Not Found} status code.
     * It also checks that the response body contains a "Not Found" message, ensuring
     * the application's error-handling path is working as expected.
     */
    @Test
    public void shouldFailInExpectedWayIfNonExistingBookIsRequested() {
        given()
        .when()
            .get("/Books/100500")
        .then()
            .statusCode(404)
            .body(containsString("Not Found"));
    }

    /**
     * Verifies the end-to-end "happy path" for creating a new book.
     * <p>
     * This test constructs a valid {@code BookDto} object with a hardcoded ID and randomly
     * generated data for its other fields. It sends this object via a {@code POST} request
     * to the {@code /Books} endpoint to create the new resource.
     * <p>
     * The test then performs a comprehensive validation, asserting that the server responds
     * with a {@code 200 OK} status and that every field in the returned JSON object
     * exactly matches the data that was sent. This confirms that the book was created
     * successfully and that the API returns the new resource in the response body.
     */
    @Test
    public void shouldBeAbleToAddBook() {
        var newBook = new BookDto();
        newBook.setId(100500);
        newBook.setTitle(faker.book().title());
        newBook.setDescription(faker.lorem().paragraph(1));
        newBook.setPageCount(ThreadLocalRandom.current().nextInt(100, 1000));
        newBook.setExcerpt(faker.lorem().paragraph(2));

        newBook.setPublishDate(getDateDaysBefore(365));

        logger.info("Trying to add a valid book {}", newBook);

        var r = given()
            .contentType(ContentType.JSON)
            .body(newBook)
        .when()
            .post("/Books")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(newBook.getId()))
            .body("title", equalTo(newBook.getTitle()))
            .body("description", equalTo(newBook.getDescription()))
            .body("pageCount", equalTo(newBook.getPageCount()))
            .body("excerpt", equalTo(newBook.getExcerpt()))
            .body("publishDate", equalTo(newBook.getPublishDate()));
    }

    /**
     * Verifies that the API correctly rejects attempts to create a book with invalid data.
     * <p>
     * This data-driven test uses the "invalidBooks" provider to send various invalid payloads,
     * such as providing a non-null ID or an invalid page count for a new book. It asserts that
     * each invalid request results in a 400 Bad Request status and that the response body
     * contains the expected validation error message.
     *
     * @param bookId       The ID for the new book. Used to test invalid scenarios, like providing a
     * non-null ID where the system should generate one. Since our system does not generate real books,
     * only null considered as erroneous value.
     * @param pageCount    The number of pages for the book. Used to test invalid values like null,
     * zero, or negative numbers. Our system accepts even negative number here, only null considered
     * as a wrong value.
     * @param errorMessage The expected error message string that should appear in the API's
     * error response.
     */
    @Test(dataProvider = "invalidBooks", dataProviderClass = DataProviders.class)
    public void shouldFailInExpectedWayIfAddingIncorrectBook(Integer bookId, Integer pageCount, String time, String errorMessage) {
        var newBook = new BookDto();
        newBook.setId(bookId);
        newBook.setTitle(faker.book().title());
        newBook.setDescription(faker.lorem().paragraph(1));
        newBook.setPageCount(pageCount);
        newBook.setExcerpt(faker.lorem().paragraph(2));
        if (time != null) {
            newBook.setPublishDate(OffsetDateTime.parse(time));
        }

        logger.info("Trying to add invalid book {}", newBook);

        given()
            .contentType(ContentType.JSON)
            .body(newBook)
        .when()
            .post("/Books")
        .then()
            .statusCode(400)
            .body(containsString(errorMessage));
    }

    /**
     * Attempts to verify the book update functionality by sending a PUT request.
     * <p>
     * Since FakeRESTApi doesn't write any actual data, it just accepts a valid JSON and returns it as
     * response, so that method uses random object IDs. In a real project, I would update and existing
     * object and check it via API call after update. Also, FakeRESTApi allows to use different Book
     * IDs in location parameter vs JSON payload which seems to be incorrect behavior for a real project.
     */
    @Test
    public void shouldBeAbeToUpdateBook() {
        var updatedBook = new BookDto();
        updatedBook.setId(ThreadLocalRandom.current().nextInt(0, 100500));
        updatedBook.setTitle(faker.book().title());
        updatedBook.setDescription(faker.lorem().paragraph(1));
        updatedBook.setPageCount(ThreadLocalRandom.current().nextInt(100, 1000));
        updatedBook.setExcerpt(faker.lorem().paragraph(2));
        updatedBook.setPublishDate(getDateDaysBefore(365));

        logger.info("Trying to update a book with {}", updatedBook);

        given()
            .contentType(ContentType.JSON)
            .body(updatedBook)
        .when()
            .put("/Books/" + ThreadLocalRandom.current().nextInt(0, 100500))
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(updatedBook.getId()))
            .body("title", equalTo(updatedBook.getTitle()))
            .body("description", equalTo(updatedBook.getDescription()))
            .body("pageCount", equalTo(updatedBook.getPageCount()))
            .body("excerpt", equalTo(updatedBook.getExcerpt()));
    }

    /**
     * Verifies that the API correctly rejects attempts to update a book with invalid data.
     * <p>
     * This data-driven test sends a {@code PUT} request with various invalid payloads, such as an
     * incorrect page count or a malformed publish date string. It asserts that the server
     * responds with a {@code 400 Bad Request} status and that the response body contains the
     * specific validation error message corresponding to the invalid input. To isolate the
     * validation, other book details are populated with valid, randomly generated data.
     *
     * @param bookId       The ID of the book to be updated.
     * @param pageCount    The page count for the book, used to test invalid values.
     * @param time         A string representation of the publish date, including values used to test invalid format.
     * @param errorMessage The expected error message returned by the API.
     */
    @Test(dataProvider = "invalidBooks", dataProviderClass = DataProviders.class)
    public void shouldFailInExpectedWayIfIncorrectlyUpdatingBook(Integer bookId, Integer pageCount, String time, String errorMessage) {
        var updatedBook = new BookDto();
        updatedBook.setId(bookId);
        updatedBook.setTitle(faker.book().title());
        updatedBook.setDescription(faker.lorem().paragraph(1));
        updatedBook.setPageCount(pageCount);
        updatedBook.setExcerpt(faker.lorem().paragraph(2));
        if (time != null) {
            updatedBook.setPublishDate(OffsetDateTime.parse(time));
        }

        logger.info("Trying to update a book with invalid data {}", updatedBook);

        given()
            .contentType(ContentType.JSON)
            .body(updatedBook)
        .when()
            .put("/Books/" + bookId)
        .then()
            .statusCode(400)
            .body(containsString(errorMessage));
    }

    /**
     * Verifies that the API can successfully process a DELETE request for a book.
     * <p>
     * This test sends an HTTP {@code DELETE} request to the {@code /Books/{id}} endpoint,
     * using a randomly generated ID. It then asserts that the server responds with a
     * {@code 200 OK} status code, indicating the request was processed successfully.
     * <p>
     * <b>Note:</b> The real endpoint seems to be returning 200 to everything except null,
     * so using random book id for fun. In a real project, off course I would add a book
     * in the preconditions.
     */
    @Test
    public void shouldBeAbleToDeleteBook() {
        given()
                .when()
                .delete("/Books/" + ThreadLocalRandom.current().nextInt(0, 100500))
                .then()
                .statusCode(200);
    }

    /**
     * Verifies that the API correctly handles a DELETE request with an invalid, non-numeric book ID.
     * <p>
     * This test sends a {@code DELETE} request using the literal string "null" as the path
     * parameter for the book ID. It asserts that the server rejects this malformed request
     * with a {@code 400 Bad Request} status code and that the response body contains a specific
     * error message confirming the input is invalid. This ensures the application's input
     * validation for path parameters is working correctly.
     */
    @Test
    public void shouldFailInExpectedWayIfTryingToDeleteNonExistingBook() {
        given()
                .when()
                .delete("/Books/null")
                .then()
                .statusCode(400)
                .body(containsString("The value 'null' is not valid"));
    }
}
