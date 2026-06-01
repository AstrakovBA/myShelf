package com.myshelf.wardrobe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshelf.wardrobe.dto.ItemDTO;
import com.myshelf.wardrobe.entity.Category;
import com.myshelf.wardrobe.entity.Season;
import com.myshelf.wardrobe.security.JwtAuthFilter;
import com.myshelf.wardrobe.security.JwtTokenProvider;
import com.myshelf.wardrobe.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfig.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID userId;
    private UUID itemId;
    private ItemDTO itemDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        itemDto = new ItemDTO(
                itemId,
                "T-Shirt",
                "White cotton T-shirt",
                "http://example.com/tshirt.jpg",
                Category.TOP,
                Season.SUMMER
        );
    }

    private UsernamePasswordAuthenticationToken userAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private RequestPostProcessor authenticatedUser() {
        return request -> {
            UsernamePasswordAuthenticationToken auth = userAuthentication();
            request.setUserPrincipal(auth);
            return request;
        };
    }

    // ===== GET /api/items =====

    @Test
    @DisplayName("GET /api/items — 200 OK, список вещей пользователя")
    void getUserItems_returnsOk() throws Exception {
        when(itemService.getItemsByUserId(userId)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/api/items").with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId.toString()))
                .andExpect(jsonPath("$[0].name").value("T-Shirt"))
                .andExpect(jsonPath("$[0].category").value("TOP"));

        verify(itemService).getItemsByUserId(userId);
    }

    // ===== GET /api/items/{id} =====

    @Test
    @DisplayName("GET /api/items/{id} — 200 OK")
    void getItemById_returnsOk() throws Exception {
        when(itemService.getItemById(itemId)).thenReturn(itemDto);

        mockMvc.perform(get("/api/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.name").value("T-Shirt"));

        verify(itemService).getItemById(itemId);
    }

    // ===== POST /api/items =====

    @Test
    @DisplayName("POST /api/items — 201 Created")
    void createItem_returnsCreated() throws Exception {
        ItemDTO requestDto = new ItemDTO(null, "New Item", "Description", null, Category.TOP, Season.WINTER);
        ItemDTO createdDto = new ItemDTO(itemId, "New Item", "Description", null, Category.TOP, Season.WINTER);
        when(itemService.createItem(eq(userId), any(ItemDTO.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/items")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.name").value("New Item"));

        verify(itemService).createItem(eq(userId), any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /api/items — 400 Bad Request при невалидном теле")
    void createItem_invalidBody_returnsBadRequest() throws Exception {
        ItemDTO invalidDto = new ItemDTO(null, "", null, null, null, null);

        mockMvc.perform(post("/api/items")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    // ===== PUT /api/items/{id} =====

    @Test
    @DisplayName("PUT /api/items/{id} — 200 OK")
    void updateItem_returnsOk() throws Exception {
        ItemDTO requestDto = new ItemDTO(null, "Updated", "New desc", null, Category.BOTTOM, Season.AUTUMN);
        ItemDTO updatedDto = new ItemDTO(itemId, "Updated", "New desc", null, Category.BOTTOM, Season.AUTUMN);
        when(itemService.updateItem(eq(itemId), any(ItemDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(itemService).updateItem(eq(itemId), any(ItemDTO.class));
    }

    @Test
    @DisplayName("PUT /api/items/{id} — 400 Bad Request при невалидном теле")
    void updateItem_invalidBody_returnsBadRequest() throws Exception {
        ItemDTO invalidDto = new ItemDTO(null, "   ", null, null, null, null);

        mockMvc.perform(put("/api/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    // ===== DELETE /api/items/{id} =====

    @Test
    @DisplayName("DELETE /api/items/{id} — 204 No Content")
    void deleteItem_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", itemId))
                .andExpect(status().isNoContent());

        verify(itemService).deleteItem(itemId);
    }
}

@WebMvcTest(controllers = ItemController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(ControllerTestSecurityConfig.class)
class ItemControllerUnauthorizedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID itemId;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/items — 401 Unauthorized без аутентификации")
    void getUserItems_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/items/{id} — 401 Unauthorized без аутентификации")
    void getItemById_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/items/{id}", itemId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/items — 401 Unauthorized без аутентификации")
    void createItem_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Item\",\"category\":\"TOP\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/items/{id} — 401 Unauthorized без аутентификации")
    void updateItem_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"category\":\"TOP\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/items/{id} — 401 Unauthorized без аутентификации")
    void deleteItem_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", itemId))
                .andExpect(status().isUnauthorized());
    }
}
