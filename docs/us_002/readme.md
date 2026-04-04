# US 002 - Project Repository

## 1. Context

This task is assigned for the first time in this sprint. It establishes the collaborative environment for the team by setting up the version control repository and the project management tooling required to track the work throughout the semester.

### 1.1 List of issues

**Analysis:** Determine the requirements for repository management and agile project tracking based on the constraints of the project.

**Design:** Define the structure of the GitHub Project board (columns) and the tagging/labeling system for repository issues.

**Implement:** Initialize the GitHub repository provided by the GitHub Classroom, invite all team members, and configure the GitHub Projects board.

**Test:** Verify that all team members have the correct access permissions and that issues can be created, assigned, and moved across the board.

## 2. Requirements

**US 002** As Project Manager, I want the team to use the defined project repository in GitHub and setup a GitHub tool for project management.

**Acceptance Criteria:**

- AC 1 The team must use the GitHub repository created via GitHub Classroom.
- AC 2 A GitHub project management tool (GitHub Projects/Issues) must be configured to track User Stories and tasks.
- AC 3 The repository must be updated regularly as work progresses.

**Dependencies/References:**

This requirement relates directly to **NFR01 (Project management using scrum)** and **NFR04 (Source Control)**. It serves as the basis for tracking all subsequent User Stories and tasks in the project.

## 3. Analysis

To fulfill the requirement of using a GitHub tool for project management while adhering to Scrum methodologies (NFR01), the team analyzed the built-in GitHub features.

GitHub Issues combined with GitHub Projects (Kanban board) provides the most seamless integration. It allows the team to create User Stories as Issues, assign them to specific sprints (using Milestones), and track their progress visually without needing external tools like Jira or Trello. This ensures all code, documentation, and project tracking remain centralized in a single platform.

## 4. Design

The design for this US focuses on the configuration of the project management workflow rather than software architecture.

### 4.1. Realization

The GitHub Project board is designed with the following workflow columns to reflect the Scrum process:
1. **Product Backlog:** All unassigned User Stories.
2. **Sprint Backlog (To Do):** Tasks selected for the current sprint.
3. **In Progress:** Tasks currently being worked on by assigned team members.
4. **In Review:** Tasks with an open Pull Request or awaiting peer validation.
5. **Done:** Completed and merged tasks.

Labels were designed to categorize issues: `bug`, `documentation`, `enhancement`, `US`, and `task`.

### 4.2. Acceptance Tests

The validation of this setup is organizational rather than programmatic.

**Test 1:** *Verifies that the repository and board are properly linked.*
- Create a test issue in the repository.
- Verify that it can be added to the GitHub Project board and assigned to a team member.

**Test 2:** *Verifies regular updates.*
- Check the commit history to ensure multiple updates are being pushed weekly by different team members, complying with the regular update requirement.

## 5. Implementation

The following configurations were applied to the repository:
- The repository was successfully cloned from the GitHub Classroom assignment link.
- A new GitHub Project (Kanban style) was created and linked to the repository.
- Milestones were created for Sprint 1, Sprint 2, and Sprint 3 to match the project deadlines.
- All team members were added as collaborators with write access.

## 6. Integration/Demonstration

This setup integrates directly with the team's daily development workflow.

To demonstrate its use:
1. When a developer starts working on a US, they move the corresponding Issue to "In Progress" on the board.
2. The developer links their branch and commits to the Issue using GitHub keywords (e.g., `Resolves #12` in the commit message or Pull Request description).
3. Once the code is merged into the main branch, the issue is automatically closed and moved to "Done".

## 7. Observations

Using GitHub Projects heavily streamlines the tracking process, as branch merges can automatically close issues and update the board. Maintaining this board accurately is critical, as it provides the required evidence of regular work and balanced task division for the sprint assessments.