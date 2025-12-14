package me.chan99k.learningmanager.member.mapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import me.chan99k.learningmanager.member.Email;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

	@Override
	public String convertToDatabaseColumn(Email email) {
		return email != null ? email.address() : null;
	}

	@Override
	public Email convertToEntityAttribute(String address) {
		return address != null ? Email.of(address) : null;
	}
}
