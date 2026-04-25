# US055 - Create an aircraft model

## User Story Description

As a Backoffice Operator, I want to register a new aircraft model. Any model must have at least one
engine model (let’s assume that all engines are of the same type). Aircraft has a model name and
manufacturer, and their combination must be unique. At least one certified engine model must be
associated. An aircraft model will have a type, maximum range and other flight characteristics.
This must also be achieved by a bootstrap process.

## Context
Aircraft models define the technical constraints for specific planes. This information includes motorization, weight limits, and performance characteristics like cruise speed and fuel capacity.

### Acceptance Criteria
- Register a new model with a name and manufacturer combination that must be unique.
- Each model must have at least one certified engine model associated with it.
- The model must include specifications for type (passenger, cargo, etc.), maximum range, and flight characteristics.
- The registration must also be achievable through an automated bootstrap process.

### Technical Notes
- Foundational for US070 (Adding an aircraft to a fleet).
- Physical parameters (wing area, drag coefficient) are used for flight physics calculations.

## ⚠️ Dependencies

## 🔗 Impact on Other Issues
This issue is required to start:
- US 057
- US 070