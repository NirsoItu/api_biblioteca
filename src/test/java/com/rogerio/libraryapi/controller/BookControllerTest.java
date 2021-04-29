package com.rogerio.libraryapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rogerio.libraryapi.dto.BookDTO;
import com.rogerio.libraryapi.entity.Book;
import com.rogerio.libraryapi.exceptions.BusinessException;
import com.rogerio.libraryapi.service.BookService;
import com.rogerio.libraryapi.service.LoanService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")                             // Habilita profile de teste em controllers
@WebMvcTest(controllers = BookController.class)     // Habilita teste em controllers
@AutoConfigureMockMvc                               // Configura um objeto
public class BookControllerTest {

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    // Criar a rota da API
    static String BOOK_API = "/api/books";

    // Injeta a dependencia de Mock no Contexto
    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("A - Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(1l).author("Rogério").title("Meu sonho").isbn("12345").build();

        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);
        // Cria objeto Json
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1l))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }


    @Test
    @DisplayName("B - Deve lançar erro de validação quando não houver dados suficientes para criação de livros")
    public void createInvalidBookTest() throws Exception{
        // Cria objeto DTO Json vazio
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("C - Deve lançar erro ao tentar cadastrar um livro com ISBN já existente")
    public void createInvalidDuplicatedIsbnTest() throws Exception {

        BookDTO dto = createNewBook();

        // Cria objeto DTO Json vazio
        String json = new ObjectMapper().writeValueAsString(dto);

        String mensagemErro = "ISBN já cadastrado.";

        BDDMockito.given(bookService.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));

    }

    @Test
    @DisplayName("D - Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception{

        // Cenário
        Long id = 1l;
        Book book = Book.builder()
                .id(id)
                .author(createNewBook().getAuthor())
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn()).build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);


        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));


    }

    @Test
    @DisplayName("E - Deve retornar resource not found quando o livro consultado não existir")
    public void bookNotFoundTest() throws Exception{

        BDDMockito.given(bookService.getById(anyLong())).willReturn(Optional.empty());


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("F - Deve deletar um livro")
    public void deleteBookTest() throws Exception {

        BDDMockito.given(bookService
                .getById(anyLong()))
                .willReturn(Optional.of(Book.builder().id(1l).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("G - Deve retornar Not Found quando não encontrar um livro para deletar")
    public void deleteBookNotFoundTest() throws Exception {

        BDDMockito.given(bookService
                .getById(anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("H - Deve atualizar um livro")
    public void updateBookTest() throws Exception {

        Long id = 1l;

        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder()
                .id(1l)
                .author("Some Author")
                .title("Some title")
                .isbn("12345").build();

        BDDMockito.given(bookService
                .getById(id))
                .willReturn(Optional.of(updatingBook));

        Book updated = Book.builder()
                .id(id)
                .author("Rogério")
                .title("Meu sonho")
                .isbn("12345").build();

        BDDMockito.given(bookService
                .update(updatingBook))
                .willReturn(updated);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value("12345"));
    }

    @Test
    @DisplayName("I - Deve retornar erro 404 ao tentar atualizar um livro inexistente")
    public void updateBookNotFoundTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(bookService
                .getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("J - Deve filtrar livros")
    public void findBookTest() throws Exception{

        Long id = 1l;

        // Cria um livro
        Book book = Book.builder()
                .id(id)
                .author(createNewBook().getAuthor())
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn())
                .build();

        // Criar um Mock de busca com passagem de parametro (pag ini, qtd pag), total pag
        BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        // Cria string parametrizada para receber dadods pela url
        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        // Verifica como será o tipo de passagem de dados
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }


    private BookDTO createNewBook() {
        return BookDTO.builder().author("Rogério").title("Meu sonho").isbn("12345").build();
    }
}
