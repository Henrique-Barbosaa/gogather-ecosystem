package com.role.net.tripmaker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.text.Normalizer;

public enum ExpenseCategory {
    GASOLINA,
    PEDAGIO,
    TRANSPORTE,
    HOSPEDAGEM,
    ALIMENTACAO,
    PASSEIOS,
    LAZER,
    OUTROS;

    @JsonCreator
    public static ExpenseCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return OUTROS;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toUpperCase()
            .trim();
        return switch (normalized) {
            case "HOSPEDAGEM" -> HOSPEDAGEM;
            case "ALIMENTACAO", "REFEICAO", "COMIDA" -> ALIMENTACAO;
            case "TRANSPORTE", "GASOLINA", "PEDAGIO", "VOO", "UBER", "TAXI" -> TRANSPORTE;
            case "LAZER", "PASSEIOS", "FESTA", "SHOW" -> LAZER;
            default -> OUTROS;
        };
    }

    @JsonValue
    public String getDisplayName() {
        return switch (this) {
            case HOSPEDAGEM -> "Hospedagem";
            case ALIMENTACAO -> "Alimentação";
            case TRANSPORTE, GASOLINA, PEDAGIO -> "Transporte";
            case LAZER, PASSEIOS -> "Lazer";
            case OUTROS -> "Outros";
        };
    }
}
