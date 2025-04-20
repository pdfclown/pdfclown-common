# pdfclown-common-build

Common build utilities for pdfClown.org projects.

Because of the following reasons, this module integrates an adaptation of [JSONassert](https://github.com/skyscreamer/JSONassert) (org.skyscreamer:jsonassert:1.5.1), which should be replaced with its corresponding dependency when, hopefully, fixed upstream:

- JPMS incompatibility (see [issue 116](https://github.com/skyscreamer/JSONassert/issues/116)): fixed split package problem (redundant `org.json.JSONString` class removed)
- obsolete json dependency (see [issue 168](https://github.com/skyscreamer/JSONassert/issues/168)): replaced with [openjson](https://github.com/openjson/openjson) (actively maintained, better API)

## Fork reconciliation

<table border="1">
<tr>
<td><b>Local package</b></td>
<td><b>Upstream package</b></td>
<td><b>Upstream commit*</b></td>
<td><b>Upstream VCS</b></td>
</tr>
<tr><td><code>org.pdfclown.common.build.internal.jsonassert</code></td><td><a href="https://github.com/skyscreamer/JSONassert">org.skyscreamer.jsonassert</a></td><td><a href="https://github.com/skyscreamer/JSONassert/commit/7414e901af11c559bc553e5bb8e12b99a57d1c1c">7414e901af11c559bc553e5bb8e12b99a57d1c1c</a> (2022-07-11 18:50:49+0530)</td><td>git</td>
</tr>
</table>
[*] Latest commit reconciled
