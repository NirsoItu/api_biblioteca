package com.rogerio.libraryapi.service;

import com.rogerio.libraryapi.dto.LoanFilterDTO;
import com.rogerio.libraryapi.entity.Book;
import com.rogerio.libraryapi.entity.Loan;
import com.rogerio.libraryapi.exceptions.BusinessException;
import com.rogerio.libraryapi.repository.LoanRepository;
import com.rogerio.libraryapi.service.imp.LoanServiceImp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService loanService;

    // Simula um repositório
    @MockBean
    LoanRepository loanRepository;

    // Estabelecer uma conexão com o banco de dados
    @BeforeEach
    public void setUp(){
        this.loanService = new LoanServiceImp(loanRepository);
    }

    @Test
    @DisplayName("A - Deve salvar um empréstimo")
    public void saveLoanTest(){

        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now()).build();

        // Cenário
        Mockito.when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(false);

        Mockito.when(loanRepository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan =  loanService.save(savingLoan);

        Assertions.assertEquals(loan.getId(), savedLoan.getId());
        Assertions.assertEquals(loan.getBook(), savedLoan.getBook());
        Assertions.assertEquals(loan.getCustomer(), savedLoan.getCustomer());
        Assertions.assertEquals(loan.getLoanDate(), savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("B - Deve lançar erro ao tentar salvar um livro já emprestado")
    public void loanedBookSaveTest(){

        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        // Cenário
        Mockito.when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(true);

        Exception exception = Assertions.assertThrows(BusinessException.class, () -> loanService.save(savingLoan));
        assertTrue(exception.getMessage().contains("Book already loaned"));
        verify(loanRepository, never()).save(savingLoan); // garantir a não execução do save, gravando os dados

    }

    @Test@DisplayName("C - Deve obter as informações de empréstimo pelo ID")
    public void getLoanDetailsTest(){

        // Cenário
        Long id = 1l;

        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        // Execução
        Optional<Loan> result = loanService.getById(id);

        // Verificação
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(result.get().getId(), id);
        Assertions.assertEquals(result.get().getBook(), loan.getBook());
        Assertions.assertEquals(result.get().getCustomer(), loan.getCustomer());
        Assertions.assertEquals(result.get().getLoanDate(), loan.getLoanDate());

        verify(loanRepository).findById(id);

    }

    @Test
    @DisplayName("D - Deve atualizar um empréstimo")
    public void updateLoanTest(){

        Loan loan = createLoan();
        loan.setId(1l);
        loan.setReturned(true);

        Mockito.when(loanRepository.save(loan)).thenReturn(loan);

        Loan updatedLoan = loanService.update(loan);

        assertTrue(updatedLoan.getReturned());

        verify(loanRepository).save(loan);


    }

    @Test
    @DisplayName("E - Filtrar empréstimos pelas propriedades")
    public void findLoanTest(){

        // Cenário
        Loan loan = createLoan();
        loan.setId(1l);

        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder()
                .customer("Rogério")
                .isbn("321").build();

        Pageable pageable = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageable, lista.size());
        Mockito.when(loanRepository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(page);

        // Execução
        Page<Loan> result = loanService.find(loanFilterDTO, pageable);

        // Verificação
        Assertions.assertEquals(result.getTotalElements(),1);
        Assertions.assertEquals(result.getContent(),lista);
        Assertions.assertEquals(result.getPageable().getPageNumber(),0);
        Assertions.assertEquals(result.getPageable().getPageSize(),10);
    }



    public static Loan createLoan(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }

}
