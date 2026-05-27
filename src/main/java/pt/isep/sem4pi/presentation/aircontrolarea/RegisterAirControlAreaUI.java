package pt.isep.sem4pi.presentation.aircontrolarea;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.aircontrolarea.RegisterAirControlAreaController;
import pt.isep.sem4pi.domain.aircontrolarea.DTO.AirControlAreaCreateDTO;

public class RegisterAirControlAreaUI extends AbstractUI {

    private final RegisterAirControlAreaController controller = new RegisterAirControlAreaController();

    @Override
    protected boolean doShow() {
        System.out.println("--- Area Boundary Details ---");

        String code = Console.readLine("Air Control Area Code (Unique): ");
        String name = Console.readLine("Area Name: ");

        double minLat = Console.readDouble("Minimum Latitude: ");
        double maxLat = Console.readDouble("Maximum Latitude: ");
        double minLon = Console.readDouble("Minimum Longitude: ");
        double maxLon = Console.readDouble("Maximum Longitude: ");

        double minFuel = Console.readDouble("Minimum Fuel Reserve Required (liters): ");

        try {
            AirControlAreaCreateDTO dto = new AirControlAreaCreateDTO(
                    code, name, minLat, maxLat, minLon, maxLon, minFuel);
            controller.registerAirControlArea(dto);
            System.out.println("\nAir Control Area successfully registered!");

        } catch (IllegalArgumentException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nUNEXPECTED ERROR: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register Air Control Area";
    }
}