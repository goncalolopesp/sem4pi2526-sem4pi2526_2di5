package pt.isep.sem4pi.presentation.usermanagement;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.usermanagement.RegisterBackofficeUserController;
import pt.isep.sem4pi.application.service.usermanagment.PasswordStrengthService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * UI da US31: recolhe dados de registo de backoffice e delega no controller.
 *
 * Faz validacoes basicas de formato (email, datas) e feedback imediato de policy de password.
 * A validacao final e a criacao efetiva sao do controller/servico.
 */
public class RegisterBackofficeUserUI extends AbstractUI {

    private final RegisterBackofficeUserController controller = new RegisterBackofficeUserController();
    private final PasswordStrengthService passwordStrengthService = new PasswordStrengthService();

    @Override
    /**
     * Fluxo principal: validar inputs, normalizar email e pedir registo ao controller.
     */
    protected boolean doShow() {
        try {
            controller.ensureAdmin();

            String username;
            while (true) {
                username = Console.readNonEmptyLine("Username:", "Username required");
                if (!controller.isUsernameAvailable(username)) {
                    System.out.println("Username invalid or already exists. Please choose another.");
                } else {
                    break;
                }
            }

            System.out.println(
                    "Password rules: at least 6 characters, include at least one digit and one uppercase letter.");
            String password;
            while (true) {
                password = Console.readNonEmptyLine("Password:", "Password required");

                // UX feedback only (não é regra formal)
                final var strength = passwordStrengthService.strength(password);
                System.out.println("Password strength: " + strength);

                if (!controller.isPasswordValid(password)) {
                    System.out.println("Invalid password. Please try again.");
                } else {
                    break;
                }
            }

            final String firstName = Console.readNonEmptyLine("First name:", "First name required");
            final String lastName = Console.readNonEmptyLine("Last name:", "Last name required");

            String email;
            while (true) {
                email = Console.readNonEmptyLine("Email:", "Email required");

                // UI valida apenas formato de forma leve; regra final é no controller/serviço
                if (!controller.isEmailFormatValid(email)) {
                    System.out.println("Invalid email format. Please enter an email like user@domain.");
                    continue;
                }

                if (!controller.isEmailAvailable(email)) {
                    System.out.println("Email invalid or already exists. Please provide another.");
                } else {
                    break;
                }
            }

            final String phone = Console.readNonEmptyLine("Phone number:", "Phone number required");

            LocalDate secDate;
            while (true) {
                final String secDateStr = Console.readNonEmptyLine(
                        "Security clearance expiration date (YYYY-MM-DD):",
                        "Security clearance expiration date required");
                try {
                    secDate = LocalDate.parse(secDateStr.trim());
                    break;
                } catch (final DateTimeParseException ex) {
                    System.out.println("Invalid date format. Use YYYY-MM-DD.");
                }
            }

            LocalDate skillsDate;
            while (true) {
                final String skillsDateStr = Console.readNonEmptyLine(
                        "Skills assessment date (YYYY-MM-DD):",
                        "Skills assessment date required");
                try {
                    skillsDate = LocalDate.parse(skillsDateStr.trim());
                    break;
                } catch (final DateTimeParseException ex) {
                    System.out.println("Invalid date format. Use YYYY-MM-DD.");
                }
            }

            final String normalizedEmail = email.trim().toLowerCase();

            controller.register(username.trim(), password, firstName.trim(), lastName.trim(), normalizedEmail,
                    phone.trim(), secDate, skillsDate);
            System.out.println("Backoffice user successfully registered.");
            return true;
        } catch (final Exception e) {
            System.out.println("Error registering user: " + e.getClass().getName() + " - " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Register Backoffice User";
    }
}
