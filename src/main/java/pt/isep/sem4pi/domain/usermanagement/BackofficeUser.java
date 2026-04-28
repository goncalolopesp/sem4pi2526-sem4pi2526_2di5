package pt.isep.sem4pi.domain.usermanagement;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Simple domain entity to hold additional backoffice user data.
 */
public class BackofficeUser {

    private final String username;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final LocalDate securityClearanceExpirationDate;
    private final LocalDate skillsAssessmentDate;
    private boolean enabled;

    public BackofficeUser(final String username, final String name, final String email,
                          final String phoneNumber, final LocalDate securityClearanceExpirationDate,
                          final LocalDate skillsAssessmentDate) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("phone number is required");
        }
        if (securityClearanceExpirationDate == null) {
            throw new IllegalArgumentException("security clearance expiration date is required");
        }
        if (skillsAssessmentDate == null) {
            throw new IllegalArgumentException("skills assessment date is required");
        }
        this.username = username.trim();
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.phoneNumber = phoneNumber.trim();
        this.securityClearanceExpirationDate = securityClearanceExpirationDate;
        this.skillsAssessmentDate = skillsAssessmentDate;
        this.enabled = true;
    }

    public String username() {
        return username;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public LocalDate securityClearanceExpirationDate() {
        return securityClearanceExpirationDate;
    }

    public LocalDate skillsAssessmentDate() {
        return skillsAssessmentDate;
    }

    /**
     * Returns whether this backoffice user is enabled. Disabled users are blocked from logging in.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set enabled status. This is persisted when saving the entity via the repository.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BackofficeUser)) return false;
        final BackofficeUser that = (BackofficeUser) o;
        return Objects.equals(username, that.username) || Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    @Override
    public String toString() {
        return "BackofficeUser{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", enabled='" + enabled + '\'' +
                '}';
    }
}
