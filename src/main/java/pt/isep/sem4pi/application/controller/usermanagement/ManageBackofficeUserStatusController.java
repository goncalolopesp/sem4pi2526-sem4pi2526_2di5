package pt.isep.sem4pi.application.controller.usermanagement;

import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;

import java.util.List;

public class ManageBackofficeUserStatusController {

    private final UserStatusService service = new UserStatusService();

    public List<UserStatusService.BackofficeUserStatus> listAllWithStatus() {
        return service.listAllWithStatus();
    }

    public void enableUser(final String username) {
        service.enableUser(username);
    }

    public void disableUser(final String username) {
        service.disableUser(username);
    }
}