package github.oldLab.oldLab.dto.response;

import lombok.Data;

@Data
public class ProductPhotoResponse {
    private Long id;
    private String objectKey;
    private byte[] file;
}
