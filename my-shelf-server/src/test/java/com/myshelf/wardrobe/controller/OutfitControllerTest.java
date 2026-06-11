package com.myshelf.wardrobe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshelf.wardrobe.dto.OutfitDTO;
import com.myshelf.wardrobe.dto.OutfitSlotDTO;
import com.myshelf.wardrobe.entity.Category;
import com.myshelf.wardrobe.security.JwtAuthFilter;
import com.myshelf.wardrobe.security.JwtTokenProvider;
import com.myshelf.wardrobe.service.OutfitService;
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

@WebMvcTest(controllers = OutfitController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfig.class)
class OutfitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OutfitService outfitService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID userId;
    private UUID outfitId;
    private UUID itemId;
    private OutfitDTO outfitDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        outfitId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        OutfitSlotDTO slotDto = new OutfitSlotDTO(UUID.randomUUID(), itemId, Category.TOP);
        outfitDto = new OutfitDTO(outfitId, "Summer Outfit", "Casual look", List.of(slotDto));
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

    // ===== GET /api/outfits =====

    @Test
    @DisplayName("GET /api/outfits — 200 OK, список образов пользователя")
    void getUserOutfits_returnsOk() throws Exception {
        when(outfitService.getOutfitsByUserId(userId)).thenReturn(List.of(outfitDto));

        mockMvc.perform(get("/api/outfits").with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(outfitId.toString()))
                .andExpect(jsonPath("$[0].name").value("Summer Outfit"))
                .andExpect(jsonPath("$[0].slots[0].slotType").value("TOP"));

        verify(outfitService).getOutfitsByUserId(userId);
    }

    // ===== GET /api/outfits/{id} =====

    @Test
    @DisplayName("GET /api/outfits/{id} — 200 OK")
    void getOutfitById_returnsOk() throws Exception {
        when(outfitService.getOutfitById(outfitId)).thenReturn(outfitDto);

        mockMvc.perform(get("/api/outfits/{id}", outfitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(outfitId.toString()))
                .andExpect(jsonPath("$.name").value("Summer Outfit"));

        verify(outfitService).getOutfitById(outfitId);
    }

    // ===== POST /api/outfits =====

    @Test
    @DisplayName("POST /api/outfits — 201 Created")
    void createOutfit_returnsCreated() throws Exception {
        OutfitSlotDTO slotRequest = new OutfitSlotDTO(null, itemId, Category.TOP);
        OutfitDTO requestDto = new OutfitDTO(null, "New Outfit", "Description", List.of(slotRequest));
        OutfitDTO createdDto = new OutfitDTO(outfitId, "New Outfit", "Description", List.of(slotRequest));
        when(outfitService.createOutfit(eq(userId), any(OutfitDTO.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/outfits")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(outfitId.toString()))
                .andExpect(jsonPath("$.name").value("New Outfit"));

        verify(outfitService).createOutfit(eq(userId), any(OutfitDTO.class));
    }

    @Test
    @DisplayName("POST /api/outfits — 400 Bad Request при пустом названии")
    void createOutfit_blankName_returnsBadRequest() throws Exception {
        OutfitSlotDTO slotRequest = new OutfitSlotDTO(null, itemId, Category.TOP);
        OutfitDTO invalidDto = new OutfitDTO(null, "", "Description", List.of(slotRequest));

        mockMvc.perform(post("/api/outfits")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/outfits — 400 Bad Request при пустом списке слотов")
    void createOutfit_emptySlots_returnsBadRequest() throws Exception {
        OutfitDTO invalidDto = new OutfitDTO(null, "Outfit", "Description", List.of());

        mockMvc.perform(post("/api/outfits")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    // ===== PUT /api/outfits/{id} =====

    @Test
    @DisplayName("PUT /api/outfits/{id} — 200 OK")
    void updateOutfit_returnsOk() throws Exception {
        OutfitSlotDTO slotRequest = new OutfitSlotDTO(null, itemId, Category.TOP);
        OutfitDTO requestDto = new OutfitDTO(null, "Updated Outfit", "New desc", List.of(slotRequest));
        OutfitDTO updatedDto = new OutfitDTO(outfitId, "Updated Outfit", "New desc", List.of(slotRequest));
        when(outfitService.updateOutfit(eq(outfitId), eq(userId), any(OutfitDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/outfits/{id}", outfitId)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Outfit"));

        verify(outfitService).updateOutfit(eq(outfitId), eq(userId), any(OutfitDTO.class));
    }

    @Test
    @DisplayName("PUT /api/outfits/{id} — 400 Bad Request при невалидном теле")
    void updateOutfit_invalidBody_returnsBadRequest() throws Exception {
        OutfitDTO invalidDto = new OutfitDTO(null, "   ", null, List.of());

        mockMvc.perform(put("/api/outfits/{id}", outfitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    // ===== DELETE /api/outfits/{id} =====

    @Test
    @DisplayName("DELETE /api/outfits/{id} — 204 No Content")
    void deleteOutfit_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/outfits/{id}", outfitId).with(authenticatedUser()))
                .andExpect(status().isNoContent());

        verify(outfitService).deleteOutfit(outfitId, userId);
    }
}

@WebMvcTest(controllers = OutfitController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(ControllerTestSecurityConfig.class)
class OutfitControllerUnauthorizedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OutfitService outfitService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID outfitId;

    @BeforeEach
    void setUp() {
        outfitId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/outfits — 401 Unauthorized без аутентификации")
    void getUserOutfits_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/outfits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/outfits/{id} — 401 Unauthorized без аутентификации")
    void getOutfitById_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/outfits/{id}", outfitId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/outfits — 401 Unauthorized без аутентификации")
    void createOutfit_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/outfits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Outfit\",\"slots\":[{\"itemId\":\"00000000-0000-0000-0000-000000000001\",\"slotType\":\"TOP\"}]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/outfits/{id} — 401 Unauthorized без аутентификации")
    void updateOutfit_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/outfits/{id}", outfitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Outfit\",\"slots\":[{\"itemId\":\"00000000-0000-0000-0000-000000000001\",\"slotType\":\"TOP\"}]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/outfits/{id} — 401 Unauthorized без аутентификации")
    void deleteOutfit_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/outfits/{id}", outfitId))
                .andExpect(status().isUnauthorized());
    }
}
