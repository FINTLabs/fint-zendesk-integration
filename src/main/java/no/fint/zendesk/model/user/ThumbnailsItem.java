package no.fint.zendesk.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThumbnailsItem {
    private String contentType;
    private int size;
    private boolean inline;
    private String mappedContentUrl;
    private String fileName;
    private int width;
    private String contentUrl;
    private long id;
    private String url;
    private int height;
}
