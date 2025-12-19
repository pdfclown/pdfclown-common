[pdfClown.org :: Documentation](README.md) > [Project Conventions](conventions.md) >

# pdfClown.org :: Project Structure

This document describes how the pdfClown.org projects are structured.

## Filesystem structure

The filesystem structure of pdfClown.org projects MUST follow [Maven's Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).

For clarity, [subproject directory names MUST match the `project/artifactID` element](https://www.sonatype.com/blog/2011/01/maven-tip-project-directories-and-artifact-ids) in their respective pom.xml; the same applies to the `project/name` element:

    <name>${project.artifactId}</name>

## Project Hierarchy

A pdfClown.org project MUST be defined according to this hierarchy:

![Image](res/images/project-hierarchy.svg)

**Legend:**
- _italic style_ denotes **subprojects with "pom" packaging** (assimilated to abstract classes)
- `<<private>>` stereotype denotes **private artifacts** (that is, not published, consumed only internally to the project hierarchy)
- `<<public>>` stereotype denotes **published artifacts** (that is, consumed also externally to the project hierarchy)
- properties represent **declarations** (for example `dependencies`, `plugins`)
- solid arrow lines represent **Maven `parent` (inheritance) relationships**, pointing from child to parent
- dashed arrow lines represent **Maven dependencies usage** (import or regular)

<table>
<tr>
  <th>Name</th>
  <th>Role</th>
  <th>Description</th>
  <th>Publish</th>
</tr>
<tr>
  <td><code>base</code></td>
  <td>:wrench:<br/>public parent</td>
  <td>Build configuration meant to be reused outside the project hierarchy via inheritance.
  <br><br>Example: <a href="../pdfclown-common-base/pom.xml"><code>pdfclown-common-base</code></a></td>
  <td>:satellite:</td>
</tr>
<tr>
  <td rowspan="2"><code>bom</code></td>
  <td>:gear:<br/>minimal BOM</td>
  <td rowspan="2">Declares its subprojects (Maven reactor) and the corresponding dependencies, meant to be reused via import.
  <br>Any third-party dependency is declared by <code>deps</code>.
  <br>Any build configuration is inherited from either <code>super</code> (if present) or a parent external to the project hierarchy.
  <br><br>Example: <a href="../pom.xml"><code>pdfclown-common-bom</code></a></td>
  <td>:satellite:</td>
</tr>
<tr>
  <td>:wrench:<br/>root project</td>
  <td>:lock:</td>
</tr>
<tr>
  <td><code>build</code></td>
  <td>:package:<br/>concrete artifact</td>
  <td>Library providing common configuration, resources and utilities for the building process.<br/><br/>Example: <a href="../pdfclown-common-build/pom.xml"><code>pdfclown-common-build</code></a></td>
  <td>:satellite:</td>
</tr>
<tr>
  <td><code>deps</code></td>
  <td>:gear:<br/>full BOM</td>
  <td>Declares all the dependency used in the project hierarchy, including those in <code>bom</code>.<br><br>Example: <a href="../pdfclown-common-deps/pom.xml"><code>pdfclown-common-deps</code></a></td>
  <td>:satellite:</td>
</tr>
<tr>
  <td><code>parent</code></td>
  <td>:wrench::gear:<br/>private parent</td>
  <td>Build configuration and dependencies meant to be reused inside the project hierarchy via inheritance by all the concrete subprojects.<br><br>Example: <a href="../pdfclown-common-parent/pom.xml"><code>pdfclown-common-parent</code></a></td>
  <td>:lock:</td>
</tr>
<tr>
  <td rowspan="2"><code>super</code></td>
  <td rowspan="2">:wrench:<br/>private super&#x2011;parent</td>
  <td>Build configuration meant to be inherited by <code>bom</code> as an alternative to a parent from an external project, if the latter is missing or needs to be customized (in such case, the parent from an external project is inherited by <code>super</code>).<br/><br/>Example: <a href="../pdfclown-common-super/pom.xml"><code>pdfclown-common-super</code></a></td>
  <td rowspan="2">:lock:&dagger;</td>
</tr>
<tr>
  <td>Build configuration meant to be inherited by <code>base</code> to expose the configuration to external projects.<br/><br/>Example: <a href="../pdfclown-common-super/pom.xml"><code>pdfclown-common-super</code></a></td>
</tr>
<tr>
  <td><code>util</code></td>
  <td>:package:<br/>concrete artifact</td>
  <td>Library providing common utilities.<br/><br/>Example: <a href="../pdfclown-common-util/pom.xml"><code>pdfclown-common-util</code></a></td>
  <td>:satellite:</td>
</tr>
<tr>
  <td><code>(lib*)</code></td>
  <td>:package:<br/>concrete artifacts</td>
  <td>Any other concrete (that is, without "pom" packaging) subproject <i>inside</i> the project hierarchy.</td>
  <td>:satellite:</td>
</tr>
<tr>
  <th colspan="4">External projects</th>
</tr>
<tr>
  <td><code>(external&#x2011;root*)</code></td>
  <td>:wrench:<br/>external children</td>
  <td>Root projects <i>outside</i> the project hierarchy (that is, external projects consuming the published <code>base</code> subproject).</td>
  <td>N/A</td>
</tr>
<tfoot><tr><td colspan="4">NOTE<br>&dagger; Currently, because of technical limitations in Maven toolset, it is transitively inherited outside the project (ideally, it should be flattened inside <code>base</code> and not published).</td></tr></tfoot>
</table>

The notorious verbosity of Maven configuration is prone to quickly degenerate into a cluttered mess of plugin and dependency declarations and references. The following guidelines are meant to define a healthy structure, and are at the basis of the hierarchy here-above illustrated:

- **plugin and dependency declarations** MUST be decoupled to take advantage of inheritance and import mechanisms, and MUST be pushed as high as possible in the inheritance hierarchy for appropriate reuse:
  - **plugins** are declared in:
    - `super` — for configuration shared _both internally and externally_ to the project
    - `parent` — for configuration shared _only internally_ to the project
    - `base` — for configuration shared _only externally_ to the project
  - **dependencies** are declared in BOM (Bill of Materials) subprojects only:
    - `bom` — for _dependencies corresponding to subprojects within_ the project hierarchy
    - `deps` — for _all the dependencies_ of the project hierarchy, `bom` inclusive
- **plugin and dependency references** MUST be pushed as low as possible in the inheritance hierarchy
- **artifacts of private subprojects** (like `parent`) are neither installed nor published, implying that the artifacts of their child subprojects MUST flatten them
