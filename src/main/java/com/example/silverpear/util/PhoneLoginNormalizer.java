package com.example.silverpear.util;

/**
 * Единый формат логина по телефону: «+» и только цифры кода/абонента.
 * Учитывает частые варианты ввода при входе (не совпадают с тем, что сохранили при регистрации).
 */
public final class PhoneLoginNormalizer {

    private PhoneLoginNormalizer() {
    }

    public static String toLogin(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = raw.replace('\u00A0', ' ').trim();
        String digits = normalized.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return "";
        }
        digits = normalizeNationalTrunk(digits);
        return "+" + digits;
    }

    /**
     * 8 029 … (11 цифр, 80…) → 375 29 … — как при регистрации через +375.
     * 8 916 … (11 цифр, 89…) → 7 916 … для РФ.
     */
    private static String normalizeNationalTrunk(String digits) {
        if (digits.length() == 11 && digits.startsWith("80")) {
            return "375" + digits.substring(2);
        }
        if (digits.length() == 11 && digits.startsWith("89")) {
            return "7" + digits.substring(1);
        }
        return digits;
    }
}
