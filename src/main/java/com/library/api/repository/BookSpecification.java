package com.library.api.repository;

import com.library.api.model.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> hasTitle(String title) {
        return (root, query, cb) -> title == null ? null :
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Book> hasGenre(String genre) {
        return (root, query, cb) -> genre == null ? null :
                cb.like(cb.lower(root.get("genre")), "%" + genre.toLowerCase() + "%");
    }

    public static Specification<Book> hasAuthor(Long authorId) {
        return (root, query, cb) -> authorId == null ? null :
                cb.equal(root.get("author").get("id"), authorId);
    }
}
