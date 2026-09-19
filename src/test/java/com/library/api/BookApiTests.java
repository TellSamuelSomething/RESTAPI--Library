package com.library.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookApiTests extends ApiTestSupport {

    @Test
    void createdBook_canBeFetchedWithItsAuthor() throws Exception {
        String user = userToken();
        long authorId = createAuthor(user, "Tove Jansson");
        long bookId = createBook(user, "Trollvinter", "Fantasy", authorId);

        mvc.perform(get("/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Trollvinter"))
                .andExpect(jsonPath("$.author.name").value("Tove Jansson"));
    }

    @Test
    void createBook_withUnknownAuthor_returns404() throws Exception {
        mvc.perform(post("/books").header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Orphan\",\"genre\":\"Drama\",\"authorId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBook_withoutTitle_returns400() throws Exception {
        String user = userToken();
        long authorId = createAuthor(user, "Author " + UUID.randomUUID());

        mvc.perform(post("/books").header("Authorization", "Bearer " + user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"genre\":\"Drama\",\"authorId\":" + authorId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void list_isPaged() throws Exception {
        String user = userToken();
        long authorId = createAuthor(user, "Paged " + UUID.randomUUID());
        for (int i = 1; i <= 3; i++) createBook(user, "Paged book " + i, "Test", authorId);

        mvc.perform(get("/books").param("authorId", String.valueOf(authorId)).param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void list_withUnknownSortField_returns400() throws Exception {
        mvc.perform(get("/books").param("sort", "password"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_withInvalidPaging_returns400() throws Exception {
        mvc.perform(get("/books").param("size", "100000")).andExpect(status().isBadRequest());
        mvc.perform(get("/books").param("size", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/books").param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/books/search").param("size", "100000")).andExpect(status().isBadRequest());
    }

    @Test
    void search_filtersByTitleAndGenre() throws Exception {
        String user = userToken();
        String marker = "Marker" + UUID.randomUUID().toString().substring(0, 8);
        long authorId = createAuthor(user, "Search " + marker);
        createBook(user, "The " + marker + " Files", "Mystery", authorId);
        createBook(user, "Other book", "Comedy", authorId);

        mvc.perform(get("/books/search").param("title", marker.toLowerCase()).param("genre", "mystery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].genre").value("Mystery"));
    }

    @Test
    void deletedBook_disappears() throws Exception {
        String admin = adminToken();
        long authorId = createAuthor(admin, "Gone " + UUID.randomUUID());
        long bookId = createBook(admin, "Short lived", "Drama", authorId);

        mvc.perform(delete("/books/" + bookId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());

        mvc.perform(get("/books/" + bookId)).andExpect(status().isNotFound());
        mvc.perform(delete("/books/" + bookId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingAnAuthorWithBooks_removesTheAuthorAndTheirBooks() throws Exception {
        String admin = adminToken();
        long authorId = createAuthor(admin, "Prolific " + UUID.randomUUID());
        long bookId = createBook(admin, "Their only book", "Drama", authorId);

        mvc.perform(delete("/authors/" + authorId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());

        mvc.perform(get("/authors/" + authorId)).andExpect(status().isNotFound());
        mvc.perform(get("/books/" + bookId)).andExpect(status().isNotFound());
    }
}
