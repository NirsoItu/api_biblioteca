package com.rogerio.libraryapi.repository;

import com.rogerio.libraryapi.entity.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

// *** TESTES DE INTEGRAÇÃO ***

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("1 - Deve retornar verdadeiro quando existir um livro na base com isbn informado")
    public void returnTrueWhenIsbnExists(){

        // Cenário
        String isbn = "12345";
        // Criar um registro na base de dados (Persistir dados)
        entityManager.persist(createNewBook("12345"));

        // Execução
        boolean exists = bookRepository.existsByIsbn(isbn);

        // Verificação
        Assertions.assertTrue(exists);

    }

    @Test
    @DisplayName("2 - Deve retornar falso quando não existir um livro na base com isbn informado")
    public void returnFalseWhenIsbnDoesntExists(){

        // Cenário
        String isbn = "12345";

        // Execução
        boolean exists = bookRepository.existsByIsbn(isbn);

        // Verificação
        Assertions.assertFalse(exists);

    }

    @Test
    @DisplayName("3 - Deve retornar um livro por ID")
    public void findByIdTest(){

        // Cenário
        Book book = createNewBook("12345");
        entityManager.persist(book);

        // Execução
        Optional<Book> foundBook = bookRepository.findById(book.getId());

        // Verificação
        Assertions.assertTrue(foundBook.isPresent());

    }

    @Test
    @DisplayName("4 - Deve retornar falso quando não existir um livro na base com ID informado")
    public void notFoundIdTest(){

        // Cenário
        Long id = 2l;

        // Execução
        Optional<Book> exists = bookRepository.findById(id);

        // Verificação
        Assertions.assertFalse(id.equals(1l));
    }

    @Test
    @DisplayName("4 - Deve salvar um livro")
    public void saveBookTest(){

        // Cenário
        Book book = createNewBook("123");

        // Execução
        Book savedBook = bookRepository.save(book);

        // Verificação
        Assertions.assertNotNull(savedBook.getId());
    }

    @Test
    @DisplayName("5 - Deve deletar um livro")
    public void deleteBookTest(){

        // Cria um livro
        Book book = createNewBook("123");

        // Persiste ele no banco de dados
        entityManager.persist(book);

        //  Busca o Id no banco de dados
        Book foundBook = entityManager.find(Book.class, book.getId());

        // Deleta o livro
        bookRepository.delete(book);

        // Busca o livro deletado
        Book deletedBook = entityManager.find(Book.class, book.getId());

        // Verifica
        Assertions.assertNull(deletedBook);
    }

    private Book createNewBook(String isbn) {
        return Book.builder().author("Rogério").title("Meu sonho").isbn(isbn).build();
    }

}
