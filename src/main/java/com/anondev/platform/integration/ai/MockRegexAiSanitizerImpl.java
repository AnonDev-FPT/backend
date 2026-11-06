package com.anondev.platform.integration.ai;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Primary
public class MockRegexAiSanitizerImpl implements AiSanitizerService {

    private static final String REDACTED_TAG = "[REDACTED_BY_ANONDEV]";

    // Regex for Vietnamese phone numbers (BR-03)
    private static final Pattern PHONE_PATTERN = Pattern.compile("(84|0[35789])\\d{8}\\b");

    // Regex for email addresses
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    // Regex for direct social/contact URLs (LinkedIn, Facebook, Zalo, Telegram)
    private static final Pattern CONTACT_LINK_PATTERN = Pattern.compile("https?://(www\\.)?(linkedin\\.com|facebook\\.com|fb\\.com|zalo\\.me|t\\.me)/[a-zA-Z0-9_.-]+", Pattern.CASE_INSENSITIVE);

    @Override
    public String sanitizeText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return rawText;
        }

        String sanitized = rawText;
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll(REDACTED_TAG);
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll(REDACTED_TAG);
        sanitized = CONTACT_LINK_PATTERN.matcher(sanitized).replaceAll(REDACTED_TAG);

        return sanitized;
    }
}
