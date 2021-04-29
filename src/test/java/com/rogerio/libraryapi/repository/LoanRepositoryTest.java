package com.rogerio.libraryapi.repository;

import com.rogerio.libraryapi.entity.Book;
import com.rogerio.libraryapi.entity.Loan;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

// *** TESTES DE INTEGRAÇÃO ***

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository loanRepository;

    @Test
    @DisplayName("A - Deve verificar se existe empréstimo não devolvido para o livro")
    public void existsByBookAndNotReturnedTest(){

        // Cenário, persistir dados
        Loan loan = createAndPersistLoan();
        Book book = loan.getBook();

        // Execução
        boolean exists = loanRepository.existsByBookAndNotReturned(book);

        // Verificação
        Assertions.assertTrue(exists);

    }

    @Test
    @DisplayName("B - Deve buscar empréstimo pelo ISBN do livro ou customer")
    public void findByBookIsbnOrCustomer(){
        // Cenário, persistir dados
        Loan loan = createAndPersistLoan();

        Page<Loan> result = loanRepository.findByBookIsbnOrCustomer(
                "1234",
                "Rogério",
                PageRequest.of(0, 10));

        // Verificação
        Assertions.assertTrue(result.getContent().contains(loan));
        Assertions.assertEquals(result.getContent().size(), 1);
        Assertions.assertEquals(result.getPageable().getPageSize(), 10);
        Assertions.assertEquals(result.getPageable().getPageNumber(), 0);
        Assertions.assertEquals(result.getTotalElements(), 1);

    }

    @Test
    @DisplayName("C - Deve obter emprestimo cuja data do emprestimo for menor ou igual a três dias atras e não retornar")
    public void findByLoanDateLessThanAndNotReturned(){
        Loan loan = createAndPersistLoanWithDate(LocalDate.now().minusDays(5));

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertEquals(result.size(), 1);
        Assertions.assertTrue(result.contains(loan));

    }

    @Test
    @DisplayName("D - Não Deve obter emprestimo cuja data do emprestimo for menor ou igual a três dias atras e não retornar")
    public void notFindByLoanDateLessThanAndNotReturned(){
        Loan loan = createAndPersistLoanWithDate(LocalDate.now());

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertTrue(result.isEmpty());

    }


    private Book createNewBook(String isbn) {
        return Book.builder().author("Rogério").title("Meu sonho").isbn(isbn).build();
    }

    public Loan createAndPersistLoan(){
        // Cenário, persistir dados
        Book book = createNewBook("1234");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);
        return loan;
    }

    public Loan createAndPersistLoanWithDate(LocalDate loanDate){
        // Cenário, persistir dados com data
        Book book = createNewBook("1234");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
        entityManager.persist(loan);
        return loan;
    }

}
