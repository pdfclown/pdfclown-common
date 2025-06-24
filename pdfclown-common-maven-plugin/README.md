# pdfclown-common-maven-plugin

Common Maven plugin for pdfClown.org projects.

This module temporarily features a patched version of [flatten-maven-plugin](https://github.com/mojohaus/flatten-maven-plugin) until (hopefully) merged upstream (UPDATE: [PR merged](https://github.com/mojohaus/flatten-maven-plugin/pull/446), waiting for release).

## Source code

### Fork reconciliation

<table border="1">
<tr>
<td><b>Local package</b></td>
<td><b>Upstream package</b></td>
<td><b>Upstream commit*</b></td>
</tr>
<tr><td><code>org.codehaus.mojo.flatten</code></td><td><a href="https://github.com/stechio/flatten-maven-plugin/tree/bugfix/issue-400-ci-friendly-version-model-reordering/src/main/java/org/codehaus/mojo/flatten">org.codehaus.mojo.flatten</a></td><td><a href="https://github.com/stechio/flatten-maven-plugin/commit/da76a8a624016fdaf1d0c620a0887c47f27e1dca">da76a8a</a> (2025-05-12_19:39:10+0200)</td>
</tr>
</table>
[*] Latest commit reconciled
