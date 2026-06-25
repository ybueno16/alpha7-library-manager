package br.com.yuri.alpha7.infra.persistence.converter;

import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converte o Value Object {@link ISBN} para {@link String} no banco e vice-versa.
 */
@Converter(autoApply = true)
public class IsbnConverter implements AttributeConverter<ISBN, String> {

    @Override
    public String convertToDatabaseColumn(ISBN isbn) {
        if (isbn == null) return null;
        return isbn.getValue();
    }

    @Override
    public ISBN convertToEntityAttribute(String value) {
        if (value == null) return null;
        return new ISBN(value);
    }
}
