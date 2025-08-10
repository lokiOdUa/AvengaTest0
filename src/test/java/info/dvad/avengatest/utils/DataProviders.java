package info.dvad.avengatest.utils;

import org.testng.annotations.DataProvider;

public class DataProviders {

    @DataProvider(name = "existingAuthors")
    public Object[][] existingAuthors() {
        return new Object[][] {
                { 1, 1, "First Name 1", "Last Name 1" },
                { 2, 1, "First Name 2", "Last Name 2" }
        };
    }

    @DataProvider(name = "existingBooks")
    public Object[][] existingBooks() {
        return new Object[][] {
                { 1, "Book 1", "lor", 100, 100 },
                { 100, "Book 100", "lor", 10000, 100 },
                { 150, "Book 150", "lor", 15000, 150 }
        };
    }

    @DataProvider(name = "invalidAuthors") public Object[][] invalidAuthors() {
        return new Object[][] {
                { null, 0, "The JSON value could not be converted to System.Int32. Path: $.id" },
                { 0, null, "The JSON value could not be converted to System.Int32. Path: $.idBook" }
        };
    }

    @DataProvider(name = "invalidBooks") public Object[][] invalidBooks() {
        return new Object[][] {
                { null, 0, "2025-08-01T02:03:04.567890+00:00", "The JSON value could not be converted to System.Int32. Path: $.id" },
                { 0, null, "2025-08-01T02:03:04.567890+00:00", "The JSON value could not be converted to System.Int32. Path: $.pageCount" },
                { 0, 0, null, "The JSON value could not be converted to System.DateTime. Path: $.publishDate" }
        };
    }
}
