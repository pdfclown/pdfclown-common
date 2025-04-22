/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (MavenDescriptorMojo.java) is part of pdfclown-common-maven-plugin module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.maven;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.maven.Util.PLUGIN_KEY__PDFCLOWN_COMMON;
import static org.pdfclown.common.maven.Util.pluginTag;
import static org.pdfclown.common.maven.util.MavenModels.PACKAGING__POM;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.COLON;
import static org.pdfclown.common.util.Strings.COMMA;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Strings.PIPE;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.regex.Patterns.wildcardToRegex;
import static org.pdfclown.common.util.system.Configs.parseStrList;
import static org.pdfclown.common.util.xml.Xmls.transformer;
import static org.pdfclown.common.util.xml.Xmls.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.pdfclown.common.util.ParamMessage;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Artifact metadata generator.
 * <p>
 * Substitutes the {@code addMavenDescriptor} feature of the
 * <a href="https://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver component</a>
 * providing a much more tidy and consistent description of the artifact, which conforms to the
 * <a href= "https://central.sonatype.org/publish/requirements/#required-pom-metadata">standard POM
 * metadata requirements for publishing</a>, without the noise of build configuration details (Maven
 * Archiver copies the raw {@code pom.xml} file as-is, whilst this implementation filters just the
 * relevant metadata and normalizes its formatting).
 * </p>
 * <p>
 * The following files are generated in
 * <code>"${project.build.outputDirectory}/META-INF/maven/${project.groupId}/${project.artifactId}"</code>
 * for inclusion in the project artifact:
 * </p>
 * <ul>
 * <li>{@code pom.properties}</li>
 * <li>{@code pom.xml}</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
