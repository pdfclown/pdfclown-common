[pdfClown.org](https://github.com/pdfclown) > [Documentation](README.md) >

# Project Maintenance

This document describes maintenance activities common to the pdfClown.org projects.

## Dependency Updates

Anytime the following dependencies are updated (manually or via Dependabot PRs), the project MUST be aligned through additional chores:

- `pdfclown:pdfclown-common`:
  - bump all the references to its documentation (`https://github.com/pdfclown/pdfclown-common/blob/.../docs/common/...`) to the corresponding version (for example, if the new version is 0.8.0, every link shall become like `https://github.com/pdfclown/pdfclown-common/blob/v0.8.0/docs/common/conventions.md`)