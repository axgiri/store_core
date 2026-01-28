package tech.github.oldlabclient.dto.response;

import lombok.Data;

@Data
public class ProductPhotoResponse {
    private String objectKey;
    private byte[] file;
}
