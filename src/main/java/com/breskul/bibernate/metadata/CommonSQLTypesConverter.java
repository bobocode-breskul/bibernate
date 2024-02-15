package com.breskul.bibernate.metadata;

import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.sql.JDBCType.BIGINT;
import static java.sql.JDBCType.CHAR;
import static java.sql.JDBCType.FLOAT;
import static java.sql.JDBCType.INTEGER;
import static java.sql.JDBCType.REAL;
import static java.sql.JDBCType.VARCHAR;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.metadata.dto.DataType;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommonSQLTypesConverter {
  static final int DEFAULT_VARCHAR_LENGTH = 255;

  /**
   * The assumption that real and double precision have exactly 24 and 53 bits in the mantissa
   * respectively is correct for IEEE-standard floating point implementations. On non-IEEE platforms
   * it may be off a little, but for simplicity the same ranges of p are used on all platforms.
   */
  private static final int FLOAT_PRECISION_THRESHOLD = 24;
  private static final int DOUBLE_PRECISION_THRESHOLD = 53;
  private static final int DEFAULT_NUMERIC_PRECISION = 38;
  private static final int DEFAULT_BIG_INTEGER_SCALE = 0;
  private static final int DEFAULT_BIG_DECIMAL_SCALE = 2;
  private static final double LOG_BASE2OF10 = log(10)/log(2);

  static final String SCALE_IN_FLOAT_ERR_MSG = "Scale has no meaning for floating point numbers";


  protected static final Map<String, ColumnDefinitionHandler> columnHandlers;

  static {
    Map<String, ColumnDefinitionHandler> tempMap = new ConcurrentHashMap<>();
    tempMap.put(Long.class.getName(), new LongColumnHandler());
    tempMap.put(long.class.getName(), new LongColumnHandler());
    tempMap.put(Integer.class.getName(), new IntConverter());
    tempMap.put(int.class.getName(), new IntConverter());
    tempMap.put(boolean.class.getName(), new BooleanConverter());
    tempMap.put(Boolean.class.getName(), new BooleanConverter());
    tempMap.put(Float.class.getName(), new FloatDefinitionHandler());
    tempMap.put(float.class.getName(), new FloatDefinitionHandler());
    tempMap.put(Double.class.getName(), new FloatDefinitionHandler());
    tempMap.put(double.class.getName(), new FloatDefinitionHandler());
    tempMap.put(String.class.getName(), new StringConverter());
    tempMap.put(Character.class.getName(), new CharacterConverter());
    tempMap.put(char.class.getName(), new CharacterConverter());
    tempMap.put(byte[].class.getName(), new ByteArrayConverter());
    tempMap.put(BigDecimal.class.getName(), new NumericHandler());
    tempMap.put(BigInteger.class.getName(), new NumericHandler());

    columnHandlers = tempMap;
  }


  /**
   * Retrieves the SQL data type of given field.
   * If {@code @Column.columnDefinition() } is overridden than sql method will return data from it,
   * otherwise sql type calculates by {@code ColumnDefinitionHandler's} from the given field
   *
   * @param field the field whose SQL data type needs to be retrieved
   * @return the SQL data type of the field
   * @throws IllegalArgumentException if the SQL type for the given field cannot be found
   */
  public static DataType getSQLType(Field field) {
    String fieldHandlerName = field.getType().getName();
    var columnAnnotation = field.getAnnotation(Column.class);
    if (columnAnnotation != null && !columnAnnotation.columnDefinition().isBlank()) {
      var colDefinition = columnAnnotation.columnDefinition();
      return new DataType(colDefinition);
    }
    ColumnDefinitionHandler handler = columnHandlers.get(fieldHandlerName);
    if (handler == null) {
      throw new IllegalArgumentException(("Can't find sql type for class:[%s] field [%s]. Please set it "
          + "manually in @Column.columnDefinition property").formatted(field.getDeclaringClass(), field.getName()));
    }
    return handler.resolveDataType(field);
  }

  static class LongColumnHandler implements ColumnDefinitionHandler {
    @Override
    public DataType resolveDataType(Field field) {
      return new DataType(BIGINT.getName());
    }

  }

  static class StringConverter implements ColumnDefinitionHandler {

    @Override
    public DataType resolveDataType(Field field) {
      var fieldAnnotation = field.getAnnotation(Column.class);
      int length = DEFAULT_VARCHAR_LENGTH;
      if (fieldAnnotation != null) {
        length = fieldAnnotation.length();
      }

      return new DataType("%s(%d)".formatted(VARCHAR.getName(), length));
    }
  }

  static class IntConverter implements ColumnDefinitionHandler {

    @Override
    public DataType resolveDataType(Field field) {
      return new DataType(INTEGER.getName());
    }
  }

  static class CharacterConverter implements ColumnDefinitionHandler {

    @Override
    public DataType resolveDataType(Field field) {
      return new DataType("%s(%d)".formatted(CHAR.getName(), 1));
    }
  }

  static class FloatDefinitionHandler implements ColumnDefinitionHandler {

    @Override
    public DataType resolveDataType(Field field) {
      var fieldAnnotation = field.getAnnotation(Column.class);
      if (fieldAnnotation != null) {
        if (fieldAnnotation.scale() != 0) {
          throw new IllegalArgumentException(SCALE_IN_FLOAT_ERR_MSG);
        }
        int precision = fieldAnnotation.precision();
        if (fieldAnnotation.precision() != 0) {
          // convert from base 10 (as specified in @Column) to base 2 (as specified by SQL)
          // using the magic of high school math: log_2(10^n) = n*log_2(10) = n*ln(10)/ln(2)
          precision = (int) ceil( precision * LOG_BASE2OF10 );
          if (precision <= FLOAT_PRECISION_THRESHOLD) {
            return new DataType(REAL.getName());
          }
          return new DataType("%s(%d)".formatted(FLOAT.toString(), precision));
        }
      }
      if (field.getType() == float.class || field.getType() == Float.class) {
        return new DataType(REAL.getName());
      }
      return new DataType("%s(%d)".formatted(FLOAT.toString(), DOUBLE_PRECISION_THRESHOLD));
    }
  }

  static class BooleanConverter implements ColumnDefinitionHandler {
    @Override
    public DataType resolveDataType(Field field) {
      return new DataType(JDBCType.BOOLEAN.getName());
    }
  }

  static class ByteArrayConverter implements ColumnDefinitionHandler {
    @Override
    public DataType resolveDataType(Field field) {
      return new DataType("BYTEA");
    }
  }

  static class NumericHandler implements ColumnDefinitionHandler {

    @Override
    public DataType resolveDataType(Field field) {
      var fieldAnnotation = field.getAnnotation(Column.class);
      int precision = DEFAULT_NUMERIC_PRECISION;
      int scale = field.getType() == BigDecimal.class
          ? DEFAULT_BIG_DECIMAL_SCALE
          : DEFAULT_BIG_INTEGER_SCALE;
      if (fieldAnnotation != null) {
        precision = fieldAnnotation.precision() == 0 ? precision : fieldAnnotation.precision();
        scale = fieldAnnotation.scale() == 0 ? scale : fieldAnnotation.scale();
      }
      return new DataType("NUMERIC(%d,%d)".formatted(precision, scale));
    }
  }
}
