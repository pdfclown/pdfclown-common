/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Xmls.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.xml;

import static java.util.Objects.checkIndex;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.util.Conditions.requireState;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unsupported;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.Aggregations;
import org.pdfclown.common.util.annot.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * W3C DOM utilities.
 * <p>
 * All the factory methods herein return XML objects hardened against known security
 * vulnerabilities; for more information, see {@link Security}.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@SuppressWarnings("SameParameterValue")
public final class Xmls {
  /**
   * Document factory profile.
   * <p>
   * Useful for common parsing configurations.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  public enum DocumentFactoryProfile implements UnaryOperator<DocumentBuilderFactory> {
    /**
     * Compact document.
     * <p>
     * Operations applied to parsed documents:
     * </p>
     * <ul>
     * <li>{@linkplain DocumentBuilderFactory#setIgnoringElementContentWhitespace(boolean) strip
     * ignorable whitespace}</li>
     * <li>{@linkplain DocumentBuilderFactory#setIgnoringComments(boolean) strip comment nodes}</li>
     * <li>{@linkplain DocumentBuilderFactory#setCoalescing(boolean) merge CDATA nodes into adjacent
     * text nodes}</li>
     * </ul>
     */
    COMPACT($ -> {
      $.setIgnoringElementContentWhitespace(true);
      $.setCoalescing(true);
      $.setIgnoringComments(true);
    });

    private final Consumer<DocumentBuilderFactory> operation;

    DocumentFactoryProfile(Consumer<DocumentBuilderFactory> operation) {
      this.operation = operation;
    }

