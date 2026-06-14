package pt.isep.sem4pi.domain.model.aircontrolarea.DTO;

public class AirControlAreaCreateDTO {
    private final String code;
    private final String name;
    private final double minLatitude;
    private final double maxLatitude;
    private final double minLongitude;
    private final double maxLongitude;
    private final double minimumReserveFuel;

    public AirControlAreaCreateDTO(String code, String name,
                                   double minLatitude, double maxLatitude,
                                   double minLongitude, double maxLongitude,
                                   double minimumReserveFuel) {
        this.code = code;
        this.name = name;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.minimumReserveFuel = minimumReserveFuel;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    public double getMinimumReserveFuel() {
        return minimumReserveFuel;
    }
}

