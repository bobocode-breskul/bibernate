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
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.metadata.dto.DataType;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class PGprovider {

  Logger logger = LoggerFactory.getLogger(PGprovider.class);
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


  static final Map<String, ColumnDefinitionHandler> columnHandlers;

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


  public static DataType getSQLType(Field field) {
    String fieldHandlerName = field.getType().getName();
    Column columnAnnotation = field.getAnnotation(Column.class);
    if (columnAnnotation != null) {
      fieldHandlerName = columnAnnotation.fieldHandler().isBlank()
          ? fieldHandlerName
          : columnAnnotation.fieldHandler();
    }
    ColumnDefinitionHandler handler = columnHandlers.get(fieldHandlerName);
    if (handler == null) {
      throw new IllegalArgumentException("Not existing field handler: [%s] for type: [%s]"
          .formatted(fieldHandlerName, field.getType()));
    }
    return handler.getDataType(field);
  }



  static class LongColumnHandler extends ColumnDefinitionHandler {
    @Override
    DataType resolveDataType(Field field) {
      return new DataType(BIGINT.getName());
    }

  }

  static class StringConverter extends ColumnDefinitionHandler {

    @Override
    DataType resolveDataType(Field field) {
      var fieldAnnotation = field.getAnnotation(Column.class);
      int length = DEFAULT_VARCHAR_LENGTH;
      if (fieldAnnotation != null) {
        length = fieldAnnotation.length();
      }

      return new DataType("%s(%d)".formatted(VARCHAR.getName(), length));
    }
  }

  static class IntConverter extends ColumnDefinitionHandler {

    @Override
    DataType resolveDataType(Field field) {
      return new DataType(INTEGER.getName());
    }
  }

  static class CharacterConverter extends ColumnDefinitionHandler {

    @Override
    DataType resolveDataType(Field field) {
      return new DataType("%s(%d)".formatted(CHAR.getName(), 1));
    }
  }

  static class FloatDefinitionHandler extends ColumnDefinitionHandler {

    @Override
    DataType resolveDataType(Field field) {
      var fieldAnnotation = field.getAnnotation(Column.class);
      if (fieldAnnotation != null && fieldAnnotation.precision() != 0) {
        int precision = fieldAnnotation.precision();

        if (fieldAnnotation.scale() != 0) {
          throw new IllegalArgumentException("scale has no meaning for floating point numbers");
        };
        // convert from base 10 (as specified in @Column) to base 2 (as specified by SQL)
        // using the magic of high school math: log_2(10^n) = n*log_2(10) = n*ln(10)/ln(2)
        precision = (int) ceil( precision * LOG_BASE2OF10 );
        if (precision <= FLOAT_PRECISION_THRESHOLD) {
          return new DataType(REAL.getName());
        }
        return new DataType("%s(%d)".formatted(FLOAT.toString(), precision));
      }
      return new DataType("%s(%d)".formatted(FLOAT.toString(), DOUBLE_PRECISION_THRESHOLD));
    }
  }

  static class BooleanConverter extends ColumnDefinitionHandler {
    @Override
    DataType resolveDataType(Field field) {
      return new DataType(JDBCType.BOOLEAN.getName());
    }
  }

  static class ByteArrayConverter extends ColumnDefinitionHandler {
    @Override
    DataType resolveDataType(Field field) {
      return new DataType("BYTEA");
    }
  }

  static class NumericHandler extends ColumnDefinitionHandler {

    @Override
    DataType resolveDataType(Field field) {
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
