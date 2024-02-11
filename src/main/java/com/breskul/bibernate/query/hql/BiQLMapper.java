package com.breskul.bibernate.query.hql;

import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;

import com.breskul.bibernate.exception.BiQLException;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * A utility class for converting BiQL (a hypothetical query language) to standard SQL queries.
 * This class provides static methods to parse BiQL strings and transform them into SQL syntax
 * based on entity class mappings and field annotations. The conversion process takes into account
 * the structure and semantics of BiQL to ensure the generated SQL queries accurately represent
 * the original intent.
 */
public class BiQLMapper {

  private BiQLMapper() {

  }

  /**
   * Converts a BiQL query string to an SQL query string, based on the provided entity class.
   * This method interprets the BiQL syntax, translates entity class references to table names,
   * and converts entity field names to their corresponding column names in the SQL query.
   *
   * @param <T>         The type of the entity class associated with the BiQL query.
   * @param bgl         The BiQL query string to be converted.
   * @param entityClass The class of the entity which the BiQL query is targeting.
   * @return A string representing the SQL query corresponding to the given BiQL query.
   * @throws BiQLException If the BiQL query is malformed or if the conversion process encounters an error.
   */
  //TODO: given Null bql then should throw BiQLException
  //TODO: given empty string then should throw BiQLException
  //TODO: given valid bql but contains different entity class
  //TODO: given valid bql but entity class is null
  //TODO: given bql without sql and valid entity class then should generate sql with asterisk select
  //TODO: given bql with table alias then should generate correct sql
  //TODO: given bql when select specific columns then should return correct sql
  public static <T> String bqlToSql(String bgl, Class<T> entityClass) {
    String entityClassName = entityClass.getSimpleName();

    validateBiQL(bgl, entityClassName);

    List<String> bqlParts = Arrays.stream(bgl.toLowerCase().split("\\s+")).toList();
    if (bqlParts.get(0).equalsIgnoreCase(SqlKeyword.SELECT.name())) {
      int entityIndex = bqlParts.indexOf(entityClassName.toLowerCase());
      int whereIndex = bqlParts.indexOf(SqlKeyword.WHERE.name().toLowerCase());
      // there is no "where" and one element after entity in bgl
      if (whereIndex == -1 && bqlParts.size() == entityIndex + 2
          // there is "where" in bgl and one element between "where" and entity
          // ex.: FROM Person WHERE age > 0
          || whereIndex == entityIndex + 2) {
        String entityAlias = bqlParts.get(entityIndex + 1);
        bgl = bgl.replaceFirst(entityAlias, "*");
      } else {
        throw new BiQLException("BiQL has incorrect structure");
      }
    }
    if (bqlParts.get(0).equalsIgnoreCase(SqlKeyword.FROM.name())) {
      bgl = "%s * %s".formatted(SqlKeyword.SELECT.name(), bgl);
    }
    String entityTableName = EntityUtil.getEntityTableName(entityClass);
    String result = bgl.replace(entityClassName, entityTableName);
    List<Field> entityFields = getClassEntityFields(entityClass);
    for (Field field : entityFields) {
      String fieldName = field.getName();
      String columnName = resolveColumnName(field);
      result = result.replace(fieldName, columnName);
    }
    return result;
  }

  /**
   * Validates the given BiQL query to ensure it meets the required syntax and contains the necessary
   * references to the entity class. This method checks for the presence of the entity class within the
   * BiQL query and ensures the query is not null or empty.
   *
   * @param bql             The BiQL query string to validate.
   * @param entityClassName The name of the entity class expected to be referenced in the query.
   * @throws BiQLException  If the entityClassName is null, the BiQL query is invalid, null, or does not reference the expected entity class.
   */
  private static void validateBiQL(String bql, String entityClassName) {
    if (Objects.isNull(entityClassName)) {
      throw new BiQLException("entityClassName should not be null");
    }
    if (Objects.isNull(bql) || bql.isEmpty()) {
      throw new BiQLException("BiQL should not be null or empty");
    }
    if (!bql.contains(entityClassName)) {
      throw new BiQLException(
          "BiQL does not contain entity with type %s".formatted(entityClassName));
    }
  }

}
