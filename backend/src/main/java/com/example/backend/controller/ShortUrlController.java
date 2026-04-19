package com.example.backend.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ShortenRequest;
import com.example.backend.dto.ShortenResponse;
import com.example.backend.model.ShortUrl;
import com.example.backend.service.ShortUrlService;

@RestController
@RequestMapping("/api/v1")
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> createShortUrl(@RequestBody ShortenRequest request) {
        ShortUrl shortUrl = shortUrlService.shortenUrl(request.longUrl());
        ShortenResponse response = new ShortenResponse(shortUrl.getShortCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> getLongUrl(@PathVariable String shortCode) {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        System.out.println(longUrl);
        if (longUrl.isEmpty() || longUrl == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();
        }
    }
}
