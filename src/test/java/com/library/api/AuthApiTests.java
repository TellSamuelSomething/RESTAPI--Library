package com.library.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTests extends ApiTestSupport {

    private static String credentials(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void register_createsRegularUserWithToken() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("user-" + UUID.randomUUID(), "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_ignoresARequestedAdminRole() throws Exception {
        String body = "{\"username\":\"sneaky-" + UUID.randomUUID() + "\",\"password\":\"password123\",\"role\":\"admin\"}";

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_withTakenUsername_returns409() throws Exception {
        String username = "taken-" + UUID.randomUUID();
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(credentials(username, "password123"))).andExpect(status().isOk());

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "password123")))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withShortPassword_returns400() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("user-" + UUID.randomUUID(), "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void login_withCorrectPassword_returnsToken() throws Exception {
        String username = "login-" + UUID.randomUUID();
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(credentials(username, "password123"))).andExpect(status().isOk());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        String username = "wrong-" + UUID.randomUUID();
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(credentials(username, "password123"))).andExpect(status().isOk());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "not-the-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mvc.perform(post("/authors").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withInvalidToken_returns401() throws Exception {
        mvc.perform(post("/authors").header("Authorization", "Bearer not.a.token")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpoint_withInvalidToken_stillWorks() throws Exception {
        mvc.perform(get("/books").header("Authorization", "Bearer not.a.token"))
                .andExpect(status().isOk());
    }

    @Test
    void regularUser_cannotDelete() throws Exception {
        String user = userToken();
        long authorId = createAuthor(user, "Astrid Lindgren");

        mvc.perform(delete("/authors/" + authorId).header("Authorization", "Bearer " + user))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_canDelete() throws Exception {
        String admin = adminToken();
        long authorId = createAuthor(admin, "Selma Lagerlof");
        long bookId = createBook(admin, "Nils Holgersson", "Fantasy", authorId);

        mvc.perform(delete("/books/" + bookId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }
}
