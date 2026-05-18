package pt.isep.sem4pi.application.controller.usermanagement;

import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;

import java.util.List;

/**
 * Controller da US32 para ativar/desativar utilizadores backoffice.
 *
 * Mantem a UI fina e delega regras e validacoes ao UserStatusService.
 */
public class ManageBackofficeUserStatusController {

    private final UserStatusService service = new UserStatusService();

    /**
     * Lista utilizadores com estado atual para apresentacao na UI.
     */
    public List<UserStatusService.BackofficeUserStatus> listAllWithStatus() {
        return service.listAllWithStatus();
    }

    /**
     * Ativa um utilizador (SystemUser.active=true).
     */
    public void enableUser(final String username) {
        service.enableUser(username);
    }

    /**
     * Desativa um utilizador (SystemUser.active=false), com validacoes de seguranca.
     */
    public void disableUser(final String username) {
        service.disableUser(username);
    }
}