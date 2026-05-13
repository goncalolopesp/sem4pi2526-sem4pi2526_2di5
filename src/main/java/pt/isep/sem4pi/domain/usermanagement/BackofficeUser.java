package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class BackofficeUser implements AggregateRoot<String> {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate securityClearanceExpirationDate;

    @Column(nullable = false)
    private LocalDate skillsAssessmentDate;

    @Version
    private Long version;

    protected BackofficeUser() {
        // for ORM
    }

    public BackofficeUser(final String username, final String name, final String email,
                          final String phoneNumber,
                          final LocalDate securityClearanceExpirationDate,
                          final LocalDate skillsAssessmentDate) {

        Preconditions.nonEmpty(username, "Username is required");
        Preconditions.nonEmpty(name, "Name is required");
        Preconditions.nonEmpty(email, "Email is required");
        Preconditions.nonEmpty(phoneNumber, "Phone number is required");
        Preconditions.nonNull(securityClearanceExpirationDate, "Security clearance expiration date is required");
        Preconditions.nonNull(skillsAssessmentDate, "Skills assessment date is required");

        this.username = username.trim();
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.phoneNumber = phoneNumber.trim();
        this.securityClearanceExpirationDate = securityClearanceExpirationDate;
        this.skillsAssessmentDate = skillsAssessmentDate;
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


    public Long version() {
        return version;
    }

    @Override
    public String identity() {
        return username;
    }

    @Override
    public boolean hasIdentity(final String id) {
        return username.equals(id);
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof BackofficeUser)) {
            return false;
        }

        final BackofficeUser that = (BackofficeUser) other;
        return this.username.equals(that.username);
    }

    @Override
    public boolean equals(final Object other) {
        return sameAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}