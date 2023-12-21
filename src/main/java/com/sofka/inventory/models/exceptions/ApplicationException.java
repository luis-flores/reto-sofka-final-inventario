package com.sofka.inventory.models.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class ApplicationException extends Exception {
    private String message;

    public ApplicationException(String message) {
        super(message);
    }
}
