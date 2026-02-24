package tech.github.storecore.service;

public interface ProductExistCountProjection {
    boolean isExists();

    long getCount();
}
