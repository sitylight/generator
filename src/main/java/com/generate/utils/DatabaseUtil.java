package com.generate.utils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author derrick
 */
public final class DatabaseUtil {
    private static final DataSource DATA_SOURCE = SpringBeanUtil.getBean(DataSource.class);

    public static LinkedHashMap<String, String> getColumnsInTable(String tableName) {
        LinkedHashMap<String, String> columnNames = new LinkedHashMap<>();
        //与数据库的连接;
        PreparedStatement pStemt = null;
        String prefixSql = "SELECT * FROM ";
        String tableSql = prefixSql + tableName;
        try (Connection conn = DATA_SOURCE.getConnection()) {
            pStemt = conn.prepareStatement(tableSql);
            //结果集元数据
            ResultSetMetaData rsmd = pStemt.getMetaData();
            //表列数
            int size = rsmd.getColumnCount();
            for (int i = 0; i < size; i++) {
                columnNames.put(rsmd.getColumnName(i + 1), rsmd.getColumnTypeName(i + 1));
            }
        } catch (SQLException e) {
            System.out.println("getColumnNames failure");
        } finally {
            if (pStemt != null) {
                try {
                    pStemt.close();
                } catch (SQLException e) {
                    System.out.println("getColumnNames close pstem and connection failure");
                }
            }
        }
        return columnNames;
    }
    public static List<String> getAllTableNames() {
        List<String> tableNames = new ArrayList<>();
        ResultSet rs = null;
        try (Connection conn = DATA_SOURCE.getConnection()) {
            //获取数据库的元数据
            DatabaseMetaData db = conn.getMetaData();
            //从元数据中获取到所有的表名
            rs = db.getTables(null, null, null, new String[] {"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString(3);
                if (tableName.startsWith("tbl")) {
                    tableNames.add(tableName);
                }
            }
        } catch (SQLException e) {
            System.out.println("getTableNames failure");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                System.out.println("close ResultSet failure");
            }
        }
        return tableNames;
    }
}
