package pt.isep.sem4pi.domain.model.airtransportcompany;

import org.junit.Test;
import static org.junit.Assert.*;

public class CompanyNameTest {

    @Test
    public void ensureValidCompanyNameIsCreated() {
        CompanyName name = new CompanyName("TAP Air Portugal");
        assertEquals("TAP Air Portugal", name.name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyNameFailsIfNull() {
        new CompanyName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyNameFailsIfEmpty() {
        new CompanyName("   ");
    }
}