/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Extension.java) is part of pdfclown-common-maven-plugin module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.maven;

import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;

/**
 * pdfClown common extension for Maven.
 *
 * <h2>Usage</h2>
 *
 * <h3>Maven</h3><pre>
 *{@code <plugin>
 *  <groupId>org.pdfclown</groupId>
 *  <artifactId>pdfclown-common-maven-plugin</artifactId>
 *  <version>}${pdfclown.common.maven.version}{@code </version>
 *  <extensions>true</extensions>
 *  <executions>
 *    . . .
 *  </executions>
 *</plugin>}</pre>
 *
 * @author Stefano Chizzolini
 */
@Named("pdfclown-common")
@Singleton
public class Extension extends AbstractMavenLifecycleParticipant {
  private MavenDescriptorManager mavenDescriptorManager = new MavenDescriptorManager();

  @Override
  public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
    /*
     * NOTE: The original listener is wrapped in order not to disrupt existing wiring.
     */
    session.getRequest().setExecutionListener(new BaseExecutionListener(
        session.getRequest().getExecutionListener()) {
      @Override
      public void mojoFailed(ExecutionEvent event) {
        super.mojoFailed(event);

        mojoFinished(event);
      }

      @Override
      public void mojoStarted(ExecutionEvent event) {
        super.mojoStarted(event);

        mavenDescriptorManager.onMojoStart(event.getProject(), event.getMojoExecution());
      }

      @Override
      public void mojoSucceeded(ExecutionEvent event) {
        super.mojoSucceeded(event);

        mojoFinished(event);
      }

      @Override
      public void projectStarted(ExecutionEvent event) {
        super.projectStarted(event);

        mavenDescriptorManager.onProjectStart(event.getProject());
      }

      private void mojoFinished(ExecutionEvent event) {
        mavenDescriptorManager.onMojoEnd(event.getProject());
      }
    });
  }
}
