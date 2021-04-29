package com.rogerio.libraryapi.service;

import com.rogerio.libraryapi.entity.Book;
import com.rogerio.libraryapi.exceptions.BusinessException;
import com.rogerio.libraryapi.repository.BookRepository;
import com.rogerio.libraryapi.service.imp.BookServiceImp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    // Criar a rota da API
    static String BOOK_API = "/api/books";

    // Simula um repositório
    @MockBean
    BookRepository bookRepository;

    // Estabelecer uma conexão com o banco de dados
    @BeforeEach
    public void setUp(){
        this.bookService = new BookServiceImp(bookRepository);
    }


    @Test
    @DisplayName("A - Deve salvar um livro")
    public void saveBookTest(){

        // Criar Cenário
        Book book = createValidBook();

        // Impede um lançamento de exceção
        Mockito.when(bookRepository.existsByIsbn(Mockito.any())).thenReturn(false);

        // Simula o savamento de um livro
        Mockito.when(bookRepository.save(book)).thenReturn(Book.builder()
                .id(1l)
                .author("Larissa")
                .title("Minha namorada")
                .isbn("54321").build());

        // Execução
        Book savedBook = bookService.save(book);

        // Verificação
        assertEquals(savedBook.getId(), 1l);
        assertEquals(savedBook.getAuthor(),"Larissa");
        assertEquals(savedBook.getTitle(),"Minha namorada");
        assertEquals(savedBook.getIsbn(),"54321");
    }

    @Test
    @DisplayName("B - Deve lançar erro ao tentar salvar livro com ISBN duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN() {

        // Cenário
        Book book = createValidBook();

        // Cria um lançamento de exceção
        Mockito.when(bookRepository.existsByIsbn(Mockito.any())).thenReturn(true);

        String mensagemErro = "ISBN já cadastrado.";

        // Verifica se a mensagem de erro é a mesma de ISBN já cadastrado
        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.save(book));
        assertTrue(exception.getMessage().contains(mensagemErro));

        // Verifica que eu não estou chamando o método salvar do repository
        verify(bookRepository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("C - Deve obter um livro por ID")
    public void getBookByIDTest(){

        // Cria cenário
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);

        // Simula a verificação pelo Id
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // Execução
        Optional<Book> foundBook = bookService.getById(id);

        // Verificação
        assertTrue(foundBook.isPresent());
        assertTrue(foundBook.get().getId().equals(id));
        assertTrue(foundBook.get().getAuthor().equals(book.getAuthor()));
        assertTrue(foundBook.get().getTitle().equals(book.getTitle()));
        assertTrue(foundBook.get().getIsbn().equals(book.getIsbn()));
    }

    @Test
    @DisplayName("D - Deve retornar vazio ao obter um livro por ID")
    public void bookNotFoundByIDTest(){

        // Cria cenário
        Long id = 1l;

        // Simula a verificação pelo Id
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // Execução
        Optional<Book> book = bookService.getById(id);

        // Verificação
        assertFalse(book.isPresent());

    }

    @Test
    @DisplayName("E - Deve deletar um livro")
    public void deleteBookTest(){

        // Criar Cenário
        Book book = Book.builder().id(1l).build();

        // Execução
        // Não deixa executar exceção
        Assertions.assertDoesNotThrow(()-> bookService.delete(book));

        // Verificação
        verify(bookRepository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("F - Deve ocorrer erro ao tentar deletar um livro inexistente")
    public void deleteInvalidBookTest(){

        // Criar Cenário
        Book book = new Book();

        // Execução
        // Garantir que não lance nenhuma exceção
        Assertions.assertThrows(IllegalArgumentException.class, ()-> bookService.delete(book));

        // Verificação
        verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("G - Deve atualizar um livro")
    public void updateBookTest(){

        // Criar Cenário
        Long id = 1l;

        // Livro a atualizar
        Book updatingBook = Book.builder().id(id).build();

        // Simulação
        Book updatedBook = createValidBook();
        updatingBook.setId(id);
        Mockito.when(bookRepository.save(updatingBook)).thenReturn(updatedBook);

        // Execução
        Book book = bookService.update(updatingBook);

        // Verificação
        Assertions.assertEquals(book.getId(),updatedBook.getId());
        Assertions.assertEquals(book.getAuthor(), updatedBook.getAuthor());
        Assertions.assertEquals(book.getTitle(), updatedBook.getTitle());
        Assertions.assertEquals(book.getIsbn(), updatedBook.getIsbn());
    }

    @Test
    @DisplayName("H - Filtrar livros pelas propriedades")
    public void findBookTest(){

        // Cenário
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        // Execução
        Page<Book> result = bookService.find(book, pageRequest);

        // Verificação
        Assertions.assertEquals(result.getTotalElements(),1);
        Assertions.assertEquals(result.getContent(),lista);
        Assertions.assertEquals(result.getPageable().getPageNumber(),0);
        Assertions.assertEquals(result.getPageable().getPageSize(),10);
    }

    @Test
    @DisplayName("I  - Deve obter um livro por ISBN")
    public void getBookByIsbnTest(){

        // Cria cenário
        String isbn = "12345";
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder()
                .id(1l)
                .isbn(isbn)
                .build()));

        // Execução
        Optional<Book> book = bookService.getBookByIsbn(isbn);

        // Verificação
        assertTrue(book.isPresent());
        assertTrue(book.get().getId().equals(1l));
        assertTrue(book.get().getIsbn().equals(isbn));

        verify(bookRepository, times(1)).findByIsbn(isbn);
    }

    public Book createValidBook() {
        return Book.builder()
                .author("Larissa")
                .title("Minha namorada")
                .isbn("54321").build();
    }


}
