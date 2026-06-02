package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.model.Username;

import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.Optional;

public class CompanyFleetBootstrapper implements Action {

    @Override
    public boolean execute() {
        AirTransportCompanyRepository companyRepo = PersistenceContext.repositories().airTransportCompanies();
        AircraftRepository aircraftRepo = PersistenceContext.repositories().aircrafts();
        AirTransportCompanyCollaboratorRepository collabRepo = PersistenceContext.repositories().airTransportCompanyCollaborators();

        try {
            Optional<AirTransportCompany> tapOpt = companyRepo.findByIataCode(new IataCodeCompany("TP"));

            if (tapOpt.isPresent()) {
                AirTransportCompany tap = tapOpt.get();

                Optional<Aircraft> a1 = aircraftRepo.ofIdentity(new AircraftRegistration("CS-TST"));
                Optional<Aircraft> a2 = aircraftRepo.ofIdentity(new AircraftRegistration("CS-RET"));
                Optional<Aircraft> a3 = aircraftRepo.ofIdentity(new AircraftRegistration("CS-B73"));
                Optional<Aircraft> a4 = aircraftRepo.ofIdentity(new AircraftRegistration("CS-B78"));

                a1.ifPresent(a -> { try { tap.addAircraftToFleet(a); } catch (Exception ignored) {} });
                a2.ifPresent(a -> { try { tap.addAircraftToFleet(a); } catch (Exception ignored) {} });
                a3.ifPresent(a -> { try { tap.addAircraftToFleet(a); } catch (Exception ignored) {} });
                a4.ifPresent(a -> { try { tap.addAircraftToFleet(a); } catch (Exception ignored) {} });

                companyRepo.save(tap);

                Username eapliUsername = Username.valueOf("pedro_collab");

                SystemUser collabUser;
                if (AuthzRegistry.userService().userOfIdentity(eapliUsername).isEmpty()) {
                    SystemUserBuilder userBuilder =
                            new SystemUserBuilder(
                                    new SEM4PIPasswordPolicy(),
                                    new PlainTextEncoder()
                            );

                    userBuilder.withUsername("pedro_collab")
                            .withPassword("Password123!")
                            .withName("Pedro", "Soares")
                            .withEmail("pedro.soares@tap.pt")
                            .withRoles(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, SEM4PIRoles.BACKOFFICE_OPERATOR);

                    collabUser = PersistenceContext.repositories().users().save(userBuilder.build());
                } else {
                    collabUser = AuthzRegistry.userService().userOfIdentity(eapliUsername).get();
                }

                if (collabRepo.findBySystemUser(collabUser).isEmpty()) {
                    AirTransportCompanyCollaborator companyCollaborator = new AirTransportCompanyCollaborator(collabUser, tap);
                    collabRepo.save(companyCollaborator);
                }

                Username adminUsername = Username.valueOf("admin");
                Optional<SystemUser> adminOpt = AuthzRegistry.userService().userOfIdentity(adminUsername);

                if (adminOpt.isPresent()) {
                    SystemUser adminUser = adminOpt.get();
                    if (collabRepo.findBySystemUser(adminUser).isEmpty()) {
                        AirTransportCompanyCollaborator adminProfile = new AirTransportCompanyCollaborator(adminUser, tap);
                        collabRepo.save(adminProfile);
                    }
                }

                System.out.println("Fleet and Collaborator successfully bootstrapped.");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Failed to bootstrap fleet/collaborator: " + e.getMessage());
            return false;
        }
    }
}