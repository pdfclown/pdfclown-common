/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (BaseExecutionListener.java) is part of pdfclown-common-maven-plugin module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.maven;

import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;

/**
 * Passes notifications to base listener.
 *
 * @author Stefano Chizzolini
 */
abstract class BaseExecutionListener extends AbstractExecutionListener {
  ExecutionListener base;

  protected BaseExecutionListener(ExecutionListener base) {
    this.base = base;
  }

  @Override
  public void forkedProjectFailed(ExecutionEvent event) {
    base.forkedProjectFailed(event);
  }

  @Override
  public void forkedProjectStarted(ExecutionEvent event) {
    base.forkedProjectStarted(event);
  }

  @Override
  public void forkedProjectSucceeded(ExecutionEvent event) {
    base.forkedProjectSucceeded(event);
  }

  @Override
  public void forkFailed(ExecutionEvent event) {
    base.forkFailed(event);
  }

  @Override
  public void forkStarted(ExecutionEvent event) {
    base.forkStarted(event);
  }

  @Override
  public void forkSucceeded(ExecutionEvent event) {
    base.forkSucceeded(event);
  }

  @Override
  public void mojoFailed(ExecutionEvent event) {
    base.mojoFailed(event);
  }

  @Override
  public void mojoSkipped(ExecutionEvent event) {
    base.mojoSkipped(event);
  }

  @Override
  public void mojoStarted(ExecutionEvent event) {
    base.mojoStarted(event);
  }

  @Override
  public void mojoSucceeded(ExecutionEvent event) {
    base.mojoSucceeded(event);
  }

  @Override
  public void projectDiscoveryStarted(ExecutionEvent event) {
    base.projectDiscoveryStarted(event);
  }

  @Override
  public void projectFailed(ExecutionEvent event) {
    base.projectFailed(event);
  }

  @Override
  public void projectSkipped(ExecutionEvent event) {
    base.projectSkipped(event);
  }

  @Override
  public void projectStarted(ExecutionEvent event) {
    base.projectStarted(event);
  }

  @Override
  public void projectSucceeded(ExecutionEvent event) {
    base.projectSucceeded(event);
  }

  @Override
  public void sessionEnded(ExecutionEvent event) {
    base.sessionEnded(event);
  }

  @Override
  public void sessionStarted(ExecutionEvent event) {
    base.sessionStarted(event);
  }
}
