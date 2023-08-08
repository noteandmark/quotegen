package com.andmark.qoutegen.util.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MapperConvert<T, DTO> {
    private final ModelMapper mapper;

    public MapperConvert(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public DTO convertToDTO(T entity, Class<DTO> dtoClass) {
        return mapper.map(entity, dtoClass);
    }

    public T convertToEntity(DTO dto, Class<T> entityClass) {
        return mapper.map(dto, entityClass);
    }
}
