/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (check-reuse.groovy) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */

/*-
  This script checks that the copyright attributions are complete, both at file level (via
  `reuse lint` command) and at project level (components listed in `THIRD-PARTY-NOTICES` file are
  checked against the codebase, thus preventing attribution drift), and that no asset is under a
  forbidden license.

  The evaluation of `THIRD-PARTY-NOTICES` file is controlled via `check-reuse.yml` configuration
  file; when all the intended copyright attribution entries are inside `THIRD-PARTY-NOTICES`, any
  residual entry can be suppressed via `test.expected.update` system property (the corresponding
  entries will be registered in `third-party-*.expected.txt` files).

  REQUIRES:
  - org.apache.groovy:groovy-json
  - org.apache.groovy:groovy-yaml
  - org.pdfclown:pdfclown-common-util
  - python 3.13+
  - reuse 6.2+ (python package)

  All the configuration is under `/src/conf/check-reuse/`:
  - `check-reuse.yml`: script settings:
    - excludes: list of GLOBs to exclude certain files or subdirectories from SPDX evaluation for
      entry matching in `THIRD-PARTY-NOTICES` file
    - forbidden-licenses: list of wildcard expressions representing SPDX license identifiers the
      project assets are not allowed to be under
  - `third-party-missing.expected.txt`: automatically generated
  - `third-party-unexpected.expected.txt`: automatically generated
 */

// REUSE-IgnoreStart

import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN
import static org.pdfclown.common.util.Objects.anyThat
import static org.pdfclown.common.util.Strings.EMPTY
import static org.pdfclown.common.util.Strings.S

import groovy.json.JsonSlurper
import groovy.yaml.YamlSlurper
import java.util.function.Predicate
import java.util.regex.MatchResult
import java.util.regex.Pattern
import org.pdfclown.common.util.regex.Patterns

def rootDir = session.request.multiModuleProjectDirectory
if (project.basedir != rootDir) {
  log.info("To execute on root module only -- execution SKIPPED")
  return
}

// Configuration.
def confDir = new File(rootDir, 'src/conf/check-reuse'); confDir.mkdirs()
def configFile = new File(confDir, 'check-reuse.yml')
def config = [
    excludes: []
] + ((configFile.exists() ? new YamlSlurper().parse(configFile) : null) ?: [:])
Predicate<String> isExcluded = config.excludes
    ? Pattern.compile(config.excludes.collect { "(?:${Patterns.globToRegex(it)})" }.join('|'))
    .asMatchPredicate()
    : { String s -> false } as Predicate<String>

def forbiddenLicenses = config.'forbidden-licenses' ?: []
def licenseEvaluator = LicenseExpression.Evaluator.forbidden(
    forbiddenLicenses.collect { "${Patterns.wildcardToRegex(it)}" })

// --- 1. Project compliance with the REUSE Specification ------------------------------------------

// Generate the SPDX report!
def sbomFile = new File(rootDir, "target/sbom.json")
sbomFile.parentFile.mkdirs()
def proc = new ProcessBuilder('reuse', 'lint', '--json')
    .directory(rootDir)
    .redirectOutput(sbomFile)
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start()
if (proc.waitFor() != 0) {
  // Re-run in plain mode to get human-readable output!
  def out = new StringBuilder()
  proc = new ProcessBuilder('reuse', 'lint')
      .directory(rootDir)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
  proc.waitForProcessOutput(out, null)
  throw new IllegalStateException("`reuse lint --json` FAILED:\n${out}")
}
log.debug("${sbomFile} GENERATED")

log.info("")
log.info("[OK] This project is compliant with the REUSE Specification.")

// -------------------------------------------------------------------------------------------------

def report = new JsonSlurper().parse(sbomFile)

// Collect all reported files!
def allFiles = []
allFiles.addAll(report.files)

// Extract expected copyright metadata from each file!
def forbiddenFiles = []
def brokenFiles = []
def expectedCopyrights = [] as Set
allFiles.each { file ->
  def exprs = (file.spdx_expressions ?: []).collect {
    it instanceof Map ? it.value : it.toString()
  }
  if (exprs) {
    try {
      /*
        NOTE: Multiple expressions on one file are combined with AND, so a forbidden license is
        required if any single expression requires it.
       */
      if (exprs.any { !LicenseExpression.of(it).evaluate(licenseEvaluator) }) {
        forbiddenFiles << [path: file.path, exprs: exprs]
      }
    } catch (IllegalStateException ex) {
      brokenFiles << [path: file.path, exprs: exprs, error: ex.message]
    }
  }

  if (isExcluded.test(file.path))
    return

  file.copyrights.each {
    def copyrightText = it['value'].toString()
        .replace("Copyright ", "")
        .replace("(C)", "")
        .replace("©", "")
        .replace("SPDX-FileCopyrightText:", "")
        .replace("SPDX-SnippetCopyrightText:", "")
        .trim()
    if (copyrightText) {
      expectedCopyrights.add(copyrightText)
    }
  }
}

