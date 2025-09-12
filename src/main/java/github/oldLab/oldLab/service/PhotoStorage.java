package github.oldLab.oldLab.service;

public interface PhotoStorage {
    String save(byte[] bytes, String contentType);

    byte[] load(String objectKey);
    
    void delete(String objectKey);
}
