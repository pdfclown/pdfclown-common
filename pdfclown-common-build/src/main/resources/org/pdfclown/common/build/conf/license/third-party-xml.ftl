<?xml version="1.0" encoding="UTF-8"?>
<dependencies>
<#list dependencyMap as entry>
<#assign p = entry.getKey()/>
  <dependency>
    <groupId>${p.groupId}</groupId>
    <artifactId>${p.artifactId}</artifactId>
    <version>${p.version}</version>
<#if p.url?has_content>
    <url>${p.url?xml}</url>
</#if>
    <licenses>
<#list entry.getValue() as l>
      <license><name>${l?xml}</name></license>
</#list>
    </licenses>
  </dependency>
</#list>
</dependencies>