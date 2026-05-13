package io.github.mirvmir.catalog.web.controller;

import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.application.service.interfaces.CatalogService;
import io.github.mirvmir.catalog.domain.CatalogType;
import io.github.mirvmir.catalog.domain.Format;
import io.github.mirvmir.catalog.web.response.CatalogItemResponse;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CatalogControllerTest {

    private CatalogService catalogService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        catalogService = mock(CatalogService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CatalogController(catalogService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getCatalog_shouldReturnCatalogItems() throws Exception {
        CatalogItemResponse response = new CatalogItemResponse(
                CatalogType.ACTIVITY,
                1L,
                "Java консультация",
                "Иван Иванов",
                "Короткое описание",
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                4.8,
                Format.INDIVIDUAL
        );

        when(catalogService.getCatalog(any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/catalog")
                        .param("search", "java")
                        .param("topicId", "10")
                        .param("sectionId", "20")
                        .param("subjectId", "30")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000")
                        .param("minRating", "4.5")
                        .param("format", "INDIVIDUAL")
                        .param("type", "ACTIVITY"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].type").value("ACTIVITY"))
                .andExpect(jsonPath("$[0].sourceId").value(1L))
                .andExpect(jsonPath("$[0].title").value("Java консультация"))
                .andExpect(jsonPath("$[0].authorName").value("Иван Иванов"))
                .andExpect(jsonPath("$[0].shortDescription").value("Короткое описание"))
                .andExpect(jsonPath("$[0].priceAmount").value(1500))
                .andExpect(jsonPath("$[0].priceCurrency").value("RUB"))
                .andExpect(jsonPath("$[0].ratingAvg").value(4.8))
                .andExpect(jsonPath("$[0].format").value("INDIVIDUAL"));

        ArgumentCaptor<CatalogFilterDto> captor =
                ArgumentCaptor.forClass(CatalogFilterDto.class);

        verify(catalogService).getCatalog(captor.capture());

        CatalogFilterDto filter = captor.getValue();

        assertEquals("java", filter.search());
        assertEquals(10L, filter.topicId());
        assertEquals(20L, filter.sectionId());
        assertEquals(30L, filter.subjectId());
        assertEquals(new BigDecimal("1000"), filter.minPrice());
        assertEquals(new BigDecimal("2000"), filter.maxPrice());
        assertEquals(4.5, filter.minRating());
        assertEquals(Format.INDIVIDUAL, filter.format());
        assertEquals(CatalogType.ACTIVITY, filter.type());
    }

    @Test
    void getCatalog_shouldReturnEmptyList() throws Exception {
        when(catalogService.getCatalog(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(catalogService).getCatalog(any());
    }

    @Test
    void getCatalog_shouldReturn503_whenDatabaseUnavailable() throws Exception {
        when(catalogService.getCatalog(any()))
                .thenThrow(new CannotGetJdbcConnectionException(
                        "Database unavailable"
                ));

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("Временная ошибка подключения к базе данных"));
    }

    @Test
    void getCatalog_shouldReturn500_whenDatabaseError() throws Exception {
        when(catalogService.getCatalog(any()))
                .thenThrow(new DataAccessResourceFailureException(
                        "Database error"
                ));

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Ошибка при обращении к базе данных"));
    }
}