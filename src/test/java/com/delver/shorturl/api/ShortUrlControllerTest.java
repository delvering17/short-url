package com.delver.shorturl.api;

import com.delver.shorturl.api.dto.ModifyShortenUrlRequest;
import com.delver.shorturl.api.dto.ShortUrlStatsResponse;
import com.delver.shorturl.api.dto.ShortenUrlResponse;
import com.delver.shorturl.application.ShortCodeGenerator;
import com.delver.shorturl.domain.ShortUrl;
import com.delver.shorturl.domain.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ShortCodeGenerator  shortCodeGenerator;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Test
    void shorten() throws Exception {
        String longUrl = "https://www.google.com";

        ResultActions actions = mockMvc.perform(
                        post("/api/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(new ShortenUrlRequest(longUrl)))
        );

        actions.andExpect(status().is(HttpStatus.CREATED.value()));
        MvcResult result = actions.andReturn();
        ShortenUrlResponse response = jsonMapper.readValue(
                result.getResponse().getContentAsString(),
                ShortenUrlResponse.class
        );

        assertThat(response).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-url", "ftp://example.com", "https://"})
    void rejectsInvalidLongUrlOnCreation(String longUrl) throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ShortenUrlRequest(longUrl))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("longUrl"))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    void accessShortUrl() throws Exception {
        ShortUrl shortUrl = createShortUrl("https://www.google.com");

        ResultActions actions = mockMvc.perform(
                get("/api/shorten/" + shortUrl.getShortCode())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        actions.andExpect(status().is(HttpStatus.OK.value()));
        MvcResult result = actions.andReturn();
        ShortenUrlResponse response = jsonMapper.readValue(
                result.getResponse().getContentAsString(),
                ShortenUrlResponse.class
        );

        assertThat(response.originalUrl()).isEqualTo(shortUrl.getOriginalUrl());
        assertThat(response.shortCode()).isEqualTo(shortUrl.getShortCode());
    }

    @Test
    void getShortUrlStatus() throws Exception {
        ShortUrl shortUrl = createShortUrl("https://www.google.com");
        for (int i = 0; i < 5; i++) {
            accessShortUrl(shortUrl.getShortCode());
        }

        ResultActions actions = mockMvc.perform(
                get("/api/shorten/" + shortUrl.getShortCode() + "/stats")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        actions.andExpect(status().is(HttpStatus.OK.value()));
        MvcResult result = actions.andReturn();
        ShortUrlStatsResponse response = jsonMapper.readValue(
                result.getResponse().getContentAsString(),
                ShortUrlStatsResponse.class
        );

        assertThat(response.accessCount()).isEqualTo(5);
        assertThat(response.lastAccessedAt()).isNotNull();
    }

    @Test
    void modifyOriginalUrl() throws Exception {
        ShortUrl shortUrl = createShortUrl("https://www.google.com");
        String replaceUrl = "http://www.naver.com";

        ResultActions actions = mockMvc.perform(
                put("/api/shorten/" + shortUrl.getShortCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ModifyShortenUrlRequest(replaceUrl)))
        );

        actions.andExpect(status().is(HttpStatus.OK.value()));
        MvcResult result = actions.andReturn();
        ShortenUrlResponse response = jsonMapper.readValue(
                result.getResponse().getContentAsString(),
                ShortenUrlResponse.class
        );

        assertThat(response.originalUrl()).isEqualTo(replaceUrl);
        assertThat(response.shortCode()).isEqualTo(shortUrl.getShortCode());
    }

    @Test
    void rejectsInvalidLongUrlOnUpdate() throws Exception {
        ShortUrl shortUrl = createShortUrl("https://www.google.com");

        mockMvc.perform(put("/api/shorten/{shortCode}", shortUrl.getShortCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ModifyShortenUrlRequest("not-a-url"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("longUrl"));

        ShortUrl savedUrl = shortUrlRepository.findByShortCode(shortUrl.getShortCode()).orElseThrow();
        assertThat(savedUrl.getOriginalUrl()).isEqualTo(shortUrl.getOriginalUrl());
    }

    @Test
    void deleteShortUrl() throws Exception {
        ShortUrl shortUrl = createShortUrl("https://www.google.com");

        ResultActions actions = mockMvc.perform(
                delete("/api/shorten/" + shortUrl.getShortCode())
                        .contentType(MediaType.APPLICATION_JSON)
        );
        actions.andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        assertThat(shortUrlRepository.findByShortCode(shortUrl.getShortCode()).isEmpty()).isTrue();
    }

    private ShortUrl createShortUrl(String longUrl) {
        ShortUrl shortUrl = ShortUrl.create(longUrl, shortCodeGenerator.generate());
        return shortUrlRepository.save(shortUrl);
    }

    private void accessShortUrl(String shortCode) throws Exception {
        mockMvc.perform(get("/api/shorten/" + shortCode).contentType(MediaType.APPLICATION_JSON));
    }

}
