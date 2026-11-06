package com.anondev.platform.integration.ai;

public interface AiSanitizerService {
    /**
     * Sanitizes raw text to redact PII (phone numbers, email addresses, external profile links, universities)
     * adhering to BR-03 and anti-circumvention policies.
     *
     * @param rawText original text
     * @return redacted sanitized text with [REDACTED_BY_ANONDEV] placeholders
     */
    String sanitizeText(String rawText);
}
