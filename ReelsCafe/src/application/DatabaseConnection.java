package application;

import java.sql.*;
import java.util.*;

public class DatabaseConnection {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "reels_cafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "TO BE FILLED";

    private static final String URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    //connection

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    //select query

    public List<Map<String, Object>> executeQuery(String sql, Object... params)
            throws SQLException {

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        }
        return results;
    }

    //INSERT / UPDATE / DELETE queries

    public int executeUpdate(String sql, Object... params)
            throws SQLException {

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bindParams(ps, params);
            return ps.executeUpdate();
        }
    }

    //PARAM BINDER (helper)

    private void bindParams(PreparedStatement ps, Object... params)
            throws SQLException {

        if (params == null) return;

        for (int i = 0; i < params.length; i++) {
            if (params[i] == null)
                ps.setNull(i + 1, Types.NULL);
            else
                ps.setObject(i + 1, params[i]);
        }
    }

    //CONNECTION TEST

    public boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
    public int executeInsertAndGetId(String sql, Object... params)
            throws SQLException {

        try (Connection con = getConnection();PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("No generated key returned");
    }

}
