package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Email implements ValueObject {

    private String value;

    protected Email() {
        // for ORM
    }

    public Email(final String value) {
        Preconditions.nonEmpty(value, "Email is required");
        final String normalized = value.trim().toLowerCase();
        EmailAddress.valueOf(normalized);
        this.value = normalized;
    }

    public static Email valueOf(final String value) {
        return new Email(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Email)) {
            return false;
        }
        final Email other = (Email) o;
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

