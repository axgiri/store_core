package github.oldLab.oldLab.service;

public interface PhotoStorage {
    String save(byte[] bytes, String contentType, String bucket);

    byte[] load(String objectKey, String bucket);

    void delete(String objectKey, String bucket);
}
