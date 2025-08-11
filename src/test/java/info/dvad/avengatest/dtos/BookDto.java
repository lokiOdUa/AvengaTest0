package info.dvad.avengatest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) representing a Book.
 * This class encapsulates the properties of a Book and provides methods to access these properties.
 * It is used to transfer data between layers of the application, such as between the business logic layer and the presentation layer.
 */
public class BookDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("pageCount")
    private Integer pageCount;

    @JsonProperty("excerpt")
    private String excerpt;

    private OffsetDateTime publishDate;

    // Getters
    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getExcerpt() {
        return excerpt;
    }

    @JsonProperty("publishDate")
    public String getPublishDate() {
        if (publishDate == null) {
            return null;
        }
        // Below block is needed since FakeRESTApi tends to skip final zero from milliseconds
        var pd = publishDate.toString();
        if (pd.charAt(pd.length() - 2) == '0') {
            StringBuilder sb = new StringBuilder(pd);
            return sb.deleteCharAt(pd.length() - 2).toString();
        }
        else {
            return pd;
        }
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public void setPublishDate(OffsetDateTime publishDate) {
        this.publishDate = publishDate;
    }

    // Service methods
    @Override
    public String toString() {
        return "*** " + this.getClass().getSimpleName()
                + "*[id=" + id
                + ", title=" + title
                + ", description=" + description
                + ", pageCount=" + pageCount
                + ", excerpt=" + excerpt
                + ", publishDate=" + publishDate
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BookDto)) {
            return false;
        }
        BookDto other = (BookDto) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(title, other.title)
                && Objects.equals(description, other.description)
                && Objects.equals(pageCount, other.pageCount)
                && Objects.equals(publishDate, other.publishDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, pageCount, publishDate);
    }
}
