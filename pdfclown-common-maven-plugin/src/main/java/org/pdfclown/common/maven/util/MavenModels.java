/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (MavenModels.java) is part of pdfclown-common-maven-plugin module in pdfClown Common
  project (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.maven.util;

import static org.pdfclown.common.util.Strings.COLON;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.util.Optional;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jspecify.annotations.Nullable;

/**
 * @author Stefano Chizzolini
 */
public final class MavenModels {
  /**
   * @author Stefano Chizzolini
   */
  public static final class Config {
    /**
     */
    public static String getValue(String path, ConfigurationContainer container,
        Object defaultValue) {
      return getValue(path, (Xpp3Dom) container.getConfiguration(), defaultValue);
    }

    /**
     */
    public static String getValue(String path, @Nullable Xpp3Dom baseNode,
        Object defaultValue) {
      return tryNode(path, baseNode)
          .map(Xpp3Dom::getValue)
          .orElse(defaultValue.toString());
    }

    /**
     */
    public static Xpp3Dom node(String path, ConfigurationContainer container) {
      return node(path, root(container));
    }

    /**
     */
    public static Xpp3Dom node(String path, Xpp3Dom baseNode) {
      String[] pathParts = path.split("\\.");
      var node = baseNode;
      for (var pathPart : pathParts) {
        Xpp3Dom childNode = node.getChild(pathPart);
        if (childNode == null) {
          node.addChild(childNode = new Xpp3Dom(pathPart));
        }
        node = childNode;
      }
      return node;
    }

    /**
     */
    public static Xpp3Dom setValue(String path, ConfigurationContainer container, Object value) {
      return setValue(path, root(container), value);
    }

    /**
     */
    public static Xpp3Dom setValue(String path, Xpp3Dom baseNode, Object value) {
      var ret = node(path, baseNode);
      ret.setValue(value.toString());
      return ret;
    }

    /**
     */
    public static Optional<Xpp3Dom> tryNode(String path, ConfigurationContainer container) {
      return tryNode(path, (Xpp3Dom) container.getConfiguration());
    }

    /**
     */
    public static Optional<Xpp3Dom> tryNode(String path, @Nullable Xpp3Dom baseNode) {
      String[] pathParts = path.split("\\.");
      var node = baseNode;
      for (var pathPart : pathParts) {
        if (node == null)
          return Optional.empty();

        node = node.getChild(pathPart);
      }
      return Optional.ofNullable(node);
    }

    private static Xpp3Dom root(ConfigurationContainer container) {
      var ret = (Xpp3Dom) container.getConfiguration();
      if (ret == null) {
        container.setConfiguration(ret = new Xpp3Dom("configuration"));
      }
      return ret;
    }

    private Config() {
    }
  }

  public static final String PACKAGING__POM = "pom";

  private static final String PHASE__NONE = "none";

  private static final String PLUGIN_NAME_SUFFIX = "-maven-plugin";

  /**
   * Disables the given execution.
   *
   * @see #isEnabled(PluginExecution)
   */
  public static void disable(PluginExecution execution) {
    execution.setPhase(PHASE__NONE);
  }

  /**
   * Creates an execution associated to the given goal.
   */
  public static PluginExecution execution(String goal) {
    var ret = new PluginExecution();
    ret.addGoal(goal);
    return ret;
  }

  /**
   * Gets the fully-qualified name of the given goal.
   */
  public static String goalFqn(String goal, Plugin plugin) {
    return pluginQName(plugin) + COLON + goal;
  }

  /**
   * Gets whether the given plugin has enabled executions.
   */
  public static boolean hasExecution(@Nullable Plugin plugin) {
    return plugin != null && plugin.getExecutions().stream()
        .filter(MavenModels::hasGoals)
        .anyMatch(MavenModels::isEnabled);
  }

  /**
   * Gets whether the given execution has configured goals.
   */
  public static boolean hasGoals(PluginExecution execution) {
    return !execution.getGoals().isEmpty();
  }

  /**
   * Gets whether the given execution is enabled.
   *
   * @see #disable(PluginExecution)
   */
  public static boolean isEnabled(PluginExecution execution) {
    return !PHASE__NONE.equalsIgnoreCase(execution.getPhase());
  }

  /**
   */
  public static String pluginName(Plugin plugin) {
    /*
     * TODO: support standard maven plugin naming (maven-*-plugin)
     */
    return plugin.getArtifactId().replace(PLUGIN_NAME_SUFFIX, EMPTY);
  }

  /**
   */
  public static String pluginQName(Plugin plugin) {
    return pluginName(plugin) + COLON + plugin.getVersion();
  }

  private MavenModels() {
  }
}
