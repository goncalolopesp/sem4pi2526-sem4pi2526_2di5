# Documento de estudo - US056, US057, US071

## US056 - Registar um novo modelo de motor

### `domain/model/engineModel/EngineModel.java`
- Entidade de dominio do modelo de motor.
- Aplica invariantes: nome nao vazio, potencia e eficiencia > 0.
- Suporta verificacao de unicidade por fabricante+nome.

### `domain/repositories/EngineModelRepository.java`
- Interface do repositorio com metodo para verificar fabricante+nome.

### `application/service/engineModel/EngineModelRegistrationService.java`
- Converte strings em IDs de dominio.
- Valida existencia do fabricante.
- Verifica unicidade do codigo do modelo e da combinacao fabricante+nome.
- Cria e persiste o `EngineModel`.

### `application/controller/engineModel/RegisterEngineModelController.java`
- Garante permissao de Backoffice Operator/Admin.
- Orquestra o registo chamando o service de aplicacao.
- Disponibiliza lista de fabricantes para a UI.

### `presentation/engineModel/RegisterEngineModelUI.java`
- Recolhe os dados do modelo de motor via consola.
- Lista fabricantes disponiveis para selecao.
- Valida entradas basicas (valores numericos positivos) antes de submeter.
- Encaminha o pedido para o controller e apresenta o resultado.

### `infrastructure/persistence/JPA/JpaEngineModelRepository.java`
- Implementacao JPA do repositorio de modelos de motor.

### `infrastructure/persistence/memory/InMemoryEngineModelRepository.java`
- Implementacao in-memory do repositorio de modelos de motor.

### `infrastructure/bootstrap/BackofficeConfigurationBootstrapper.java`
- Carrega dados iniciais de fabricantes e modelos de motor (seed).

### Testes (US056)
- `src/test/java/pt/isep/sem4pi/application/service/engineModel/EngineModelRegistrationServiceTest.java`
- `src/test/java/pt/isep/sem4pi/application/controller/engineModel/RegisterEngineModelControllerTest.java`
- `src/test/java/pt/isep/sem4pi/domain/model/engineModel/EngineModelTest.java`
- `src/test/java/pt/isep/sem4pi/domain/model/engineModel/EngineModelIdTest.java`
- `src/test/java/pt/isep/sem4pi/infrastructure/persistence/InMemoryEngineModelRepositoryTest.java`

## US057 - Certificar um motor para um modelo de aeronave

### `domain/model/aircraftModel/AircraftModel.java`
- Regra central: `certifyEngine` valida compatibilidade e duplicados.
- Compatibilidade baseada em `motorization` vs `EngineType`.
- Mantem lista de motores certificados no agregado.

### `domain/repositories/EngineModelRepository.java`
- Permite carregar o motor a certificar por ID.

### `domain/repositories/AircraftModelRepository.java`
- Permite carregar o modelo de aeronave a certificar por ID.

### `application/service/aircraftModel/AircraftModelCertificationService.java`
- Carrega agregados `AircraftModel` e `EngineModel`.
- Invoca a regra de negocio no agregado.
- Persiste o resultado.

### `application/controller/aircraftModel/CertifyEngineModelController.java`
- Valida permissao de Backoffice Operator/Admin.
- Delegacao para o service de certificacao.
- Disponibiliza listas de aeronaves e motores para a UI.

### `presentation/aircraftModel/CertifyEngineModelUI.java`
- Lista modelos de aeronave e motores disponiveis.
- Permite a selecao de ambos e envia para o controller.
- Apresenta mensagens de sucesso/erro.

### `infrastructure/persistence/JPA/JpaAircraftModelRepository.java`
- Implementacao JPA do repositorio de modelos de aeronave.

### `infrastructure/persistence/memory/InMemoryAircraftModelRepository.java`
- Implementacao in-memory do repositorio de modelos de aeronave.

### `infrastructure/persistence/JPA/JpaEngineModelRepository.java`
- Implementacao JPA para carregar motores na certificacao.

### `infrastructure/persistence/memory/InMemoryEngineModelRepository.java`
- Implementacao in-memory para carregar motores na certificacao.

### Testes (US057)
- Nenhum teste encontrado no repositorio.

## US071 - Retirar uma aeronave da frota

### `domain/repositories/FlightRepository.java`
- Fornece o metodo `hasPendingFlights` usado na regra de negocio da retirada.

### `domain/repositories/AircraftRepository.java`
- Permite carregar e guardar a aeronave a retirar.

### `application/service/aircraft/AircraftRetirementService.java`
- Verifica se a aeronave existe.
- Bloqueia retirada se houver voos pendentes.
- Altera o estado e persiste.

### `application/controller/aircraft/RetireAircraftController.java`
- Garante permissao de colaborador/Admin.
- Delegacao para o service de retirada.
- Disponibiliza lista de aeronaves para a UI.

### `presentation/aircraft/RetireAircraftUI.java`
- Lista aeronaves ativas e permite selecao.
- Pede a retirada ao controller e mostra o resultado.

### `infrastructure/persistence/JPA/JpaAircraftRepository.java`
- Implementacao JPA do repositorio de aeronaves.

### `infrastructure/persistence/memory/InMemoryAircraftRepository.java`
- Implementacao in-memory do repositorio de aeronaves.

### `infrastructure/persistence/JPA/JpaFlightRepository.java`
- Implementacao JPA para consultar voos pendentes.

### `infrastructure/persistence/memory/InMemoryFlightRepository.java`
- Implementacao in-memory para consultar voos pendentes.

### Testes (US071)
- `src/test/java/pt/isep/sem4pi/application/service/aircraft/AircraftRetirementServiceTest.java`
