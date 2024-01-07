package com.example.ezibackend.service.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Class<?> object_class, Long id) {
        super(String.format("%s with this id doesn't exist: %d", object_class.getSimpleName(), id));
    }
}