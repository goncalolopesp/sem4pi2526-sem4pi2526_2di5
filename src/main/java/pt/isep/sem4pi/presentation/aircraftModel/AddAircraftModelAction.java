package pt.isep.sem4pi.presentation.aircraftModel;

import eapli.framework.actions.Action;

/**
 * Action class for adding a new aircraft model through the user interface.
 * <p>This class implements the Action interface and triggers the display
 * of the AddAircraftModelUI to allow users to input and create new aircraft models.</p>
 */
public class AddAircraftModelAction implements Action {

    /**
     * Executes the action to add a new aircraft model.
     * <p>This method instantiates and shows the AddAircraftModelUI.</p>
     *
     * @return true if the UI operation completes successfully, false otherwise
     */
    @Override
    public boolean execute() {
        return new AddAircraftModelUI().show();
    }
}