package pt.isep.sem4pi.domain.usermanagement;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade que guarda dados extra de um utilizador do backoffice.
 *
 * Utilizada para armazenar contacto, datas e estado (enabled disabled) de um utilizador.
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

    /**
     * Nome de utilizador.
     * @return username
     */
    public String username() {
        return username;
    }

    /**
     * Nome completo (nome + apelido).
     * @return nome do utilizador
     */
    public String name() {
        return name;
    }

    /**
     * E mail do utilizador (em minusculas).
     * @return e mail
     */
    public String email() {
        return email;
    }

    /**
     * Telefone de contacto.
     * @return numero de telemovel
     */
    public String phoneNumber() {
        return phoneNumber;
    }

    /**
     * Data de validade da habilitacao de seguranca.
     * @return data
     */
    public LocalDate securityClearanceExpirationDate() {
        return securityClearanceExpirationDate;
    }

    /**
     * Data da avaliacao de competencias.
     * @return data
     */
    public LocalDate skillsAssessmentDate() {
        return skillsAssessmentDate;
    }

    /**
     * Indica se o utilizador esta ativo e pode fazer login.
     * @return true se activo
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Altera o estado ativo do utilizador. Isto e persistido quando se salva via repo.
     * @param enabled novo estado
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
