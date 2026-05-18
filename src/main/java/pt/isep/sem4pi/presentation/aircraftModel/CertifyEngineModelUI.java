package pt.isep.sem4pi.presentation.aircraftModel;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.aircraftModel.CertifyEngineModelController;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;

import java.util.List;

@SuppressWarnings("squid:S106")
/**
 * UI da US57: seleciona modelo de aeronave e motor para certificacao.
 */
public class CertifyEngineModelUI extends AbstractUI {
    private final CertifyEngineModelController controller = new CertifyEngineModelController();

    @Override
    /**
     * Fluxo principal: selecionar modelos e enviar para o controller.
     */
    protected boolean doShow() {
        final AircraftModel aircraftModel = selectAircraftModel();
        if (aircraftModel == null) {
            return false;
        }
        final EngineModel engineModel = selectEngineModel();
        if (engineModel == null) {
            return false;
        }

        try {
            controller.certifyEngineModel(aircraftModel.identity(), engineModel.identity());
            System.out.println("Engine model certified successfully for aircraft model: " + aircraftModel.identity());
            return true;
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Certify Engine Model";
    }

    /**
     * Lista modelos de aeronave e devolve o selecionado.
     */
    private AircraftModel selectAircraftModel() {
        final List<AircraftModel> models = controller.listAircraftModels();
        if (models.isEmpty()) {
            System.out.println("No aircraft models available.");
            return null;
        }

        System.out.printf("%3s  %-12s  %-20s  %-10s  %-12s%n", "#", "Code", "Model", "Type", "Motorization");
        int idx = 1;
        for (final AircraftModel model : models) {
            System.out.printf("%3d) %-12s  %-20s  %-10s  %-12s%n",
                    idx++, model.identity(), model.modelName(), model.aircraftType(), model.motorization());
        }

        final int choice = Console.readInteger("Select aircraft model by number (0 to cancel):");
        if (choice <= 0 || choice > models.size()) {
            System.out.println("Operation cancelled.");
            return null;
        }
        return models.get(choice - 1);
    }

    /**
     * Lista modelos de motor e devolve o selecionado.
     */
    private EngineModel selectEngineModel() {
        final List<EngineModel> models = controller.listEngineModels();
        if (models.isEmpty()) {
            System.out.println("No engine models available.");
            return null;
        }

        System.out.printf("%3s  %-12s  %-20s  %-10s  %-10s%n", "#", "Code", "Name", "Type", "Maker");
        int idx = 1;
        for (final EngineModel model : models) {
            System.out.printf("%3d) %-12s  %-20s  %-10s  %-10s%n",
                    idx++, model.identity(), model.name(), model.type(), model.manufacturerId());
        }

        final int choice = Console.readInteger("Select engine model by number (0 to cancel):");
        if (choice <= 0 || choice > models.size()) {
            System.out.println("Operation cancelled.");
            return null;
        }
        return models.get(choice - 1);
    }
}
