package com.andmark.quotegen.util.impl;

import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.service.UserService;
import com.andmark.quotegen.util.PersonValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class PersonValidatorImpl implements PersonValidator {

    private final UserService userService;

    public PersonValidatorImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDTO userDTO = (UserDTO) target;

        // Check that a person's names starts with a capital letter
        if (userDTO.getUsername().isEmpty()) {
            errors.rejectValue("username", "", "Введите логин. Поле не должно быть пустым");
        }
    }
}
