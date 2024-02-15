package com.breskul.bibernate.metadata;

import static com.breskul.bibernate.metadata.CommonSQLTypesConverter.SCALE_IN_FLOAT_ERR_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.breskul.bibernate.metadata.dto.DataType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;


class CommonSQLTypesConverterTest {

    @Test
    void given_Long_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varLong");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("BIGINT");
    }
    @Test
    void given_LongPrimitive_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varPrimitiveLong");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("BIGINT");
    }

    @Test
    void given_Integer_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varInt");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("INTEGER");
    }

    @Test
    void given_primitiveInt_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varPrimitiveInt");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("INTEGER");
    }

    @Test
    void given_String_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varString");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("VARCHAR(255)");
    }

    @Test
    void given_Float_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varFloat");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("REAL");
    }

    @Test
    void given_primitiveFloat_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varPrimitiveFloat");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("REAL");
    }

    @Test
    void given_Double_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varDouble");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("FLOAT(53)");
    }

    @Test
    void given_primitiveDouble_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varPrimitiveDouble");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("FLOAT(53)");
    }

    @Test
    void given_Char_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varChar");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("CHAR(1)");
    }

    @Test
    void given_primitiveChar_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varPrimitiveChar");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("CHAR(1)");
    }

    @Test
    void given_BigDecimal_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varBigDecimal");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("NUMERIC(38,2)");
    }

    @Test
    void given_BigInteger_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varBigInteger");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("NUMERIC(38,0)");
    }

    @Test
    void given_byteArray_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varByteArray");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("BYTEA");
    }

    @Test
    void given_varBoolean_when_getSQLType_than_correctSQLType() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("varBoolean");
        DataType dataType = CommonSQLTypesConverter.getSQLType(field);
        assertThat(dataType.getName()).isEqualTo("BOOLEAN");
    }

    @Test
    void given_doubleWithLowPrecision_when_getSQLType_than_returnRealType() throws NoSuchFieldException {
        Field field = AnnotatedColumnsClass.class.getDeclaredField("varDouble");

        DataType dataType = CommonSQLTypesConverter.getSQLType(field);

        assertThat(dataType).isNotNull();
        assertThat(dataType.getName()).isEqualTo("REAL");
    }

    @Test
    void given_floatWithPrecision8_when_getSQLType_than_returnFloat27Type() throws NoSuchFieldException {
        Field field = AnnotatedColumnsClass.class.getDeclaredField("varFloat");

        DataType dataType = CommonSQLTypesConverter.getSQLType(field);

        assertThat(dataType).isNotNull();
        assertThat(dataType.getName()).isEqualTo("FLOAT(27)");
    }

    @Test
    void givenAnnotatedScaleInFloatType_when_getSQLType_than_throwsIllegalArgumentException() throws NoSuchFieldException {
        Field field = AnnotatedColumnsClass.class.getDeclaredField("field3");

        assertThatThrownBy(() -> CommonSQLTypesConverter.getSQLType(field))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(SCALE_IN_FLOAT_ERR_MSG);
    }

    @Test
    void given_definedType_when_getSQLType_than_typeFromAnnotation() throws NoSuchFieldException {
        Field field = AnnotatedColumnsClass.class.getDeclaredField("definedType");

        DataType dataType = CommonSQLTypesConverter.getSQLType(field);

        assertThat(dataType).isNotNull();
        assertThat(dataType.getName()).isEqualTo("definedType");
    }


    private static class AnnotatedColumnsClass {

        @com.breskul.bibernate.annotation.Column(precision = 1)
        public double varDouble;

        @com.breskul.bibernate.annotation.Column(precision = 8)
        public float varFloat;

        @com.breskul.bibernate.annotation.Column(scale = 1)
        public float field3;

        @com.breskul.bibernate.annotation.Column(columnDefinition = "definedType")
        public int definedType;
    }

    static class TestClass {
        public Long varLong;
        public long varPrimitiveLong;
        public Integer varInt;
        public int varPrimitiveInt;
        public String varString;
        public Float varFloat;
        public float varPrimitiveFloat;
        public Double varDouble;
        public double varPrimitiveDouble;
        public Character varChar;
        public char varPrimitiveChar;
        public BigDecimal varBigDecimal;
        public BigInteger varBigInteger;
        public byte[] varByteArray;
        public Boolean varBoolean;
    }
}