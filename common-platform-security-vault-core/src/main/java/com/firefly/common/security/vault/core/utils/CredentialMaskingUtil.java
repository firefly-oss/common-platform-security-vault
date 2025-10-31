/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.common.security.vault.core.utils;

import java.util.regex.Pattern;

/**
 * Utility class for masking sensitive credential data in logs and displays
 * 
 * Security Best Practices:
 * - Never log sensitive data in plain text
 * - Mask all but first/last few characters
 * - Detect and mask common credential patterns
 * - Provide safe string representations for debugging
 */
public class CredentialMaskingUtil {

    private static final String MASK_CHAR = "*";
    private static final int VISIBLE_PREFIX_LENGTH = 4;
    private static final int VISIBLE_SUFFIX_LENGTH = 4;
    private static final int MIN_LENGTH_FOR_MASKING = 10;
    
    // Patterns for detecting sensitive data
    private static final Pattern API_KEY_PATTERN = Pattern.compile(
        "(?i)(api[_-]?key|apikey|access[_-]?key|secret[_-]?key)[:=]\\s*['\"]?([a-zA-Z0-9_\\-\\.]+)['\"]?");
    private static final Pattern AWS_KEY_PATTERN = Pattern.compile(
        "(?i)(AKIA[0-9A-Z]{16}|aws[_-]?secret[_-]?access[_-]?key)");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "(?i)(password|passwd|pwd)[:=]\\s*['\"]?([^\\s'\"]+)['\"]?");
    private static final Pattern JWT_PATTERN = Pattern.compile(
        "eyJ[a-zA-Z0-9_-]*\\.eyJ[a-zA-Z0-9_-]*\\.[a-zA-Z0-9_-]*");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
        "\\b(?:\\d[ -]*?){13,16}\\b");

    /**
     * Mask a credential value for safe display/logging
     * Shows first 4 and last 4 characters, masks the rest
     * 
     * @param value The credential value to mask
     * @return Masked value
     */
    public static String maskCredential(String value) {
        if (value == null || value.isEmpty()) {
            return "***";
        }
        
        if (value.length() < MIN_LENGTH_FOR_MASKING) {
            return MASK_CHAR.repeat(value.length());
        }
        
        String prefix = value.substring(0, VISIBLE_PREFIX_LENGTH);
        String suffix = value.substring(value.length() - VISIBLE_SUFFIX_LENGTH);
        int maskedLength = value.length() - VISIBLE_PREFIX_LENGTH - VISIBLE_SUFFIX_LENGTH;
        
        return prefix + MASK_CHAR.repeat(maskedLength) + suffix;
    }

    /**
     * Mask only the middle portion, showing more of prefix/suffix
     * Useful for API keys where the prefix indicates the provider
     * 
     * @param value The value to mask
     * @param visibleLength Number of characters to show on each end
     * @return Masked value
     */
    public static String maskMiddle(String value, int visibleLength) {
        if (value == null || value.isEmpty()) {
            return "***";
        }
        
        if (value.length() <= visibleLength * 2) {
            return MASK_CHAR.repeat(value.length());
        }
        
        String prefix = value.substring(0, visibleLength);
        String suffix = value.substring(value.length() - visibleLength);
        int maskedLength = value.length() - (visibleLength * 2);
        
        return prefix + MASK_CHAR.repeat(maskedLength) + suffix;
    }

    /**
     * Completely mask a value - useful for highly sensitive data
     * 
     * @param value The value to mask
     * @return Fully masked string
     */
    public static String maskFull(String value) {
        if (value == null || value.isEmpty()) {
            return "***";
        }
        return "[REDACTED-" + value.length() + "-chars]";
    }

    /**
     * Mask sensitive data in a full string (e.g., log message or JSON)
     * Detects common patterns and masks them
     * 
     * @param text Text potentially containing sensitive data
     * @return Text with sensitive data masked
     */
    public static String maskSensitivePatterns(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text;
        
        // Mask API keys
        result = API_KEY_PATTERN.matcher(result).replaceAll("$1:***MASKED***");
        
        // Mask AWS keys
        result = AWS_KEY_PATTERN.matcher(result).replaceAll("***AWS_KEY_MASKED***");
        
        // Mask passwords
        result = PASSWORD_PATTERN.matcher(result).replaceAll("$1:***MASKED***");
        
        // Mask JWTs
        result = JWT_PATTERN.matcher(result).replaceAll("***JWT_MASKED***");
        
        // Mask credit cards
        result = CREDIT_CARD_PATTERN.matcher(result).replaceAll("***CARD_MASKED***");
        
        return result;
    }

    /**
     * Create a safe string representation for an object containing credentials
     * 
     * @param className The class name
     * @param id The object ID (if any)
     * @return Safe string representation
     */
    public static String safeToString(String className, Object id) {
        return String.format("%s[id=%s, ***SENSITIVE_DATA_MASKED***]", className, id);
    }

    /**
     * Hash a credential for comparison without exposing the value
     * Uses SHA-256 for one-way hashing
     * 
     * @param value The value to hash
     * @return Hex-encoded hash
     */
    public static String hashForComparison(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "HASH_ERROR";
        }
    }

    /**
     * Validate if a string appears to be sensitive (for automatic masking)
     * 
     * @param value The value to check
     * @return True if appears sensitive
     */
    public static boolean appearsSensitive(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        // Check for common sensitive patterns
        return API_KEY_PATTERN.matcher(value).find() ||
               AWS_KEY_PATTERN.matcher(value).find() ||
               PASSWORD_PATTERN.matcher(value).find() ||
               JWT_PATTERN.matcher(value).find() ||
               CREDIT_CARD_PATTERN.matcher(value).find();
    }
}
