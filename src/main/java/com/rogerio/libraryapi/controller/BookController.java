package com.rogerio.libraryapi.controller;

import com.rogerio.libraryapi.dto.BookDTO;
import com.rogerio.libraryapi.dto.LoanDTO;
import com.rogerio.libraryapi.entity.Book;
import com.rogerio.libraryapi.entity.Loan;
import com.rogerio.libraryapi.service.BookService;
import com.rogerio.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j                          // Objeto de log
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;
    private final LoanService loanService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Criar um livro")
    public BookDTO create(@RequestBody @Valid BookDTO bookDTO){

        log.info("Criando um livro para o isbn: {}", bookDTO.getIsbn());

        Book entity = modelMapper.map(bookDTO, Book.class);

        entity = bookService.save(entity);

        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Obter um livro por ID")
    public BookDTO get(@PathVariable Long id){

        log.info("Obtendo um livro pelo Id:", id);

        return bookService
                .getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Excluir um livro por Id")
    @ApiResponses({
            @ApiResponse(
                    code = 204, message = "Livro excluido com sucesso!"
            )
    })
    public void delete(@PathVariable Long id){

        log.info("Excluindo um livro pelo Id: {}", id);

        Book book = bookService.getById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Editar um livro por Id")
    public BookDTO update(@PathVariable Long id, BookDTO dto){

        log.info("Atualizando um livro pelo Id: {}", dto.getId());

        Book book = bookService.getById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        book.setAuthor(dto.getAuthor());
        book.setTitle(dto.getTitle());
        book = bookService.update(book);

        return modelMapper.map(book, BookDTO.class);
    }

    @GetMapping
    @ApiOperation("Buscar livros por parâmetros")
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest){

        log.info("Buscando livros pelos parâmetros : {}", dto.getIsbn());

        Book filter = modelMapper.map(dto, Book.class);

        Page<Book> result = bookService.find(filter, pageRequest);

        List<BookDTO> list = result.getContent().stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
    }

    // Mapeando um subrecurso
    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable){
        Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        List<LoanDTO> list = result.getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }
}
