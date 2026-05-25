package pt.isep.sem4pi.presentation.weatherdata;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.weatherdata.WeatherDataTotalsController;

public class WeatherDataTotalsUI extends AbstractUI {
    private final WeatherDataTotalsController controller = new WeatherDataTotalsController();

    @Override
    protected boolean doShow() {
        long total = controller.totalWeatherRecords();
        System.out.println("\nTotal Weather Data records: " + total);
        return false;
    }

    @Override
    public String headline() {
        return "Weather Data Totals";
    }
}

