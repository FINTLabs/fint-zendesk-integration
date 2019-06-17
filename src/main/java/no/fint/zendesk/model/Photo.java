package no.fint.zendesk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Photo {
    private String contentType;
    private int size;
    private boolean inline;
    private String mappedContentUrl;
    private String fileName;
    private int width;
    private String contentUrl;
    private long id;
    private List<ThumbnailsItem> thumbnails;
    private String url;
    private int height;
}