package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class PhoneNumber implements ValueObject {

    private static final String E164_LIKE_REGEX = "\\+?[0-9]{7,15}";

    private String value;

    protected PhoneNumber() {
        // for ORM
    }

    public PhoneNumber(final String value) {
        Preconditions.nonEmpty(value, "Phone number is required");
        final String normalized = value.trim();
        if (!normalized.matches(E164_LIKE_REGEX)) {
            throw new IllegalArgumentException("Phone number must contain 7-15 digits and may start with +");
        }
        this.value = normalized;
    }

    public static PhoneNumber valueOf(final String value) {
        return new PhoneNumber(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhoneNumber)) {
            return false;
        }
        final PhoneNumber other = (PhoneNumber) o;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

