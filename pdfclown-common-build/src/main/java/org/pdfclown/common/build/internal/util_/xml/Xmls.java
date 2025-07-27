/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Xmls.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.xml;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Objects.nonNull;
import static org.pdfclown.common.build.internal.util_.Objects.toLiteralString;
import static org.pdfclown.common.build.internal.util_.Strings.strEmptyToNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util_.Aggregations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * W3C DOM utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Xmls {
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

  private static class XPathNamespaces implements NamespaceContext {
    Map<String, String> base = new HashMap<>();
    {
      register(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
      register(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
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

    public void register(String prefix, String namespaceUri) {
      if (base.containsKey(prefix))
        throw wrongArg("prefix", prefix, "Already used for {} namespace",
            toLiteralString(base.get(prefix)));

      base.put(prefix, namespaceUri);
    }
  }

  private static final String PATTERN_GROUP__PSEUDO_ATTR__NAME = "name";

  private static final String PATTERN_GROUP__PSEUDO_ATTR__VALUE = "value";

  /**
   * Processing instruction's pseudo-attribute pattern {@biblio.spec XML-SS 3}.
   */
  private static final Pattern PATTERN__PSEUDO_ATTR = Pattern.compile(
      "(?<" + PATTERN_GROUP__PSEUDO_ATTR__NAME + ">[^\\s=]+)"
          + "\\s?=\\s?"
          + "([\"'])(?<" + PATTERN_GROUP__PSEUDO_ATTR__VALUE + ">(?:(?!\\2).)*)\\2");

  private static final ThreadLocal<XPath> XPATH = ThreadLocal.withInitial(() -> {
    var ret = XPathFactory.newInstance().newXPath();
    ret.setNamespaceContext(new XPathNamespaces());
    return ret;
  });

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
   * Gets the standard representation of the given node list.
   */
  public static List<@NonNull Node> asList(NodeList nodes) {
    return nodes.getLength() > 0
        ? new AbstractList<>() {
          @Override
          public Node get(int index) {
            return nonNull(nodes.item(index));
          }

          @Override
          public int size() {
            return nodes.getLength();
          }
        }
        : List.of();
  }

  /**
   * Evaluates the given XPath expression in the given context.
   *
   * @param <R>
   *          Result type (see {@code returnType} parameter).
   * @param expression
   *          XPath expression.
   * @param source
   *          Context where {@code expression} will be evaluated.
   * @param returnType
   *          Result type expected to be returned by {@code expression}, as defined in
   *          {@link XPathConstants}.
   * @return Result of evaluating {@code expression} as an instance of {@code returnType}; if not
   *         found:
   *         <ul>
   *         <li>{@link XPathConstants#NODESET NODESET}: empty list</li>
   *         <li>{@link XPathConstants#NODE NODE}: {@code null}</li>
   *         <li>{@link XPathConstants#STRING STRING}: empty string</li>
   *         </ul>
   */
  @SuppressWarnings("unchecked")
  public static <R> @Nullable R filter(String expression, Object source, QName returnType) {
    try {
      return (R) xpath().evaluate(expression, source, returnType);
    } catch (XPathExpressionException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Evaluates the given XPath expression in the given context.
   *
   * @param <R>
   *          Result type.
   * @param expression
   *          XPath expression.
   * @param source
   *          Context where {@code expression} will be evaluated.
   * @return {@code null}, if not found.
   */
  public static <R extends Node> @Nullable R filterNode(String expression, Object source) {
    return filter(expression, source, XPathConstants.NODE);
  }

  /**
   * Evaluates the given XPath expression in the given context.
   *
   * @param expression
   *          XPath expression.
   * @param source
   *          Context where {@code expression} will be evaluated.
   * @return Empty, if not found.
   */
  public static String filterNodeValue(String expression, Object source) {
    return nonNull(filter(expression, source, XPathConstants.STRING));
  }

  /**
   * Evaluates the given XPath expression in the given context.
   *
   * @param expression
   *          XPath expression.
   * @param source
   *          Context where {@code expression} will be evaluated.
   * @return Empty, if not found.
   */
  public static List<@NonNull Node> filterNodes(String expression, Object source) {
    return asList(nonNull(filter(expression, source, XPathConstants.NODESET)));
  }

  /**
   * Finds the first node matching the given node name which is a descendant of the given source
   * node.
   *
   * @param <R>
   *          Result type.
   * @return {@code null}, if not found.
   */
  public static <R extends Node> @Nullable R findNode(String nodeName, Node source) {
    return walkDescendants(source, $ -> $.getNodeName().equals(nodeName) ? $ : WalkAction.CONTINUE);
  }

  /**
   * Finds the nodes matching the given node name which are descendants of the given source node.
   * <p>
   * NOTE: Matched nodes are not traversed for nested matches.
   * </p>
   *
   * @return Empty, if not found.
   */
  public static List<@NonNull Node> findNodes(String nodeName, Node source) {
    var ret = new ArrayList<@NonNull Node>();
    walkDescendants(source,
        $ -> {
          if ($.getNodeName().equals(nodeName)) {
            ret.add($);
            return WalkAction.SKIP;
          } else
            return WalkAction.CONTINUE;
        });
    return ret;
  }

  /**
   * Creates a new XML transformer for document fragments.
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
   * Gets the value of the given attribute walking across the inheritance line of the given element.
   *
   * @return {@code null}, if the attribute has no inherited value.
   */
  public static @Nullable String getInheritableAttributeValue(String name, Element element) {
    return walkAncestors(element, $ -> $.getNodeType() == Node.ELEMENT_NODE
        ? strEmptyToNull(((Element) $).getAttribute(name))
        : null);
  }

  /**
   * Gets the metadata of the given document as maps by metadata type.
   */
  public static Map<String, Map<String, String>> getMetaInfo(Document document) {
    Map<String, Map<String, String>> ret = null;
    for (Node node : filterNodes("/*/head/meta", nonNull(document.getDocumentElement()))) {
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
        //TODO:log unexpected meta element type.
      }
    }
    return ret != null ? ret : Map.of();
  }

  /**
   * Gets the pseudo-attributes of the given processing instruction {@biblio.spec XML-SS 3}.
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
   * Gets the unqualified (local) name of the given node.
   */
  public static String simpleName(Node node) {
    var ret = node.getLocalName();
    if (ret == null) {
      ret = node.getNodeName();
    }
    return ret;
  }

  /**
   * Gets the string representation of the given XML element.
   */
  public static String toString(Element element) {
    try {
      var ret = new StringWriter();
      fragmentTransformer(null).transform(new DOMSource(element), new StreamResult(ret));
      return ret.toString();
    } catch (TransformerFactoryConfigurationError | TransformerException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Creates a new XML transformer for documents.
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
   * Walks along the ancestor-or-self axis of the given node until the mapping succeeds (ie, a
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
   * Walks along the descendant axis of the given node until the mapping succeeds (ie, a non-null
   * result is returned by the mapper).
   *
   * @param <R>
   *          Result type.
   * @param mapper
   *          (return either {@link WalkAction} or a user object).
   * @return {@code null}, if no mapping succeeded.
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
   * Loads the given XML document.
   */
  public static Document xml(InputStream in) throws IOException, SAXException {
    return xml(in, (Source) null);
  }

  /**
   * Loads the given XML document.
   */
  public static Document xml(InputStream in, DocumentBuilderFactory factory)
      throws IOException, SAXException {
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
        public void warning(SAXParseException exception) {
          // TODO log warning
        }
      });
      return builder.parse(in);
    } catch (ParserConfigurationException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Loads the given XML document.
   */
  public static Document xml(InputStream in, @Nullable Source xsd)
      throws IOException, SAXException {
    var factory = DocumentBuilderFactory.newInstance();
    {
      factory.setIgnoringElementContentWhitespace(true);
      factory.setCoalescing(true);
      factory.setIgnoringComments(true);
      if (xsd != null) {
        factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            .newSchema(xsd));
        factory.setNamespaceAware(true);
      }
    }
    return xml(in, factory);
  }

  /**
   * Loads the given XML document.
   */
  public static Document xml(Path file) throws IOException, SAXException {
    return xml(file, (Source) null);
  }

  /**
   * Loads the given XML document.
   */
  public static Document xml(Path file, DocumentBuilderFactory factory)
      throws IOException, SAXException {
    try (var in = new FileInputStream(file.toFile())) {
      return xml(in, factory);
    }
  }

  /**
   * Loads the given XML document.
   */
  public static Document xml(Path file, @Nullable Source xsd) throws IOException, SAXException {
    try (var in = new FileInputStream(file.toFile())) {
      return xml(in, xsd);
    }
  }

  @SuppressWarnings("null")
  public static XPath xpath() {
    return XPATH.get();
  }

  /*
   * FIXME: static configuration of shared object (xpath) is an anti-pattern: move ALL xpath-related
   * methods to dedicated instance
   */
  /**
   * Registers a namespace prefix for xpath expressions.
   */
  public static void xpathNS(String prefix, String namespaceUri) {
    ((XPathNamespaces) xpath().getNamespaceContext()).register(prefix, namespaceUri);
  }

  private static Transformer newTransformer(@Nullable Source style)
      throws TransformerConfigurationException, TransformerFactoryConfigurationError {
    var factory = TransformerFactory.newInstance();
    /*
     * NOTE: Unfortunately, newTransformer(..) overloads have inconsistent semantics
     * (newTransformer() isn't equivalent to newTransformer(null) as one might reasonably expect),
     * so we have to branch our call.
     */
    return style != null ? factory.newTransformer(style) : factory.newTransformer();
  }

  private Xmls() {
  }
}
