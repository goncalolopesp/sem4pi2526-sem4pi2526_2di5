package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade de dominio que representa o perfil de negocio de um utilizador backoffice.
 *
 * Contem dados de negocio (nome, email, telefone e datas) e nao inclui credenciais.
 * A autenticacao/estado ativo e gerida pelo SystemUser do framework (US30/US32).
 */
@Entity
public class BackofficeUser implements AggregateRoot<String> {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phoneNumber", nullable = false))
    private PhoneNumber phoneNumber;

    @Column(nullable = false)
    private LocalDate securityClearanceExpirationDate;

    @Column(nullable = false)
    private LocalDate skillsAssessmentDate;

    @Version
    private Long version;

    protected BackofficeUser() {
        // for ORM
    }

    public BackofficeUser(final String username, final String name, final Email email,
                          final PhoneNumber phoneNumber,
                          final LocalDate securityClearanceExpirationDate,
                          final LocalDate skillsAssessmentDate) {

        Preconditions.nonEmpty(username, "Username is required");
        Preconditions.nonEmpty(name, "Name is required");
        Preconditions.nonNull(email, "Email is required");
        Preconditions.nonNull(phoneNumber, "Phone number is required");
        Preconditions.nonNull(securityClearanceExpirationDate, "Security clearance expiration date is required");
        Preconditions.nonNull(skillsAssessmentDate, "Skills assessment date is required");

        this.username = username.trim();
        this.name = name.trim();
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.securityClearanceExpirationDate = securityClearanceExpirationDate;
        this.skillsAssessmentDate = skillsAssessmentDate;
    }

    public String username() {
        return username;
    }

    public String name() {
        return name;
    }

    public Email email() {
        return email;
    }

    public PhoneNumber phoneNumber() {
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