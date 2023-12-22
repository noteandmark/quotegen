package com.andmark.quotegen.service;

import java.util.List;

public interface AbstractService<T> {

    T save(T t);

    T findOne(Long id);

    List<T> findAll();

    T update(T t);

    void delete(Long id);
}
