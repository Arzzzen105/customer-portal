package org.example.customerportal.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldErrorDto {

    private String field;
    private String message;
}
