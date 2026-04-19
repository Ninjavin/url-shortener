package com.example.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.model.ShortUrl;
import com.example.backend.repository.ShortUrlRepository;

@Service
public class ShortUrlService {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final ShortUrlRepository shortUrlRepository;
    private final CounterAllocator counterAllocator;

    @Autowired
    public ShortUrlService(ShortUrlRepository shortUrlRepository, CounterAllocator counter) {
        this.shortUrlRepository = shortUrlRepository;
        this.counterAllocator = counter;
    }

    public ShortUrl shortenUrl(String longUrl) {
        // Long counter = stringRedisTemplate.opsForValue().increment(COUNTER_KEY);
        long id = counterAllocator.nextId();
        String shortCode = encodeBase62(id);
        ShortUrl shortUrl = new ShortUrl(shortCode, longUrl);
        return shortUrlRepository.save(shortUrl);
    }

    public String getLongUrl(String shortCode) {
        Optional<ShortUrl> shortCodeExists = shortUrlRepository.findByShortCode(shortCode);

        if (shortCodeExists.isPresent()) {
            return shortCodeExists.get().getLongUrl();
        }

        return "";
    }

    private String encodeBase62(long value) {
        if (value == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder encoded = new StringBuilder();
        int base = ALPHABET.length();
        long current = value;

        while (current > 0) {
            int index = (int) (current % base);
            encoded.append(ALPHABET.charAt(index));
            current /= base;
        }

        return encoded.reverse().toString();
    }

}
