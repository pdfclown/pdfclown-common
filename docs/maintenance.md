pdfClown Common > [Documentation](README.md) >

# Project Maintenance

## Dependency Updates

Anytime the following dependencies are updated (manually or via Dependabot PRs), the project MUST be aligned through additional chores:

- [common dependency updates](common/maintenance.md#dependency-updates) < pdfClown.org
- `com.puppycrawl.tools:checkstyle`:
  - update [`google_checks.patch.xsl`](../pdfclown-common-build/src/main/resources/org/pdfclown/common/build/conf/checkstyle/google_checks.patch.xsl) (see MAINTENANCE inside)
- `org.skyscreamer:jsonassert`:
  - align `json.version` property to its transitive
    `org.json:json` dependency
