package com.breskul.bibernate.query.hql;

import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;

import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.BiQLException;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;


/**
 * A utility class for converting BiQL (a hypothetical query language) to standard SQL queries. This class provides static methods to parse
 * BiQL strings and transform them into SQL syntax based on entity class mappings and field annotations. The conversion process takes into
 * account the structure and semantics of BiQL to ensure the generated SQL queries accurately represent the original intent.
 */
public class BiQLMapper {

  private static final Logger log = LoggerFactory.getLogger(BiQLMapper.class);

  private static final String BIQL_HAS_INCORRECT_STRUCTURE = "BiQL has incorrect structure";

  private BiQLMapper() {

  }

  /**
   * Converts a BiQL query string to an SQL query string, based on the provided entity class. This method interprets the BiQL syntax,
   * translates entity class references to table names, and converts entity field names to their corresponding column names in the SQL
   * query.
   *
   * @param <T>         The type of the entity class associated with the BiQL query.
   * @param bgl         The BiQL query string to be converted.
   * @param entityClass The class of the entity which the BiQL query is targeting.
   * @return A string representing the SQL query corresponding to the given BiQL query.
   * @throws BiQLException If the BiQL query is malformed or if the conversion process encounters an error.
   */
  public static <T> String bqlToSql(String bgl, Class<T> entityClass) {
    validateBiQL(bgl, entityClass);

    String entityClassName = entityClass.getSimpleName();

    List<String> bqlParts = Arrays.stream(bgl.replace(",", " ").toLowerCase().split("\\s+")).toList();
    if (bqlParts.get(0).equalsIgnoreCase(SqlKeyword.SELECT.name())) {
      int entityIndex = bqlParts.indexOf(entityClassName.toLowerCase());

      if (isSelectAll(bqlParts, entityIndex)) {
        String entityAlias = bqlParts.get(entityIndex + 1);
        bgl = bgl.replaceFirst(entityAlias, "*");
      }
    } else {
      bgl = "%s * %s".formatted(SqlKeyword.SELECT.name().toLowerCase(), bgl);
    }

    String entityTableName = EntityUtil.getEntityTableName(entityClass);
    String result = bgl.replace(entityClassName, entityTableName);
    List<Field> entityFields = getClassEntityFields(entityClass);
    String alias = getAlias(bqlParts, entityClassName);
    for (Field field : entityFields) {
      String fieldName = field.getName();
      if (bqlParts.contains(fieldName.toLowerCase()) || bqlParts.contains("%s.%s".formatted(alias, fieldName.toLowerCase()))) {
        String columnName = resolveColumnName(field);
        result = result.replace(fieldName, columnName);
      }
    }
    return result;
  }

  private static boolean isSelectAll(List<String> bqlParts, int entityIndex) {
    int whereIndex = bqlParts.indexOf(SqlKeyword.WHERE.name().toLowerCase());
    int fromIndex = bqlParts.indexOf(SqlKeyword.FROM.name().toLowerCase());
    // there is "from" on 2nd position
    return fromIndex == 2 &&
        // there is no "where" and one element after entity in bgl
        (
            whereIndex == -1 &&
                bqlParts.size() == entityIndex + 2 ||
                // there is "where" in bgl and one element between "where" and entity
                // ex.: FROM Person WHERE age > 0
                whereIndex == entityIndex + 2
        );
  }

  /**
   * Validates the given BiQL query to ensure it meets the required syntax and contains the necessary references to the entity class. This
   * method checks for the presence of the entity class within the BiQL query and ensures the query is not null or empty.
   *
   * @param bql         The BiQL query string to validate.
   * @param entityClass The entity class expected to be referenced in the query.
   * @throws BiQLException If the entityClass is null, the BiQL query is invalid, null, or does not reference the expected entity class.
   */
  private static <T> void validateBiQL(String bql, Class<T> entityClass) {
    log.trace("Starting to validate bql:[{}] for entity:[{}]", bql, entityClass);

    if (Objects.isNull(entityClass)) {
      throw new BiQLException("EntityClass should not be null");
    }
    if (Objects.isNull(bql) || bql.isEmpty()) {
      throw new BiQLException("BiQL should not be null or empty");
    }

    if (bql.contains("*")) {
      log.error("bql contains not valid symbol [*]");
      throw new BiQLException(BIQL_HAS_INCORRECT_STRUCTURE);
    }

    String entityClassName = entityClass.getSimpleName();
    if (!bql.contains(entityClassName)) {
      throw new BiQLException(
          "BiQL does not contain entity with type %s".formatted(entityClassName));
    }

    List<String> bqlParts = List.of(bql.toLowerCase().split("\\s+"));
    int selectIndex = bqlParts.indexOf(SqlKeyword.SELECT.name().toLowerCase());
    int fromIndex = bqlParts.indexOf(SqlKeyword.FROM.name().toLowerCase());

    if (fromIndex == -1) {
      log.error("Bql not contains from keyword");
      throw new BiQLException(BIQL_HAS_INCORRECT_STRUCTURE);
    }

    String allowedQueryStartWord = bqlParts.get(0);
    if (!(allowedQueryStartWord.equalsIgnoreCase(SqlKeyword.SELECT.name()) ||
        allowedQueryStartWord.equalsIgnoreCase(SqlKeyword.FROM.name()))) {
      throw new BiQLException(BIQL_HAS_INCORRECT_STRUCTURE);
    }

    String alias = getAlias(bqlParts, entityClassName);
    if (Objects.isNull(alias) && selectIndex == 0) {
      validateParamWithoutAlias(bqlParts, fromIndex);
    }

    if (Objects.nonNull(alias) && selectIndex == 0 && fromIndex > selectIndex + 1) {
      validateParam(bqlParts, fromIndex, alias);
    }

    log.trace("Bql [{}] for entity [{}] successfully validated", bql, entityClass);
  }

  private static void validateParamWithoutAlias(List<String> bqlParts, int fromIndex) {
    boolean isValidParam = bqlParts.subList(1, fromIndex)
        .stream()
        .noneMatch(el -> el.contains("."));

    if (!isValidParam) {
      log.error("Bql contains not valid symbol '.' in select params [{}]", bqlParts);
      throw new BiQLException(BIQL_HAS_INCORRECT_STRUCTURE);
    }
  }

  private static void validateParam(List<String> bqlParts, int fromIndex, String alias) {
    var subList = bqlParts.subList(1, fromIndex);

    if (subList.size() == 1 && !subList.get(0).equals(alias)) {
      log.error("Bql does not contain select param");
      throw new BiQLException(BIQL_HAS_INCORRECT_STRUCTURE);
    } else if (subList.size() == 1) {
      return;
    }

    boolean isValidParam = subList.stream()
        .allMatch(el -> el.startsWith(alias.concat(".")));

    if (!isValidParam) {
      log.error("Bql contains not valid select param [{}]", bqlParts);
      throw new BiQLException(BIQL_HAS_INCORRECT_STRUCTURE);
    }
  }

  private static String getAlias(List<String> bqlParts, String entityClassName) {
    int whereIndex = bqlParts.indexOf(SqlKeyword.WHERE.name().toLowerCase());
    int entityIndex = bqlParts.indexOf(entityClassName.toLowerCase());
    // there is no "where" and one element after entity in bgl
    if (whereIndex == -1 &&
        bqlParts.size() == entityIndex + 2 ||
        // there is "where" in bgl and one element between "where" and entity
        // ex.: FROM Person WHERE age > 0
        whereIndex == entityIndex + 2
    ) {
      return bqlParts.get(entityIndex + 1);
    }
    return null;
  }
}