@Mojo(name = MavenDescriptorMojo.NAME, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class MavenDescriptorMojo extends AbstractMojo {
  public static final String NAME = "normalizeMavenDescriptor";

  static final String CONTEXT_KEY__MAVEN_DESCRIPTOR =
      PLUGIN_KEY__PDFCLOWN_COMMON + COLON + "normalMavenDescriptor";
  static final String CONTEXT_KEY__NORMAL_MAVEN_DESCRIPTOR_GOAL_FILTER =
      PLUGIN_KEY__PDFCLOWN_COMMON + COLON + "normalMavenDescriptorGoalFilter";

  /*
   * TODO: The @Mojo annotation is not retained at runtime, so we have to declare its `defaultPhase`
   * value here for runtime reuse. However, due to a java language limitation, annotations cannot
   * reference a constant field pointing to an enum constant, only the enum constant itself, so we
   * are forced to redeclare it here -- really ugly, but the only alternative seems to be to extract
   * information from the plugin descriptor, quite overkill...
   */
  static final LifecyclePhase DEFAULT_PHASE = LifecyclePhase.GENERATE_RESOURCES;

  /**
   * Predefined goals for Maven descriptor replacement for metadata publishing.
   */
  private static final String INCLUDED_GOALS__PREDEFINED =
      "central-publishing-maven-plugin:publish" + COMMA
          + "maven-deploy-plugin:deploy" + COMMA
          + "maven-gpg-plugin:sign" + COMMA
          + "maven-install-plugin:install" + COMMA
          + "maven-jar-plugin:*jar*" + COMMA
          + "maven-javadoc-plugin:*jar*" + COMMA
          + "maven-source-plugin:*jar*";

  /**
   * List of comma-separated goals, each represented as a pair of artifactId and goal (eg,
   * {@code "maven-source-plugin:*jar*"} means that any goal whose name contains "jar" is selected),
   * which, for metadata publishing, need to replace the default Maven descriptor (ie, the project
   * POM as-is) with the normalized Maven descriptor generated by this goal.
   * <p>
   * Because of their ubiquity, the following goals are predefined:
   * </p>
   * <ul>
   * <li>{@code central-publishing-maven-plugin:publish}</li>
   * <li>{@code maven-deploy-plugin:deploy}</li>
   * <li>{@code maven-gpg-plugin:sign}</li>
   * <li>{@code maven-install-plugin:install}</li>
   * <li>{@code maven-jar-plugin:*jar*}</li>
   * <li>{@code maven-javadoc-plugin:*jar*}</li>
   * <li>{@code maven-source-plugin:*jar*}</li>
   * </ul>
   */
  @Parameter
  protected String includedGoals;

  private OffsetDateTime generationTime;
  private String generator;
  private Path outputDir;
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() {
    if (project.getPackaging().equals(PACKAGING__POM)) {
      getLog().info("Skipping normalized Maven descriptor build as POM package is intended to be "
          + "published as-is.");

      return;
    }

    initialize();

    /*
     * MAVEN DESCRIPTOR (target/pdfclown-common-maven-plugin/maven-descriptor.xml)
     */
    {
      File mavenDescriptorFile = writeMavenDescriptor();

      // Share the location of the normalized descriptor!
      project.setContextValue(CONTEXT_KEY__MAVEN_DESCRIPTOR, mavenDescriptorFile);
    }
  }

  private void initialize() {
    // Build the goal filter for Maven descriptor replacement for metadata publishing!
    {
      includedGoals = requireNonNullElse(includedGoals, EMPTY);

      var b = new StringBuilder();
      for (String includedGoal : parseStrList(INCLUDED_GOALS__PREDEFINED + COMMA + includedGoals)) {
        // Unqualified goal?
        if (includedGoal.indexOf(COLON) == INDEX__NOT_FOUND)
          throw runtime("Invalid goal in `includedGoals` configuration property: '{}' "
              + "(MUST be qualified by its plugin, eg 'foobar-maven-plugin:{}')", includedGoal,
              includedGoal);

        if (b.length() > 0) {
          b.append(PIPE);
        }
        b.append(ROUND_BRACKET_OPEN)
            .append(wildcardToRegex(includedGoal))
            .append(ROUND_BRACKET_CLOSE);
      }
      if (getLog().isDebugEnabled()) {
        getLog().debug(ParamMessage.format("[{}] {} regex: '{}'", pluginTag(project),
            CONTEXT_KEY__NORMAL_MAVEN_DESCRIPTOR_GOAL_FILTER, b.toString()));
      }

      project.setContextValue(CONTEXT_KEY__NORMAL_MAVEN_DESCRIPTOR_GOAL_FILTER,
          Pattern.compile(b.toString()).asMatchPredicate());
    }

    outputDir = Path.of(project.getBuild().getDirectory(), "pdfclown-common-maven-plugin")
        .normalize();
    try {
      Files.createDirectories(outputDir);
    } catch (IOException ex) {
      throw runtime(ex);
    }

    generator = project.getPlugin(PLUGIN_KEY__PDFCLOWN_COMMON).getId();
    generationTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
  }

  private Document loadEffectivePom() {
    // Generate the effective POM!
    var out = new StringWriter();
    {
      Model pom = project.getModel();
      try {
        new MavenXpp3Writer().write(out, pom);
      } catch (IOException ex) {
        throw runtime("Effective POM generation FAILED", ex);
      }
    }

    // Load the effective POM!
    try {
      return xml(IOUtils.toInputStream(out.toString(), UTF_8));
    } catch (IOException | SAXException ex) {
      throw runtime("Effective POM loading FAILED", ex);
    }
  }

  private File writeMavenDescriptor() {
    Document effectivePom = loadEffectivePom();

    File ret = outputDir.resolve("maven-descriptor.xml").toFile();

    getLog().info("Building normalized Maven descriptor: " + ret);

    try (var targetStream = new FileOutputStream(ret)) {
      var source = new DOMSource(effectivePom);
      var target = new StreamResult(targetStream);
      var style = new StreamSource(getClass().getResourceAsStream("maven-descriptor.xsl"));
      Transformer trasformer = transformer(style);
      {
        trasformer.setParameter("header", String.format(
            "\n  MAVEN DESCRIPTOR for %s:%s:%s\n\n"
                + "  Generated by %s on %s\n",
            project.getGroupId(), project.getArtifactId(), project.getVersion(), generator,
            generationTime));
      }
      trasformer.transform(source, target);
    } catch (TransformerException | IOException ex) {
      throw runtime("Maven descriptor ({}) generation FAILED", ret, ex);
    }
    return ret;
  }
}
