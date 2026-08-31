package org.example.customerportal.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PasswordValidator Unit Tests")
class PasswordValidatorTest {

    private PasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
    }

    @Test
    @DisplayName("Should accept valid complex password")
    void shouldAcceptValidComplexPassword() {
        assertTrue(validator.isValid("ValidPassword123!", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Short1!",                           // < 12 characters
        "alllowercaseandnumbers12345!",      // Missing uppercase
        "ALLUPPERCASEANDNUMBERS12345!",      // Missing lowercase
        "NoDigitsSpecialCharacter!",         // Missing digit
        "NoSpecialCharacter123456"           // Missing special character
    })
    @DisplayName("Should reject invalid passwords failing complexity requirements")
    void shouldRejectInvalidPasswords(String password) {
        assertFalse(validator.isValid(password, null));
    }

    @Test
    @DisplayName("Should reject null password")
    void shouldRejectNullPassword() {
        assertFalse(validator.isValid(null, null));
    }
}
