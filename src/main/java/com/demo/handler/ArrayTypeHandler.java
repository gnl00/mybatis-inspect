package com.demo.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes({JdbcType.INTEGER, JdbcType.FLOAT, JdbcType.DOUBLE, JdbcType.CHAR, JdbcType.VARCHAR}) // 处理这些类型的数组
public class ArrayTypeHandler extends BaseTypeHandler<Object> {

    // 插入数据时会调用 setNonNullParameter
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object objects, JdbcType jdbcType) throws SQLException {
        Connection connection = ps.getConnection();
        /*
         以下面的 mapper 语句为例子：
         <insert id="insertWithArray">
            insert into tb_ts(time, id, arr, arr_fl)
            values(
                #{timestamp},
                #{id},
                #{arr, jdbcType=INTEGER, typeHandler=com.demo.handler.ArrayTypeHandler},
                #{arrFl, jdbcType=FLOAT, typeHandler=com.demo.handler.ArrayTypeHandler}
            );
         </insert>
         当 MyBatis 解析到 #{arr, jdbcType=INTEGER, typeHandler=com.demo.handler.ArrayTypeHandler} 这句代码，
         就会调用 ArrayTypeHandler 来处理 arr 这个列，并传入 jdbcType=INTEGER 等相关数据。

         在这里使用 jdbcType.name() 我们会获取到 @MappedJdbcTypes 中预定义的 JDBC 类型名称，
         java.sql.Connection.createArrayOf 会根据传入的 typeName 参数创建不同类型的 PGArray 对象。
         PGArray 对象是 java.sql.Array 的子类。
         */
        if (objects instanceof Array) {
            ps.setArray(i, (Array) objects);
        }
        Array array = connection.createArrayOf(jdbcType.name(), (Object[]) objects);
        ps.setArray(i, array);
        array.free();
    }

    /* 获取数据时会调用下面的方法 */

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getArray(rs.getArray(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getArray(rs.getArray(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getArray(cs.getArray(columnIndex));
    }

    private Object getArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        Object result = array.getArray();
        array.free();
        return result;
    }
}
