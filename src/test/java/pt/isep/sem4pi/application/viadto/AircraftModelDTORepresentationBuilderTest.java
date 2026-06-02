package pt.isep.sem4pi.application.viadto;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircraftModel.DTO.AircraftModelDTO;

import static org.junit.Assert.*;

public class AircraftModelDTORepresentationBuilderTest {

    private AircraftModelDTORepresentationBuilder builder;

    @Before
    public void setUp() {
        builder = new AircraftModelDTORepresentationBuilder();
    }

    @Test
    public void ensureBuildsBasePropertiesCorrectly() {
        builder.startObject("AircraftModel")
                .withProperty("manufacturerId", "AIRBUS")
                .withProperty("modelName", "A320")
                .withProperty("aircraftType", "PASSENGER")
                .withProperty("motorization", "TURBOFAN")
                .withProperty("maximumCapacity", 180)
                .withProperty("version", 1L)
                .endObject();

        AircraftModelDTO dto = builder.build();

        assertEquals("AIRBUS", dto.getManufacturerId());
        assertEquals("A320", dto.getModelName());
        assertEquals("PASSENGER", dto.getAircraftType());
        assertEquals("TURBOFAN", dto.getMotorization());
        assertEquals(Integer.valueOf(180), dto.getMaximumCapacity());
        assertEquals(Long.valueOf(1L), dto.getVersion());
    }

    @Test
    public void ensureBuildsNestedWeightLimitsCorrectly() {
        builder.startObject("weightLimits")
                .withProperty("emptyWeight", 40000.0)
                .withProperty("maxZeroFuelWeight", 60000.0)
                .withProperty("maxTakeOffWeight", 70000.0)
                .endObject();

        AircraftModelDTO dto = builder.build();

        assertNotNull(dto.getWeightLimits());
        assertEquals(Double.valueOf(40000.0), dto.getWeightLimits().getEmptyWeight());
        assertEquals(Double.valueOf(60000.0), dto.getWeightLimits().getMaxZeroFuelWeight());
        assertEquals(Double.valueOf(70000.0), dto.getWeightLimits().getMaxTakeOffWeight());
    }

    @Test
    public void ensureBuildsCertifiedEnginesCollectionCorrectly() {
        builder.startCollection("certifiedEngines");

        builder.startObject("certifiedEngine")
                .withProperty("engineModelId", "CFM56")
                .endObject();

        builder.startObject("certifiedEngine")
                .withProperty("engineModelId", "TRENT1000")
                .endObject();

        builder.endCollection();

        AircraftModelDTO dto = builder.build();

        assertNotNull(dto.getCertifiedEngines());
        assertEquals(2, dto.getCertifiedEngines().size());
        assertEquals("CFM56", dto.getCertifiedEngines().get(0).getEngineModelId());
        assertEquals("TRENT1000", dto.getCertifiedEngines().get(1).getEngineModelId());
    }

    @Test
    public void ensureBuildMethodResetsTheBuilderForReuse() {
        builder.startObject("AircraftModel")
                .withProperty("modelName", "A320")
                .endObject();

        AircraftModelDTO dto1 = builder.build();
        AircraftModelDTO dto2 = builder.build(); // O segundo deve vir vazio

        assertEquals("A320", dto1.getModelName());
        assertNull("Builder should be reset after build()", dto2.getModelName());
    }
}