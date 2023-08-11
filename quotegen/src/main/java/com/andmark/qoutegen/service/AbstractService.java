package com.andmark.qoutegen.service;

import java.util.List;

public interface AbstractService<T> {

    void save(T t);

    T findOne(Long id);

    List<T> findAll();

    void update(Long id, T t);

    void delete(Long id);
}
