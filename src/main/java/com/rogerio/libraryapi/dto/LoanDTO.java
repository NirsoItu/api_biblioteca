package com.rogerio.libraryapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanDTO {
    private Long id;

    @NotEmpty
    private String isbn;

    @NotEmpty
    private String customer;

    @NotEmpty
    private String customer_email;

    private BookDTO bookDTO;
}
