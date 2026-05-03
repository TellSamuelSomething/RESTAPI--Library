package com.library.api.service;

import com.library.api.dto.AuthorRequest;
import com.library.api.dto.AuthorResponse;
import com.library.api.exception.AuthorNotFoundException;
import com.library.api.model.Author;
import com.library.api.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository repository;

    public AuthorService(AuthorRepository repository) {
        this.repository = repository;
    }

    public List<AuthorResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public AuthorResponse getById(Long id) {
        return toResponse(repository.findById(id).orElseThrow(() -> new AuthorNotFoundException(id)));
    }

    public AuthorResponse create(AuthorRequest request) {
        Author author = new Author();
        author.setName(request.getName());
        author.setNationality(request.getNationality());
        return toResponse(repository.save(author));
    }

    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = repository.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
        author.setName(request.getName());
        author.setNationality(request.getNationality());
        return toResponse(repository.save(author));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw new AuthorNotFoundException(id);
        repository.deleteById(id);
    }

    public Author findEntityById(Long id) {
        return repository.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName(), author.getNationality());
    }
}
