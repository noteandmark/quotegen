package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.domain.Quote;
import com.andmark.qoutegen.dto.QuoteDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MapperConvert<T, DTO> {
    private final ModelMapper mapper;

    public MapperConvert(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public DTO convertToDTO(T entity, Class<DTO> dtoClass) {
        DTO dto = mapper.map(entity, dtoClass);

        if (dto instanceof QuoteDTO quoteDTO) {
            if (entity instanceof Quote quote) {
                quoteDTO.setBookAuthor(quote.getBookSource().getAuthor());
                quoteDTO.setBookTitle(quote.getBookSource().getTitle());
            }
        }

        return dto;
    }

    public T convertToEntity(DTO dto, Class<T> entityClass) {
        return mapper.map(dto, entityClass);
    }
}
