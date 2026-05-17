package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.aircraft.AircraftModelTemp;
import pt.isep.sem4pi.domain.repositories.AircraftModelTempRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryAircraftModelTempRepository implements AircraftModelTempRepository {

    private final List<AircraftModelTemp> database = new ArrayList<>();

    public InMemoryAircraftModelTempRepository() {
        database.add(new AircraftModelTemp("A320", 180));
        database.add(new AircraftModelTemp("B737", 215));
        database.add(new AircraftModelTemp("A380", 853));
    }

    @Override
    public AircraftModelTemp save(AircraftModelTemp model) {
        database.removeIf(m -> m.getModelId().equalsIgnoreCase(model.getModelId()));
        database.add(model);
        return model;
    }

    @Override
    public Optional<AircraftModelTemp> findById(String modelId) {
        return database.stream()
                .filter(m -> m.getModelId().equalsIgnoreCase(modelId))
                .findFirst();
    }
}