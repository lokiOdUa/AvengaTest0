package info.dvad.avengatest.dtos;
import com.beust.jcommander.internal.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Data Transfer Object (DTO) representing an Author.
 * This class encapsulates the properties of an Author and provides methods to access these properties.
 * It is used to transfer data between layers of the application, such as between the business logic layer and the presentation layer.
 */
public class AuthorDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("idBook")
    private Integer idBook;

    @JsonProperty("firstName")
    @Nullable
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    // Getters
    public Integer getId() {
        return id;
    }

    public Integer getIdBook() {
        return idBook;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setIdBook(Integer idBook) {
        this.idBook = idBook;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Service methods
    @Override
    public String toString() {
        return "*** " + this.getClass().getSimpleName()
                + "*[id=" + id
                + ", idBook=" + idBook
                + ", firstName=" + firstName
                + ", lastName=" + lastName
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AuthorDto)) {
            return false;
        }
        AuthorDto other = (AuthorDto) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(idBook, other.idBook)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idBook, firstName, lastName);
    }
}