// --- 2. Forbidden licenses check -----------------------------------------------------------------

def forbiddenLicensesText = forbiddenLicenses.join(', ')
if (forbiddenFiles) {
  def sb = new StringBuilder()
  sb << "\nThis project contains ${forbiddenFiles.size()} assets under forbidden licenses " +
      "(${forbiddenLicensesText}):\n"
  forbiddenFiles.each { sb << "  - ${it.path} (${it.exprs})\n" }
  throw new IllegalStateException(sb.toString())
} else if (brokenFiles) {
  def sb = new StringBuilder()
  sb << "\nThis project contains ${brokenFiles.size()} assets whose license evaluation failed:\n"
  brokenFiles.each { sb << "  - ${it.path} (${it.exprs})\n" }
  throw new IllegalStateException(sb.toString())
}

log.info("[OK] None of the ${allFiles.size()} assets in this project is under forbidden licenses " +
    "(${forbiddenLicensesText}).")

// --- 3. `THIRD-PARTY-NOTICES` file entries sync to actual project SPDX ---------------------------

// Extract actual copyright metadata from `THIRD-PARTY-NOTICES`!
def thirdPartyNoticesFile = new File(rootDir, "THIRD-PARTY-NOTICES.txt")
def actualCopyrights = new LinkedHashSet<String>() // Keeps insertion order.
thirdPartyNoticesFile.eachLine {
  def m = it =~ /(?i)^\s*Copyright:\s*(.+?)\s*$/
  if (m.find()) {
    actualCopyrights << m.group(1)
  }
}

// Diff actual end expected copyright metadata!
def expectedMissingFile = new File(confDir, 'third-party-missing.expected.txt')
def expectedMissing = expectedMissingFile.exists()
    ? (expectedMissingFile.readLines() as Set)
    : ([] as Set)
def expectedUnexpectedFile = new File(confDir, 'third-party-unexpected.expected.txt')
def expectedUnexpected = expectedUnexpectedFile.exists()
    ? (expectedUnexpectedFile.readLines() as Set)
    : ([] as Set)
def missing = expectedCopyrights - actualCopyrights - expectedMissing
def unexpected = actualCopyrights - expectedCopyrights - expectedUnexpected
if (missing || unexpected) {
  def sb = new StringBuilder()
  sb << "\nCopyright entries in `${thirdPartyNoticesFile.name}` DO NOT correspond to actual ones:\n"
  if (missing) {
    // EXPECTED missing?
    if (session.userProperties['test.expected.update']) {
      expectedMissing += missing
      expectedMissingFile.text = expectedMissing.join('\n')
      missing = null
    }
    // UNEXPECTED missing.
    else {
      sb << "  MISSING (${missing.size()}):\n"
      missing.sort().each { sb << "    - $it\n" }
    }
  }
  if (unexpected) {
    // EXPECTED unexpected?
    if (session.userProperties['test.expected.update']) {
      expectedUnexpected += unexpected
      expectedUnexpectedFile.text = expectedUnexpected.join('\n')
      unexpected = null
    }
    // UNEXPECTED unexpected.
    else {
      sb << "  UNEXPECTED (${unexpected.size()}):\n"
      unexpected.sort().each { sb << "    + $it\n" }
    }
  }
  if (missing || unexpected)
    throw new IllegalStateException(sb.toString())
}

def matching = actualCopyrights.intersect(expectedCopyrights) // true positives
log.info("""[OK] Copyright entries (${matching.size()}) in `${thirdPartyNoticesFile.name}` \
correspond to actual ones (${expectedMissing.size() + expectedUnexpected.size()} others ignored). \
BEWARE: Since REUSE metadata doesn't track the third-party component associated to a copyright \
attribution, false negatives may occur in case the copyright happens to be the same across \
multiple components: always keep track of components as soon as they are added to or removed from \
this project!""")

/*
  TODO: [util:0.9.0] remove and replace its usage with Spdx.LicenseExpression, also changing on call
  site the caught exception to `ParseException`.
*/
/**
 * <a href="https://spdx.github.io/spdx-spec/v2.3.1/SPDX-license-expressions/">SPDX license
 * expression (SPDX 2.3)</a>.
 *
 * @author Stefano Chizzolini
 */
class LicenseExpression {
  /**
   * SPDX license evaluator.
   *
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  public interface Evaluator {
    /**
     * Creates an evaluator that allows only the given licenses.
     *
     * @param licenses
     *          (regex) Allowed licenses.
     */
    static Evaluator allowed(Collection<String> licenses) {
      List<Pattern> licensePatterns = Evaluator.compile(licenses);
      return ($licenseId, $exceptionId) -> licensePatterns.stream()
          .anyMatch(it -> it.matcher($licenseId).matches());
    }

