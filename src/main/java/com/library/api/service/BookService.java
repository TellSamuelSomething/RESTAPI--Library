package com.library.api.service;

import com.library.api.model.Book;
import com.library.api.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> getAll() {
        return repository.findAll();
    }

    public Optional<Book> getById(Long id) {
        return repository.findById(id);
    }

    public List<Book> getByAuthor(String author) {
        return repository.findByAuthorContainingIgnoreCase(author);
    }

    public Book create(Book book) {
        return repository.save(book);
    }

    public Optional<Book> update(Long id, Book updated) {
        return repository.findById(id).map(book -> {
            book.setTitle(updated.getTitle());
            book.setAuthor(updated.getAuthor());
            book.setGenre(updated.getGenre());
            return repository.save(book);
        });
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
