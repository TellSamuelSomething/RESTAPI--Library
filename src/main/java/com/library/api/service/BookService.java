package com.library.api.service;

import com.library.api.dto.AuthorResponse;
import com.library.api.dto.BookRequest;
import com.library.api.dto.BookResponse;
import com.library.api.dto.PageResponse;
import com.library.api.exception.BookNotFoundException;
import com.library.api.model.Book;
import com.library.api.repository.BookRepository;
import com.library.api.repository.BookSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;
    private final AuthorService authorService;

    public BookService(BookRepository repository, AuthorService authorService) {
        this.repository = repository;
        this.authorService = authorService;
    }

    public PageResponse<BookResponse> getAll(int page, int size, String sort) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sort));
        return PageResponse.from(repository.findAll(pageable).map(this::toResponse));
    }

    public BookResponse getById(Long id) {
        return toResponse(repository.findById(id).orElseThrow(() -> new BookNotFoundException(id)));
    }

    public List<BookResponse> getByAuthor(Long authorId) {
        return repository.findByAuthorId(authorId).stream().map(this::toResponse).toList();
    }

    public PageResponse<BookResponse> search(String title, String genre, Long authorId, int page, int size) {
        Specification<Book> spec = Specification
                .where(BookSpecification.hasTitle(title))
                .and(BookSpecification.hasGenre(genre))
                .and(BookSpecification.hasAuthor(authorId));
        PageRequest pageable = PageRequest.of(page, size);
        return PageResponse.from(repository.findAll(spec, pageable).map(this::toResponse));
    }

    public BookResponse create(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setGenre(request.getGenre());
        book.setAuthor(authorService.findEntityById(request.getAuthorId()));
        return toResponse(repository.save(book));
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        book.setTitle(request.getTitle());
        book.setGenre(request.getGenre());
        book.setAuthor(authorService.findEntityById(request.getAuthorId()));
        return toResponse(repository.save(book));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw new BookNotFoundException(id);
        repository.deleteById(id);
    }

    private BookResponse toResponse(Book book) {
        AuthorResponse authorResponse = book.getAuthor() == null ? null :
                new AuthorResponse(book.getAuthor().getId(), book.getAuthor().getName(), book.getAuthor().getNationality());
        return new BookResponse(book.getId(), book.getTitle(), book.getGenre(), authorResponse);
    }
}
