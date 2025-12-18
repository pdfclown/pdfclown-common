[[pdfClown.org] Documentation](README.md) > [Project Conventions](conventions.md) >

# [pdfClown.org] Project Maintenance

## Source Code Management

> [!IMPORTANT]
> To enforce the conventions described herein, committers have to set up the [**commit validation hooks**](building.md#setup).

### Commits

Commit messages MUST be defined according to [Conventional Commits 1.0](https://www.conventionalcommits.org/en/v1.0.0/).

Since the specification encouraged the flexible/opinionated definition of commit types, lots of entropy has plagued the way common chores like project and dependency version bumps are marked in commits (some projects opted for `chore`, others for `build` and `fix`, or even `release`).

To avoid ambiguities, pdfClown.org projects adopt the following custom conventions on top of the specification.

#### Commit Types

Commit types are defined according to commitizen <code>[cz_conventional_commits](https://commitizen-tools.github.io/commitizen/config/)</code> — by default:

- `build`
- `bump`
- `chore`
- `ci`
- `docs`
- `feat`
- `fix`
- `perf`
- `refactor`
- `revert`
- `style`
- `test`

#### Commit Scopes

Whilst the specification prescribes that "a scope MUST consist of a noun describing a section of the codebase" (rule 4), in practice there is room for overlapping and ambiguity: on one side, regular scopes typically divide a codebase into a partition by subproject name; on the other side, special scopes select codebase elements (like dependencies) orthogonally across that partition. Since there is no established convention about special scopes, automated tools like dependabot unfortunately use nouns (for example, `deps`) which are prone to name clash with regular scopes (in the example, there could be a subproject named "deps").

Within pdfClown.org projects, scopes are informally distinguished between regular and special ones according to these rules:
- a **regular scope** is the *name of the affected subproject, without its root project prefix* (for example, if the subproject is `pdfclown-common-util` under `pdfclown-common` root project, the scope shall be `util`); if a commit affects multiple subprojects, the scope is omitted
- **special scopes** are *not related to subprojects; in order to distinguish them from regular scopes, they are prefixed by hash*

#### Commit Cases

- **dependency updates** (see also [dependabot scopes](https://docs.github.com/en/code-security/dependabot/working-with-dependabot/dependabot-options-reference#include)):
  - **production dependency**:<br>
    `fix(deps): bump *** to X.Y.Z`
  - **development dependency**:<br>
    `build(deps-dev): bump *** to X.Y.Z`
- **project version bumps** (see also [commitizen `cz bump` command](https://github.com/commitizen-tools/commitizen)):
  - **project release**:<br>
    `bump: release version X.Y.Z`
  - **project development**:<br>
    `bump: dev version X.Y.Z-SNAPSHOT`

### Branches

pdfClown.org projects adhere to [trunk-based branching model](https://www.atlassian.com/continuous-delivery/continuous-integration/trunk-based-development).

Branch names MUST be defined according to [Conventional Commits 1.0](https://www.conventionalcommits.org/en/v1.0.0/), as specified in "[Commit Types](#commit-types)" section, with the addition of the following custom names:

- `dependabot` (ephemeral) — for pull-request branches automatically created by dependabot
- `r` (long-lived) — for release maintenance (e.g., `r/1.3` for maintaining updates of version 1.3.*)
- `release` (ephemeral) — for release preparation (e.g. `release/1.3.0`), branched out from `main` (current development line) or `r/*` (release maintenance line) branches

> [!NOTE]
> Branch names are configured for pre-commit check via `commit-check.toml` in the project.

### Pull Requests

Pull request titles MUST be defined according to Conventional Commits 1.0, as specified in "[Commits](#commits)" section.

> [!NOTE]
> Pull request titles are configured for CI check via `.github/workflows/pull-request.yml` in the project.

In order to leave a clear and linear commit history, PR branches MUST be merged into their base branch via squash, retaining the relevant commit messages of the squashed commits.