package com.rogerio.libraryapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rogerio.libraryapi.dto.LoanDTO;
import com.rogerio.libraryapi.dto.LoanFilterDTO;
import com.rogerio.libraryapi.dto.ReturnedLoanDto;
import com.rogerio.libraryapi.entity.Book;
import com.rogerio.libraryapi.entity.Loan;
import com.rogerio.libraryapi.exceptions.BusinessException;
import com.rogerio.libraryapi.service.BookService;
import com.rogerio.libraryapi.service.LoanService;
import com.rogerio.libraryapi.service.LoanServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    private static String LOAN_API = "/api/loans";

    @Test
    @DisplayName("A - Deve realizar um empréstimo")
    public void createLoanTest() throws Exception {

        // Cria um arquivo json de exemplo
        LoanDTO dto = creteNewLoan();
        String json = new ObjectMapper().writeValueAsString(dto);

        // Cria um book e faz a busca pelo Isbn
        Book book = Book.builder().id(1l).isbn("12345").build();
        BDDMockito.given(bookService.getBookByIsbn("12345")).willReturn(Optional.of(Book.builder()
                .id(1l).isbn("12345").build()));

        // Cria o objeto de emprestimo e salva
        Loan loan = Loan.builder().id(1l).customer("Rogério").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        // Configura o tipo de retorno
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));

    }

    @Test
    @DisplayName("B - Deve retornar erro ao tentar fazer empréstimo de um livro inexistente")
    public void invalidIsbnCreateLoanTest() throws Exception {

        // Cria um arquivo json de exemplo
        LoanDTO dto = creteNewLoan();
        String json = new ObjectMapper().writeValueAsString(dto);

        // Procura Isbn e retorna vazio
        BDDMockito.given(bookService.getBookByIsbn("12345")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed Isbn"));
    }

    @Test
    @DisplayName("C - Deve retornar erro ao tentar fazer empréstimo de um livro jáemprestado")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {

        // Cria um arquivo json de exemplo
        LoanDTO dto = creteNewLoan();
        String json = new ObjectMapper().writeValueAsString(dto);

        // Cria um book e faz a busca pelo Isbn
        Book book = Book.builder().id(1l).isbn("12345").build();
        BDDMockito.given(bookService.getBookByIsbn("12345")).willReturn(Optional.of(Book.builder()
                .id(1l).isbn("12345").build()));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("D - Deve retornar um livro")
    public void returnedBookTest() throws Exception{
        // Cenário: returned=True
        ReturnedLoanDto dto = ReturnedLoanDto.builder().returned(true).build();

        // Cria um arquivo json de exemplo
        Loan loan = Loan.builder().id(1l).build();

        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.of(loan));

        String json = new ObjectMapper().writeValueAsString(dto);

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isOk());
        verify(loanService, Mockito.times(1)).update(loan);

    }

    @Test
    @DisplayName("E - Deve retornar 404 quando tentar devolver um livro inexistente")
    public void returnedInexistentBookTest() throws Exception{

        ReturnedLoanDto dto = ReturnedLoanDto.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("F - Deve filtrar empréstimos")
    public void filterLoanTest() throws Exception{

        Long id = 1l;

        // Cria um livro
        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        Book book = Book.builder()
                .id(1l)
                .isbn("12345")
                .build();
        loan.setBook(book);

        // Criar um Mock de busca com passagem de parametro (pag ini, qtd pag), total pag
        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

        // Cria string parametrizada para receber dadods pela url
        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                book.getIsbn(), loan.getCustomer());

        // Verifica como será o tipo de passagem de dados
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    private LoanDTO creteNewLoan() {
        return LoanDTO.builder()
                .isbn("12345")
                .customer_email("customer@email.com")
                .customer("Rogério")
                .build();
    }
}