    /**
     * Creates an evaluator that allows any but the given licenses.
     *
     * @param licenses
     *          (regex) Forbidden licenses.
     */
    static Evaluator forbidden(Collection<String> licenses) {
      List<Pattern> licensePatterns = Evaluator.compile(licenses);
      return ($licenseId, $exceptionId) -> licensePatterns.stream()
          .noneMatch(it -> it.matcher($licenseId).matches());
    }

    private static List<Pattern> compile(Collection<String> licenses) {
      return licenses.stream()
          .map(it -> Pattern.compile(it, Pattern.CASE_INSENSITIVE))
          .toList();
    }

    boolean test(String licenseId, String exceptionId);
  }

  /**
   * {@link LicenseExpression} evaluation context.
   *
   * @author Stefano Chizzolini
   */
  protected static class Context {
    final Evaluator evaluator;
    int position;
    final List<String> tokens;

    public Context(List<String> tokens, Evaluator evaluator) {
      this.tokens = tokens;
      this.evaluator = evaluator;
    }

    public String consume() {
      checkPosition();
      return tokens.get(position++);
    }

    public String current() {
      checkPosition();
      return tokens.get(position);
    }

    /**
     * @implNote For leniency, all tokens are considered case-insensitive, despite the
     *           specification indicates that the operators should be matched in a case-sensitive
     *           manner.
     */
    public boolean is(String token) {
      return !isEnded() && tokens.get(position).equalsIgnoreCase(token);
    }

    public boolean isEnded() {
      return position >= tokens.size();
    }

    public boolean moveNext() {
      checkPosition();
      return position++ < tokens.size();
    }

    public int position() {
      return position;
    }

    private void checkPosition() {
      if (isEnded())
        throw new IllegalStateException("Unexpected end of expression");
    }
  }

  private static final String TOKEN__AND = "AND";
  private static final String TOKEN__GROUP_BEGIN = S + ROUND_BRACKET_OPEN;
  private static final String TOKEN__GROUP_END = S + ROUND_BRACKET_CLOSE;
  private static final String TOKEN__OR = "OR";
  private static final String TOKEN__WITH = "WITH";

  public static LicenseExpression of(String expr) {
    return new LicenseExpression(expr);
  }

  private final List<String> tokens;

  protected LicenseExpression(String expr) {
    this.tokens = Pattern.compile("\\(|\\)|[^\\s()]+")
        .matcher(expr)
        .results()
        .map(MatchResult::group)
        .toList();
  }

  public boolean evaluate(Evaluator evaluator) {
    var context = createContext(evaluator);
    var ret = evalOr(context);
    if (!context.isEnded())
      throw new IllegalStateException("Unexpected token '${context.current()}'");

    return ret;
  }

  public List<String> getTokens() {
    return tokens;
  }

  protected Context createContext(Evaluator evaluator) {
    return new Context(tokens, evaluator);
  }

  protected boolean evalAnd(Context context) {
    var ret = evalOperand(context);
    while (context.is(TOKEN__AND)) {
      context.moveNext();
      /*
        WARNING: KEEP operand resolution separate from `&&` -- the operand MUST always be evaluated
        (no short-circuit!), otherwise its tokens would be left unconsumed.
      */
      var operand = evalOperand(context);
      ret = ret && operand;
    }
    return ret;
  }

  protected boolean evalOperand(Context context) {
    String t = context.consume();
    // Group?
    if (t.equalsIgnoreCase(TOKEN__GROUP_BEGIN)) {
      var ret = evalOr(context);
      if (!context.is(TOKEN__GROUP_END))
        throw new IllegalStateException("Missing token '${TOKEN__GROUP_END}'");

      context.moveNext();
      return ret;
    }
    // Identifier.
    else {
      // Not an identifier?
      if (anyThat(t, String::equalsIgnoreCase, TOKEN__GROUP_END, TOKEN__AND, TOKEN__OR, TOKEN__WITH))
        throw new IllegalStateException("Unexpected token '${t}'");

      /*
       * NOTE: In case of "X WITH some-exception":, `some-exception` is skipped, and the license
       * `X` is what gets checked.
       */
      var licenseException = EMPTY;
      if (context.is(TOKEN__WITH)) {
        if (!context.moveNext())
          throw new IllegalStateException("Missing exception after WITH");

        licenseException = context.consume();
      }
      return context.evaluator.test(t, licenseException);
    }
  }

  protected boolean evalOr(Context context) {
    var ret = evalAnd(context);
    while (context.is(TOKEN__OR)) {
      context.moveNext();
      /*
        WARNING: KEEP operand resolution separate from `||` -- the operand MUST always be evaluated
        (no short-circuit!), otherwise its tokens would be left unconsumed.
      */
      var operand = evalAnd(context);
      ret = ret || operand;
    }
    return ret;
  }
}

// REUSE-IgnoreEnd
