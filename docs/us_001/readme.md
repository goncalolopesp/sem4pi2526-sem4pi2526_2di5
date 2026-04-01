# US001 - Technical Constraints

## 1. Context

This User Story aims to identify and document all technical constraints and non-functional requirements defined in section 5 of the project specification.

These constraints must be considered throughout the development of the system.

---

## 2. Requirements

**US001** As Project Manager, I want the team to follow the technical constraints and concerns of the project.

### Acceptance Criteria

- All constraints from section 5 are identified and documented  
- The chosen technologies comply with the constraints  
- The project structure reflects the defined constraints  
- Evidence of constraints is present in documentation  

---

## 3. Identified Constraints

Based on section 5 of the specification, the following constraints were identified:

### NFR01 - Scrum

- The project must follow Scrum methodology  
- Weekly meetings must be held  

---

### NFR02 - Technical Documentation

- Documentation must be in the repository  
- Must use Markdown  
- UML diagrams must use PlantUML  
- Diagrams must include source and image  

---

### NFR03 - Unit Testing

- Test coverage must be at least 90%  
- Recommended use of TDD  
- Use of AAA pattern  

---

### NFR04 - Source Control

- Use GitHub repository  
- Use main branch  
- Frequent commits  

---

### NFR05 - Continuous Integration

- Use GitHub Actions  
- Use Maven for build  

---

### NFR06 - Valid Commits

- Every commit must leave the system in a valid state  
- System must compile and pass tests  

---

### NFR07 - Deployment

- System must include scripts for:
  - build
  - execution
  - deployment  
- Must run in Unix environment  

---

### NFR08 - Database

- Support in-memory database  
- Support relational database  
- Allow configuration-based switching  

---

### NFR09 - Authentication and Authorization

- All users must be authenticated  
- Authorization must be enforced  

---

## 4. Compliance with Constraints

The project complies with the identified constraints as follows:

- Scrum is used for project management (GitHub Issues and sprint organization)  
- GitHub is used for source control (NFR04)  
- Maven is used for build and dependency management (NFR05)  
- GitHub Actions supports continuous integration (NFR05)  
- Documentation is maintained in `/docs` (NFR02)  
- PlantUML is used for diagrams (NFR02)  
- The system is designed to support configurable persistence (NFR08)  
- Authentication and authorization will be implemented (NFR09)  

---

## 5. Project Structure Alignment

The project structure reflects the defined constraints:

- `/docs` contains all documentation (NFR02)
- `/src` follows a layered architecture (domain, application, etc.)
- CI configuration is placed in `.github/workflows`
- Build configuration is defined in `pom.xml`

---

## 6. Conclusion

All technical constraints defined in section 5 were identified and documented.

The project structure and chosen technologies are aligned with these constraints, ensuring compliance throughout development.