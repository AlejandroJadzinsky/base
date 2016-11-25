package com.base.orm;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility to manage databases in test environments.
 */
public class DatabaseUtility {
  /** The logger. */
  private static Logger log = LoggerFactory.getLogger(DatabaseUtility.class);

  /** The default marker table. */
  private static final String MARKER_TABLE = "test_marker";

  /** The dialect place holder. */
  private static final String DIALECT_PLACE_HOLDER = "{dialect}";

  /** The {@link HibernateComponent} instance, never null after
   * initialization. */
  private HibernateComponent hibernateComponent;

  /** Constructor with mandatory parameters.
   *
   * @param theHibernateComponent the {@link HibernateComponent} instance,
   * cannot be null.
   */
  public DatabaseUtility(final HibernateComponent theHibernateComponent) {
    Validate.notNull(theHibernateComponent, "The HibernateComponent is null");
    hibernateComponent = theHibernateComponent;
  }

  /** Deletes the given entities.
   *
   * @param entities the entities represented by the given classes, cannot be
   * null.
   */
  public void delete(final Class<?>... entities) {
    Validate.notEmpty(entities, "No entities to delete");

    final Metadata metadata = hibernateComponent.getMetadata();
    List<String> statements = new ArrayList<>();
    Arrays.stream(entities)
        .forEach(entity -> statements.add("delete from "
            + metadata.getEntityBinding(entity.getCanonicalName()).getTable()
                .getName()));

    runSqlCommands(statements.toArray(new String[statements.size()]));
  }

  /** Regenerate the schema defined in the {@link #hibernateComponent}
   *  attribute.
   */
  public void regenerateDevelopmentDatabase() {
    assertDevelopmentDatabase();
    new SchemaExport()
        .setDelimiter(";")
        .setHaltOnError(true)
        .create(EnumSet.of(TargetType.DATABASE),
            hibernateComponent.getMetadata());
  }

  /** Generates an initialization script.
   *
   * @param schemaFile the file where the script is generated, cannot be null
   * nor empty.
   */
  public void generateSchemaScript(final String schemaFile) {
    Validate.notEmpty(schemaFile, "Schema file name is null or empty");

    String outputFile = processFileNameDialect(schemaFile);
    prepareFileEnvironment(outputFile);

    new SchemaExport()
        .setOutputFile(outputFile)
        .setDelimiter(";")
        .setFormat(true)
        .setHaltOnError(true)
        .createOnly(EnumSet.of(TargetType.SCRIPT),
            hibernateComponent.getMetadata());
  }

  /** Builds a valid file name for a creation schema script.
   *
   * @param fileName the proposed file name, cannot be null nor empty.
   *
   * @return a file name with the dialect replaced.
   */
  private String processFileNameDialect(final String fileName) {
    String outputFile = fileName;
    if (fileName.contains(DIALECT_PLACE_HOLDER)) {
      String[] choppedDialect = hibernateComponent.getDialect().split("\\.");
      String dialect = choppedDialect[choppedDialect.length - 1];
      outputFile = fileName.replace("{dialect}", dialect);
    }

    return outputFile;
  }

  /** If the file already exists, delete it. If the path to file doesn't exist
   * create it.
   */
  private void prepareFileEnvironment(final String fileName) {
    File file = new File(fileName);
    if (file.exists()) {
      file.delete();
    } else {
      File parent = file.getParentFile();
      if (parent != null && !parent.exists()) {
        parent.mkdirs();
      }
    }
  }

  /** Runs a set of sql sentences stored in a file.
   *
   * Sentences in the file are delimited by a line ending in ;.
   *
   * @param fileName the String with the file name.
   */
  public void runSqlScript(final String fileName) {
    Validate.notEmpty(fileName, "Script File Name is null or empty");

    log.trace("Entering runSqlSentences('" + fileName + "')");
    Validate.notNull(fileName, "The file name cannot be null.");

    File file = new File(fileName);
    if (file.exists()) {
      if (file.isDirectory()) {
        FilenameFilter filter = new RegexFileFilter(".*\\.sql");
        File[] sqlScripts = file.listFiles(filter);
        Arrays.stream(sqlScripts)
            .sorted()
            .forEach(this::runSqlScript);
      } else {
        runSqlScript(file);
      }
    } else {
      log.info(String.format(
          "SQL script file or directory '%s' does not exist", fileName));
    }

    log.trace("Leaving runSqlSentences");
  }

  /** Runs a set of sql sentences stored in a file.
   *
   * Sentences in the file are delimited by a line ending in ;.
   *
   * @param file the String with the file name.
   */
  public void runSqlScript(final File file) {
    Validate.notNull(file, "Script File is null");

    SqlScriptParser parser = new SqlScriptParser();
    runSqlCommands(parser.parse(file));
  }

  /** executes the given statements.
   *
   * @param commands the statements to run, cannot be null.
   */
  public void runSqlCommands(final String... commands) {
    Validate.notEmpty(commands, "No commands to run");

    try (Session session = hibernateComponent.getSessionFactory()
        .openSession()) {
      Transaction tx = session.beginTransaction();
      try (Connection connection = hibernateComponent.getDataSource()
          .getConnection()) {
        try (Statement statement = connection.createStatement()) {
          for (String sentence : commands) {
            statement.addBatch(StringEscapeUtils.unescapeJava(sentence));
          }
          statement.executeBatch();
        }
      } catch (SQLException ioe) {
        tx.rollback();
        log.error(ioe.getMessage());
        throw new RuntimeException(ioe);
      }
      tx.commit();
    }
  }

  /** Verifies if it is a development database, that is, if it has the mark
   * table or is in memory.
   *
   * It throws an exception if it is not a development database.
   *
   * @throws SQLException if a database access error occurs.
   */
  private void assertDevelopmentDatabase() {
    if (hibernateComponent.isInMemoryDatasource()) {
      // we assume that in memory data sources are never used in production
      return;
    }

    try (Connection connection = hibernateComponent.getDataSource()
        .getConnection()) {
      String queryMarkerTest = "select drop_database from " + MARKER_TABLE;
      try (Statement st = connection.createStatement()) {
        log.debug("Verifying if it is a test database");

        try (ResultSet rs = st.executeQuery(queryMarkerTest)) {
          String message = null;
          if (rs.next()) {
            message = rs.getString("drop_database");
          }
          if (message == null) {
            log.error("create table {} (drop_database varchar (50));",
                MARKER_TABLE);
            log.error("insert into {} values ('YES, DROP ME');", MARKER_TABLE);
            throw new RuntimeException("Marker table does not contain a row");
          }
          if (!message.equals("YES, DROP ME")) {
            log.error("create table {} (drop_database varchar (50));",
                MARKER_TABLE);
            log.error("insert into {} values ('YES, DROP ME');", MARKER_TABLE);
            throw new RuntimeException(
                "Marker table does not contain the correct row");
          }
        } catch (SQLException e) {
          log.error("An exception was caught selecting from {}. It is probable"
              + " because the table does not exist. Please create it with: ",
              MARKER_TABLE);
          log.error("create table {} (drop_database varchar (50));",
              MARKER_TABLE);
          log.error("insert into {} values ('YES, DROP ME');", MARKER_TABLE);

          System.out.println("An exception was caught selecting from "
           + MARKER_TABLE + ". It is probable because the table does"
           + " not exist. Please create it with:");
          System.out.println("create table " + MARKER_TABLE
           + "(drop_database varchar (50));");
          System.out.println("insert into " + MARKER_TABLE
           + " values ('YES, DROP ME');");
          throw e;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
