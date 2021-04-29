package com.rogerio.libraryapi.controller;

import com.rogerio.libraryapi.dto.BookDTO;
import com.rogerio.libraryapi.dto.LoanDTO;
import com.rogerio.libraryapi.dto.LoanFilterDTO;
import com.rogerio.libraryapi.dto.ReturnedLoanDto;
import com.rogerio.libraryapi.entity.Book;
import com.rogerio.libraryapi.entity.Loan;
import com.rogerio.libraryapi.service.BookService;
import com.rogerio.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final BookService bookService;
    private final LoanService loanService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO loanDTO){

        Book book = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed Isbn"));

        Loan entity = Loan.builder()
                .book(book)
                .customer(loanDTO.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = loanService.save(entity);

        return entity.getId();
    }

    @PatchMapping("{id}")
    public void returnedBook(@PathVariable Long id,
                             @RequestBody ReturnedLoanDto dto){

        Loan loan = loanService.getById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageable){
        Page<Loan> result = loanService.find(dto, pageable);
        List<LoanDTO> loans = result.getContent()
                .stream()
                .map( entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loans, pageable, result.getTotalElements());
    }
}
