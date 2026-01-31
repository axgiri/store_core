package tech.github.oldlabclient.dto.response;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductPhotoResponse {
    private final String objectKey;
    private final byte[] file;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductPhotoResponse that)) return false;
        return objectKey.equals(that.objectKey) && Arrays.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return 31 * objectKey.hashCode() + Arrays.hashCode(file);
    }
}
