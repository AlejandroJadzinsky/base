package com.base.orm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Parses a text files consisting of sql sentences separated by ';'.
 *
 * This parses is very naive, it considers a line ending in ; as a sentence
 * separator. If the ; is followed by white space, it is not considered the end
 * of the line.
 */
public class SqlScriptParser {

  /** The class logger.
   */
  private static Logger log = LoggerFactory.getLogger(SqlScriptParser.class);

  /** Translate the file with the script Name into an Array of sentences to
   * execute.
   *
   * @param scriptName the file name with the script, cannot be null nor empty.
   *
   * @return an Array of sentences, never null.
   */
  public String[] parse(final String scriptName) {
    Validate.notNull(scriptName, "The sql file name to parse cannot be null.");
    return parse(new File(scriptName));
  }

  /** Construct a SqlSentencesParser.
   *
   * @param scriptFile The file to parse, it cannot be null.
   *
   * @return an Array of sentences, never null.
   */
  public String[] parse(final File scriptFile) {
    Validate.notNull(scriptFile, "The sql file to parse cannot be null.");
    log.trace("Parsing script file {}", scriptFile.getName());
    Scanner scanner = createScanner(scriptFile);
    return readSentences(scanner);
  }

  /** Creates a Scanner for the given File.
   *
   * @param scriptFile a File instance, cannot be null.
   *
   * @return a Scanner instance, never null.
   */
  private Scanner createScanner(final File scriptFile) {
    try {
      Scanner scanner = new Scanner(scriptFile);
      scanner.useDelimiter("\n");
      return scanner;
    } catch (IOException ioe) {
      log.error(ioe.getMessage());
      throw new RuntimeException(ioe);
    }
  }

  /** Obtains the next sentence in the file.
   *
   * @param scanner a Scanner instance, cannot be null.
   *
   * @return the next sentence in the file, or null if it reached the end.
   */
  private String[] readSentences(final Scanner scanner) {
    log.trace("Entering readSentences");
    List<String> result = new ArrayList<>();

    StringBuffer sentence = null;
    while (scanner.hasNext()) {
      String line = scanner.next().trim();
      // I read a line, initialize the StringBuffer.
      if (sentence == null) {
        // Skip empty lines between sentences.
        if (StringUtils.isEmpty(line)) {
          continue;
        }
        sentence = new StringBuffer();
      }
      if (line.endsWith(";")) {
        sentence.append(line);
        result.add(sentence.toString());
        sentence = null;
      } else {
        sentence.append(line);
        sentence.append("\n");
      }
    }

    return result.toArray(new String[result.size()]);
  }
}