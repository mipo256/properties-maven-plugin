package io.polivakha.mojo.properties.exception;

public class PropertyCircularDefinitionException extends RuntimeException {

    public PropertyCircularDefinitionException(String message) {
        super(message);
    }
}