    @Override
    public DocumentBuilderFactory apply(DocumentBuilderFactory factory) {
      operation.accept(factory);
      return factory;
    }
  }

  /**
   * XML feature setter.
   *
   * @param <T>
   *          Value type.
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  public interface FeatureSetter<T> extends FailableBiConsumer<String, T, Exception> {
  }

  /**
   * XML hardening utilities.
   * <p>
   * They are implicitly applied to all the XML objects returned by {@link Xmls} factory methods to
   * harden them against known security vulnerabilities; they can be explicitly called by users to
   * modify the settings of any applicable XML object.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  public static final class Security {
    /**
     * External entity mode.
     *
     * @author Stefano Chizzolini
     */
    public enum ExternalEntityMode {
      /**
       * Fully disables DOCTYPEs.
       * <p>
       * This is the primary defense: if DOCTYPEs are disallowed, almost all XML entity attacks are
       * prevented.
       * </p>
       */
      NONE,
      /**
       * Enables external DOCTYPEs support, along with inline.
       * <p>
       * <span class="warning">WARNING: This is dangerous; it MUST be accompanied by whitelisting
       * (for example, through
       * {@link org.xml.sax.XMLReader#setEntityResolver(EntityResolver)})</span>.
       * </p>
       */
      EXTERNAL_DTD,
      /**
       * Enables inline DOCTYPEs support only.
       * <p>
       * <span class="important">IMPORTANT: "If, for some reason, support for inline DOCTYPEs are a
       * requirement, then [...] beware that
       * <a href="https://cwe.mitre.org/data/definitions/918.html">SSRF attacks</a> and
       * denial-of-service attacks (such as billion laughs or decompression bombs via jar protocol)
       * are a risk"</span> (Timothy Morgan, cited by <a href=
       * "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java">OWASP
       * - XML External Entity Prevention Cheat Sheet</a>).
       * </p>
       */
      INLINE_DTD_ONLY
    }

    /**
     * Parsing configuration.
     *
     * @author Stefano Chizzolini
     * @see Security#secureParsing(Object, FeatureSetter, ParsingSecuritySettings)
     */
    public static class ParsingSecuritySettings {
      ExternalEntityMode externalEntityMode = ExternalEntityMode.NONE;

      protected ParsingSecuritySettings() {
      }

      /**
       * External entity mode.
       */
      public ExternalEntityMode getExternalEntityMode() {
        return externalEntityMode;
      }

      /**
       * Sets {@linkplain #getExternalEntityMode() external entity mode}.
       */
      public ParsingSecuritySettings setExternalEntityMode(ExternalEntityMode value) {
        externalEntityMode = requireNonNull(value);
        return this;
      }
    }

    /**
     * Creates a new parsing configuration.
     *
     * @see #secureParsing(Object, FeatureSetter, ParsingSecuritySettings)
     */
    public static ParsingSecuritySettings parsingSettings() {
      return new ParsingSecuritySettings();
    }

    /**
     * Hardens a DOM factory.
     * <p>
     * Based on <a href=
     * "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j">OWASP
     * - XML External Entity Prevention Cheat Sheet</a>.
     * </p>
     */
    public static DocumentBuilderFactory secure(DocumentBuilderFactory obj) {
      secureParsing(obj, obj::setFeature, parsingSettings());
      /*
       * (see Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks")
       */
      obj.setXIncludeAware(false);
      obj.setExpandEntityReferences(false);
      return obj;
    }

    /**
     * Hardens a SAX parser factory.
     * <p>
     * Based on <a href=
     * "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j">OWASP
     * - XML External Entity Prevention Cheat Sheet</a>.
     * </p>
     */
    public static SAXParserFactory secure(SAXParserFactory obj) {
      secureParsing(obj, obj::setFeature, parsingSettings());
      return obj;
    }

    /**
     * Hardens a schema factory.
     * <p>
     * Based on <a href=
     * "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#schemafactory">OWASP
     * - XML External Entity Prevention Cheat Sheet</a>.
     * </p>
     */
    public static SchemaFactory secure(SchemaFactory obj) {
      var b = true;
      b &= secureBasic(obj::setFeature);
      b &= applyFeature(XMLConstants.ACCESS_EXTERNAL_DTD, EMPTY, obj::setProperty);
      b &= applyFeature(XMLConstants.ACCESS_EXTERNAL_SCHEMA, EMPTY, obj::setProperty);
      checkStatus(b, obj);
      return obj;
    }

    /**
     * Hardens an XML transformer factory.
     * <p>
     * Based on <a href=
     * "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#transformerfactory">OWASP
     * - XML External Entity Prevention Cheat Sheet</a>.
     * </p>
     */
    public static TransformerFactory secure(TransformerFactory obj) {
      var b = true;
      b &= secureBasic(obj::setFeature);
      b &= applyFeature(XMLConstants.ACCESS_EXTERNAL_DTD, EMPTY, obj::setAttribute);
      b &= applyFeature(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, EMPTY, obj::setAttribute);
      checkStatus(b, obj);
      return obj;
    }

    /**
     * Applies common security settings.
     * <p>
     * Based on <a href=
     * "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java">OWASP
     * - XML External Entity Prevention Cheat Sheet</a>.
     * </p>
     */
    public static boolean secureBasic(FeatureSetter<Boolean> setter) {
      return applyFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true, setter);
    }

    // SPDX-SnippetBegin
    // SPDX-SnippetCopyrightText: 2015-2022 Daniel Fickling, 2015 Patrick Wright
    // SPDX-License-Identifier: LGPL-3.0-only
    //
    // Source: https://github.com/danfickle/openhtmltopdf/blob/780ba564839f1ad5abfa5df12e4aebb9dd6782d2/openhtmltopdf-core/src/main/java/com/openhtmltopdf/resource/XMLResource.java#L150
    // SourceName: com.openhtmltopdf.resource.XMLResource.setSaxParserRequestedFeatures(..)
    // Changes: `settings` parameter to tune DTD surface.
    /**
     * Applies parsing security settings.
     * <p>
     * Based on <a href=
     * "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java">OWASP
     * - XML External Entity Prevention Cheat Sheet</a>.
     * </p>
     * <p>
     * Comprises {@link #secureBasic(FeatureSetter)}.
     * </p>
     */
    public static void secureParsing(Object obj, FeatureSetter<Boolean> setter,
        ParsingSecuritySettings settings) {
      var b = secureBasic(setter);
      b &= applyFeature("http://apache.org/xml/features/disallow-doctype-decl",
          settings.externalEntityMode == ExternalEntityMode.NONE, setter);
      b &= applyFeature("http://xml.org/sax/features/external-general-entities", false, setter);
      b &= applyFeature("http://xml.org/sax/features/external-parameter-entities", false, setter);
      b &= applyFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
          settings.externalEntityMode == ExternalEntityMode.EXTERNAL_DTD, setter);
      checkStatus(b, obj);
    }
    // SPDX-SnippetEnd

    private static void checkStatus(boolean status, Object obj) {
      if (!status) {
        log.error("XML External Entities (XXE) disablement FAILED for {} "
            + "-- the application may be vulnerable to XXE attacks", fqn(obj));
      }
    }

    private Security() {
    }
  }

  /**
   * Action returned by the mapper of a walk method.
   *
   * @author Stefano Chizzolini
   * @see Xmls#walkAncestors(Node, Function)
   * @see Xmls#walkDescendants(Node, Function)
   */
  public enum WalkAction {
    /**
     * Walk down to the descendants of the current node.
     */
    CONTINUE,
    /**
     * Exit from the walk.
     * <p>
     * This will be the result of the walk — useful to indicate the success of a node-matching
     * condition.
     * </p>
     */
    DONE,
    /**
     * Remove the current node.
     */
    REMOVE,
    /**
     * Walk to the next sibling, skipping the descendants of the current node.
     */
    SKIP
  }

  /**
   * XPath engine.
   * <p>
   * High-level wrapper of the {@linkplain #getBase() native engine}.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  public static class XPath {
    /**
     * {@link XPath} expression.
     *
     * @author Stefano Chizzolini
     */
    public static class Expression {
      private final XPathExpression base;

      protected Expression(XPathExpression base) {
        this.base = base;
      }

      /**
       * Native XPath expression.
       */
      public XPathExpression getBase() {
        return base;
      }

      /**
       * Gets the content corresponding to this expression.
       *
       * @param <R>
       *          Result type.
       * @param source
       *          Content source where this expression will be evaluated.
       * @return {@code null}, if not found.
       */
      public <R extends Node> @Nullable R node(Object source) {
        return get(source, XPathConstants.NODE);
      }

      /**
       * Gets the content corresponding to this expression.
       *
       * @param source
       *          Content source where this expression will be evaluated.
       * @return Empty, if not found.
       */
      public String nodeValue(Object source) {
        return requireState(get(source, XPathConstants.STRING));
      }

      /**
       * Gets the content corresponding to this expression.
       *
       * @param source
       *          Content source where this expression will be evaluated.
       * @return Empty, if not found.
       */
      public List<Node> nodes(Object source) {
        return asList(requireState(get(source, XPathConstants.NODESET)));
      }

      /**
       * Gets the content corresponding to this expression.
       *
       * @param <R>
       *          Result type (see {@code returnType} parameter).
       * @param source
       *          Content source where this expression will be evaluated.
       * @param returnType
       *          Result type expected to be returned by this expression, as defined in
       *          {@link XPathConstants}.
       * @return Result of evaluating this expression as an instance of {@code returnType}; if not
       *         found:
       *         <ul>
       *         <li>empty list — {@link XPathConstants#NODESET NODESET}</li>
       *         <li>{@code null} — {@link XPathConstants#NODE NODE}</li>
       *         <li>empty string — {@link XPathConstants#STRING STRING}</li>
       *         </ul>
       */
      @SuppressWarnings("unchecked")
      protected <R> @Nullable R get(Object source, QName returnType) {
        try {
          return (R) base.evaluate(source, returnType);
        } catch (XPathExpressionException ex) {
          throw runtime(ex);
        }
      }
    }

    /**
     * {@link XPath} namespaces mapping.
     *
     * @author Stefano Chizzolini
     */
    public static class Namespaces implements NamespaceContext {
      public static Namespaces of() {
        return new Namespaces();
      }

      private final Map<String, String> base = new HashMap<>();

      {
        register(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        register(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
      }

      protected Namespaces() {
      }

      @Override
      public String getNamespaceURI(String prefix) {
        return base.getOrDefault(requireNonNull(prefix), XMLConstants.NULL_NS_URI);
      }

      @Override
      public @Nullable String getPrefix(String namespaceURI) {
        return Aggregations.getKey(base, requireNonNull(namespaceURI));
      }

      @Override
      public Iterator<String> getPrefixes(String namespaceURI) {
        requireNonNull(namespaceURI);
        return base.entrySet().stream()
            .filter($ -> $.getValue().equals(namespaceURI))
            .map(Map.Entry::getKey)
            .iterator();
      }

      /**
       * Associates the prefix to the namespace URI.
       *
       * @return Self.
       */
      public Namespaces register(String prefix, String namespaceUri) {
        if (base.containsKey(prefix))
          throw wrongArg("prefix", prefix, "Already used for {} namespace",
              textLiteral(base.get(prefix)));

        base.put(prefix, namespaceUri);
        return this;
      }
    }

    private final javax.xml.xpath.XPath base;

    protected XPath(javax.xml.xpath.XPath base) {
      this.base = base;
    }

    /**
     * Compiles the expression for later evaluation.
     */
    public Expression compile(String expression) {
      try {
        return new Expression(base.compile(expression));
      } catch (XPathExpressionException ex) {
        throw runtime(ex);
      }
    }

    /**
     * Native XPath engine.
     */
    public javax.xml.xpath.XPath getBase() {
      return base;
    }

    /**
     * Gets the content corresponding to the expression.
     *
     * @param <R>
     *          Result type.
     * @param expression
     *          XPath expression.
     * @param source
     *          Content source where {@code expression} will be evaluated.
     * @return {@code null}, if not found.
     */
    public <R extends Node> @Nullable R node(String expression, Object source) {
      return get(expression, source, XPathConstants.NODE);
    }

    /**
     * Gets the content corresponding to the expression.
     *
     * @param expression
     *          XPath expression.
     * @param source
     *          Content source where {@code expression} will be evaluated.
     * @return Empty, if not found.
     */
    public String nodeValue(String expression, Object source) {
      return requireState(get(expression, source, XPathConstants.STRING));
    }

    /**
     * Gets the content corresponding to the expression.
     *
     * @param expression
     *          XPath expression.
     * @param source
     *          Content source where {@code expression} will be evaluated.
     * @return Empty, if not found.
     */
    public List<Node> nodes(String expression, Object source) {
      return asList(requireState(get(expression, source, XPathConstants.NODESET)));
    }

    /**
     * Gets the content corresponding to the expression.
     *
     * @param <R>
     *          Result type (see {@code returnType} parameter).
     * @param expression
     *          XPath expression.
     * @param source
     *          Content source where {@code expression} will be evaluated.
     * @param returnType
     *          Result type expected to be returned by {@code expression}, as defined in
     *          {@link XPathConstants}.
     * @return Result of evaluating {@code expression} as an instance of {@code returnType}; if not
     *         found:
     *         <ul>
     *         <li>empty list — {@link XPathConstants#NODESET NODESET}</li>
     *         <li>{@code null} — {@link XPathConstants#NODE NODE}</li>
     *         <li>empty string — {@link XPathConstants#STRING STRING}</li>
     *         </ul>
     */
    @SuppressWarnings("unchecked")
    protected <R> @Nullable R get(String expression, Object source, QName returnType) {
      try {
        return (R) base.evaluate(expression, source, returnType);
      } catch (XPathExpressionException ex) {
        throw runtime(ex);
      }
    }
  }

  private static final Logger log = LoggerFactory.getLogger(Xmls.class);

  private static final String PATTERN_GROUP__PSEUDO_ATTR__NAME = "name";
  private static final String PATTERN_GROUP__PSEUDO_ATTR__VALUE = "value";

  /**
   * Processing instruction's pseudo-attribute pattern {@biblio.spec XML-SS 3}.
   */
  private static final Pattern PATTERN__PSEUDO_ATTR = Pattern.compile("""
      (?<%s>[^\\s=]+)\\s?=\\s?(["'])\
      (?<%s>(?:(?!\\2).)*)\\2""".formatted(
      PATTERN_GROUP__PSEUDO_ATTR__NAME,
      PATTERN_GROUP__PSEUDO_ATTR__VALUE));

  /**
   * <a href="https://www.w3.org/1999/xhtml/">XHTML namespace</a>.
   */
  public static final String NS__XHTML = "http://www.w3.org/1999/xhtml";

  /**
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta">{@code <meta>}
   * (metadata element)</a> types.
   */
  private static final String[] META_TYPES = {
      // Document-level metadata.
      "name",
      // Pragma directive.
      "http-equiv",
      // Charset declaration.
      "charset",
      // User-defined metadata.
      "itemprop"
  };

  /**
   * Default XPath.
   * <p>
   * Useful for simple evaluations, when neither namespaces nor custom functionalities are required.
   * </p>
   */
  @Immutable
  public static final XPath XPATH = new XPath(new javax.xml.xpath.XPath() {
    /*
     * NOTE: For simplicity, the assumption here is that the default objects composing the base
     * XPath (such as `base.getNamespaceContext()`) are dummy, immutable instances -- it would make
     * no sense for users to hack them, so we avoid to wrap them in turn.
     */

    final javax.xml.xpath.XPath base = xpathFactory().newXPath();

    @Override
    public XPathExpression compile(String expression) throws XPathExpressionException {
      return base.compile(expression);
    }

    @Override
    public String evaluate(String expression, InputSource source) throws XPathExpressionException {
      return base.evaluate(expression, source);
    }

    @Override
    public Object evaluate(String expression, InputSource source, QName returnType)
        throws XPathExpressionException {
      return base.evaluate(expression, source, returnType);
    }

    @Override
    public String evaluate(String expression, Object item) throws XPathExpressionException {
      return base.evaluate(expression, item);
    }

    @Override
    public Object evaluate(String expression, Object item, QName returnType)
        throws XPathExpressionException {
      return base.evaluate(expression, item, returnType);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
      return base.getNamespaceContext();
    }

    @Override
    public XPathFunctionResolver getXPathFunctionResolver() {
      return base.getXPathFunctionResolver();
    }

    @Override
    public XPathVariableResolver getXPathVariableResolver() {
      return base.getXPathVariableResolver();
    }

    @Override
    public void reset() {
      // NOP: By definition, this immutable object cannot alter its state, so reset is redundant.
    }

    @Override
    public void setNamespaceContext(NamespaceContext nsContext) {
      throw unsupported();
    }

    @Override
    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
      throw unsupported();
    }

    @Override
    public void setXPathVariableResolver(XPathVariableResolver resolver) {
      throw unsupported();
    }
  });

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: 2015-2022 Daniel Fickling, 2015 Patrick Wright
  // SPDX-License-Identifier: LGPL-3.0-only
  //
  // Source: https://github.com/danfickle/openhtmltopdf/blob/780ba564839f1ad5abfa5df12e4aebb9dd6782d2/openhtmltopdf-core/src/main/java/com/openhtmltopdf/resource/XMLResource.java#L131
  // SourceName: com.openhtmltopdf.resource.XMLResource.trySetFeature(..)
  // Changes: `SetFeature` parameter type replaced by `FeatureSetter`.
  /**
   * Applies a feature via setter.
   *
   * @param name
   *          Feature name.
   * @param value
   *          Feature value.
   * @param setter
   *          Applies the feature to the target XML object.
   * @return Whether the operation succeeded.
   */
  public static <T> boolean applyFeature(String name, T value, FeatureSetter<T> setter) {
    try {
      setter.accept(name, value);
      return true;
    } catch (Exception ex) {
      log.warn("Feature '{}' NOT supported by XML processor ({})", name, ex.getMessage());
      return false;
    }
  }
  // SPDX-SnippetEnd

  /**
   * Gets the standard representation of the node list.
   */
  public static List<@NonNull Node> asList(NodeList nodes) {
    return nodes.getLength() > 0
        ? new AbstractList<>() {
          @Override
          public Node get(int index) {
            return nodes.item(checkIndex(index, nodes.getLength()));
          }

          @Override
          public int size() {
            return nodes.getLength();
          }
        }
        : List.of();
  }

  /**
   * Creates a hardened DOM factory.
   * <p>
   * Applies {@link Security#secure(DocumentBuilderFactory)}.
   * </p>
   *
   * @throws FactoryConfigurationError
   *           if the factory cannot be created.
   */
  public static DocumentBuilderFactory documentFactory() {
    return documentFactory(null);
  }

  /**
   * Creates a hardened DOM factory.
   * <p>
   * Applies {@link Security#secure(DocumentBuilderFactory)}.
   * </p>
   *
   * @param className
   *          Fully-qualified factory class name that provides the implementation of
   *          {@code DocumentBuilderFactory}.
   * @throws FactoryConfigurationError
   *           if the factory class cannot be created.
   */
  public static DocumentBuilderFactory documentFactory(@Nullable String className) {
    /*
     * NOTE: Unfortunately, `newInstance(..)` overloads have inconsistent semantics (`newInstance()`
     * isn't equivalent to `newInstance(null, null)` as one might reasonably expect), so we have to
     * branch our call.
     */
    var ret = className != null ? DocumentBuilderFactory.newInstance(className, null)
        : DocumentBuilderFactory.newInstance();
    ret.setNamespaceAware(true);
    return Security.secure(ret);
  }

  /**
   * Creates a hardened XML transformer for document fragments.
   * <p>
   * Applies {@link Security#secure(TransformerFactory)}.
   * </p>
   * <p>
   * The transformer is configured with the following output properties:
   * </p>
   * <ul>
   * <li>{@link OutputKeys#OMIT_XML_DECLARATION}</li>
   * </ul>
   *
   * @param style
   *          XSLT document to use ({@code null} for identity transformation).
   */
  public static Transformer fragmentTransformer(@Nullable Source style)
      throws TransformerConfigurationException, TransformerFactoryConfigurationError {
    var ret = newTransformer(style);
    ret.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    return ret;
  }

  /**
   * Gets the attribute value found walking across the inheritance line of the element.
   *
   * @param name
   *          Attribute name.
   * @return {@code null}, if the attribute has no inherited value.
   */
  public static @Nullable String getInheritableAttributeValue(String name, Element element) {
    return walkAncestors(element, $ -> $.getNodeType() == Node.ELEMENT_NODE
        ? stripToNull(((Element) $).getAttribute(name))
        : null);
  }

  /**
   * Gets the document metadata.
   *
   * @return Document metadata keyed by metadata type.
   */
  public static Map<String, Map<String, String>> getMetaInfo(Document document) {
    Map<String, Map<String, String>> ret = null;
    for (Node node : XPATH.nodes("/*/head/meta", document.getDocumentElement())) {
      Element meta = (Element) node;
      String content = meta.getAttribute("content");
      if (content.isEmpty()) {
        continue;
      }

      Map<String, String> subMap = null;
      String metaName = null;
      for (String metaType : META_TYPES) {
        if ((metaName = meta.getAttribute(metaType)).isEmpty()) {
          continue;
        }

        if (ret == null) {
          ret = new HashMap<>();
        }
        subMap = ret.computeIfAbsent(metaType, $k -> new HashMap<>());
        break;
      }
      if (subMap != null) {
        subMap.put(metaName, content);
      } else {
        log.warn("Unexpected meta element type: {}", toString(meta));
      }
    }
    return ret != null ? ret : Map.of();
  }

  /**
   * Gets the pseudo-attributes of the processing instruction {@biblio.spec XML-SS 3}.
   */
  public static Map<String, String> getPseudoAttributes(ProcessingInstruction pi) {
    Matcher m = PATTERN__PSEUDO_ATTR.matcher(pi.getData());
    var ret = new HashMap<String, String>();
    while (m.find()) {
      ret.put(m.group(PATTERN_GROUP__PSEUDO_ATTR__NAME),
          m.group(PATTERN_GROUP__PSEUDO_ATTR__VALUE));
    }
    return ret;
  }

  /**
   * Saves an XML document to stream.
   */
  public static void save(Document xml, OutputStream out) throws TransformerException {
    var output = new StreamResult(out);
    var input = new DOMSource(xml);
    transformerFactory().newTransformer().transform(input, output);
  }

  /**
   * Saves an XML document to file.
   */
  public static void save(Document xml, Path file) throws TransformerException, IOException {
    try (var out = Files.newOutputStream(file)) {
      save(xml, out);
    }
  }

  /**
   * Creates a hardened SAX parser factory.
   * <p>
   * Applies {@link Security#secure(SAXParserFactory)}.
   * </p>
   */
  public static SAXParserFactory saxParserFactory() {
    var ret = SAXParserFactory.newInstance();
    ret.setNamespaceAware(true);
    return Security.secure(ret);
  }

  /**
   * Creates a hardened schema factory.
   * <p>
   * Applies {@link Security#secure(SchemaFactory)}.
   * </p>
   */
  public static SchemaFactory schemaFactory() {
    var ret = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    return Security.secure(ret);
  }

  /**
   * Gets the unqualified (local) name of the node.
   */
  public static String simpleName(Node node) {
    var ret = node.getLocalName();
    if (ret == null) {
      ret = node.getNodeName();
    }
    return ret;
  }

  /**
   * Gets the string representation of the XML element.
   */
  public static String toString(Element element) {
    try {
      var ret = new StringWriter();
      fragmentTransformer(null).transform(new DOMSource(element), new StreamResult(ret));
      return ret.toString();
    } catch (TransformerFactoryConfigurationError | TransformerException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Creates a hardened XML transformer.
   * <p>
   * Applies {@link Security#secure(TransformerFactory)}.
   * </p>
   * <p>
   * The transformer is configured with the following output properties:
   * </p>
   * <ul>
   * <li>{@code "http://www.oracle.com/xml/is-standalone"} — Due to a regression in indenting
   * behavior of JDK's XSLT engine (Apache Xalan 2.7.1), newlines between the XML declaration and
   * the root element are omitted, making the serialized XML look odd to human readers; as a
   * workaround, this implementation-specific property brings back the original behavior (see
   * <a href=
   * "https://bugs.openjdk.org/browse/JDK-7150637?focusedId=12534763&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-12534763">JDK-7150637</a>)</li>
   * <li><code>"{http://xml.apache.org/xslt}indent-amount"</code> — {@linkplain OutputKeys#INDENT
   * Indentation} is applied with an amount of 2</li>
   * </ul>
   *
   * @param style
   *          XSLT document to use ({@code null} for identity transformation).
   */
  public static Transformer transformer(@Nullable Source style)
      throws TransformerConfigurationException, TransformerFactoryConfigurationError {
    var ret = newTransformer(style);
    {
      ret.setOutputProperty("http://www.oracle.com/xml/is-standalone", "yes");

      ret.setOutputProperty(OutputKeys.INDENT, "yes");
      ret.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    }
    return ret;
  }

  /**
   * Creates a hardened XML transformer factory.
   * <p>
   * Applies {@link Security#secure(TransformerFactory)}.
   * </p>
   */
  public static TransformerFactory transformerFactory() {
    return transformerFactory(null);
  }

  /**
   * Creates a hardened XML transformer factory.
   * <p>
   * Applies {@link Security#secure(TransformerFactory)}.
   * </p>
   */
  public static TransformerFactory transformerFactory(@Nullable String className) {
    /*
     * NOTE: Unfortunately, `newInstance(..)` overloads have inconsistent semantics (`newInstance()`
     * isn't equivalent to `newInstance(null, null)` as one might reasonably expect), so we have to
     * branch our call.
     */
    var ret = className != null ? TransformerFactory.newInstance(className, null)
        : TransformerFactory.newInstance();
    return Security.secure(ret);
  }

  /**
   * Walks across the ancestor-or-self axis of the node until the mapping succeeds (that is, a
   * non-null result is returned by the mapper).
   *
   * @param <R>
   *          Result type.
   * @param mapper
   *          (return either {@link WalkAction} or a user object).
   * @return {@code null}, if {@code node} is undefined or no mapping succeeded.
   */
  @SuppressWarnings("unchecked")
  public static <R> @Nullable R walkAncestors(@Nullable Node node,
      Function<Node, @Nullable Object> mapper) {
    if (node == null)
      return null;

    while (node != null) {
      var ret = mapper.apply(node);
      if (ret == WalkAction.REMOVE) {
        var oldNode = node;
        node = requireNonNull(oldNode.getParentNode());
        node.removeChild(oldNode);
        continue;
      } else if (ret != WalkAction.CONTINUE && ret != WalkAction.SKIP && ret != null)
        return (R) ret;

      node = node.getParentNode();
    }
    return null;
  }

  /**
   * Walks across the descendant axis of the node until the mapping succeeds (that is, a non-null
   * result is returned by the mapper).
   *
   * @param <R>
   *          Result type.
   * @param mapper
   *          (return either {@link WalkAction} or a user object).
   * @return {@code null}, if {@code node} is undefined or no mapping succeeded.
   */
  @SuppressWarnings("unchecked")
  public static <R> @Nullable R walkDescendants(@Nullable Node node,
      Function<Node, @Nullable Object> mapper) {
    if (node == null)
      return null;

    Node child = node.getFirstChild();
    while (child != null) {
      var ret = mapper.apply(child);
      if (ret == WalkAction.REMOVE) {
        var oldChild = child;
        child = oldChild.getNextSibling();
        node.removeChild(oldChild);
        continue;
      } else if (ret == WalkAction.SKIP || !child.hasChildNodes()) {
        // NOP
      } else if (ret == WalkAction.CONTINUE || ret == null) {
        ret = walkDescendants(child, mapper);
        if (ret != null)
          return (R) ret;
      } else
        return (R) ret;

      child = child.getNextSibling();
    }
    return null;
  }

  /**
   * Loads a hardened XML document.
   * <p>
   * Applies {@link Security#secure(DocumentBuilderFactory)}.
   * </p>
   */
  public static Document xml(InputStream in) throws IOException, SAXException {
    return xml(in, documentFactory());
  }

  /**
   * Loads an XML document.
   *
   * @param factory
   *          <span class="important">IMPORTANT: It is caller's responsibility to ensure this
   *          factory is {@link Security#secure(DocumentBuilderFactory) hardened}</span>.
   */
  public static Document xml(InputStream in, DocumentBuilderFactory factory)
      throws IOException, SAXException {
    requireNonNull(in, "`in`");
    requireNonNull(factory, "`factory`");

    try {
      var builder = factory.newDocumentBuilder();
      builder.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException ex) throws SAXException {
          throw ex;
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
          throw ex;
        }

        @Override
        public void warning(SAXParseException ex) {
          log.warn(ex.getMessage());
        }
      });
      return builder.parse(in);
    } catch (ParserConfigurationException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Loads a hardened XML document.
   * <p>
   * Applies {@link Security#secure(DocumentBuilderFactory)}.
   * </p>
   */
  public static Document xml(Path file) throws IOException, SAXException {
    return xml(file, documentFactory());
  }

  /**
   * Loads an XML document.
   *
   * @param factory
   *          <span class="important">IMPORTANT: It is caller's responsibility to ensure this
   *          factory is {@link Security#secure(DocumentBuilderFactory) hardened}</span>.
   */
  public static Document xml(Path file, DocumentBuilderFactory factory)
      throws IOException, SAXException {
    requireNonNull(file, "`file`");
    requireNonNull(factory, "`factory`");

    try (var in = Files.newInputStream(file)) {
      return xml(in, factory);
    }
  }

  /**
   * Creates a hardened XPath engine.
   *
   * @param namespaces
   *          Namespace context ({@code null}, for namespace-unaware XPath evaluation).
   * @see XPath.Namespaces
   */
  public static XPath xpath(@Nullable NamespaceContext namespaces) {
    var ret = xpathFactory().newXPath();
    if (namespaces != null) {
      ret.setNamespaceContext(namespaces);
    }
    return new XPath(ret);
  }

  /**
   * Creates a hardened XPath factory.
   * <p>
   * Applies {@link Security#secureBasic(FeatureSetter)}.
   * </p>
   */
  public static XPathFactory xpathFactory() {
    var ret = XPathFactory.newInstance();
    Security.secureBasic(ret::setFeature);
    return ret;
  }

  private static Transformer newTransformer(@Nullable Source style)
      throws TransformerConfigurationException, TransformerFactoryConfigurationError {
    var factory = transformerFactory();
    /*
     * NOTE: Unfortunately, `newTransformer(..)` overloads have inconsistent semantics
     * (`newTransformer()` isn't equivalent to `newTransformer(null)` as one might reasonably
     * expect), so we have to branch our call.
     */
    return style != null ? factory.newTransformer(style) : factory.newTransformer();
  }

  private Xmls() {
  }
}
