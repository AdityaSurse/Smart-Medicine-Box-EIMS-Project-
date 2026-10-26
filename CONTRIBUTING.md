# Contributing Guidelines

## Branching Strategy
- `main`: Production-ready releases (tagged with `vX.Y.Z`).
- `develop`: Integration branch for completed features.
- `feature/<name>`: New feature branches branched off `develop`.
- `hotfix/<name>`: Urgent patches branched off `main` and backported to `develop`.

## Commit Message Convention
Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` New user-facing feature or domain capability.
- `fix:` Bug fix.
- `chore:` Maintenance, dependency updates, build tooling.
- `docs:` Documentation and diagram updates.
- `refactor:` Code restructuring without functional change.
- `test:` Adding or updating unit/integration tests.

## Pull Request Guidelines
1. Branch off `develop` using `feature/<feature-name>`.
2. Keep changes small, atomic, and focused.
3. Ensure all domain Use Cases include unit tests.
4. Verify tests pass locally: `./gradlew test`.
5. Open PR targeting `develop` using the standard PR template.
