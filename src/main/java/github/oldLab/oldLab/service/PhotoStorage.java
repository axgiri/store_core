package github.oldLab.oldLab.service;

public interface PhotoStorage {
    String saveDefault(byte[] bytes, String contentType);

    byte[] loadDefault(String objectKey);

    String savePerson(byte[] bytes, String contentType);

    byte[] loadPerson(String objectKey);

    String saveShop(byte[] bytes, String contentType);

    byte[] loadShop(String objectKey);

    String saveProduct(byte[] bytes, String contentType);

    byte[] loadProduct(String objectKey);

    void delete(String objectKey, String bucket);
}
