package de.uni_potsdam.hpi.bpt.bp2014.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Helper {

    /**
     * @param sql
     * @return
     */
    public static List<Map<String, Object>> executeStatementReturnsMap(String sql) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        try {
            //Execute a query
            stmt = conn.createStatement();

            //query
            ResultSet result;
            boolean returningRows = stmt.execute(sql);
            if (returningRows)
                result = stmt.getResultSet();
            else
                return new ArrayList<>();

            //get metadata
            ResultSetMetaData meta = result.getMetaData();

            //get column names
            int columnCount = meta.getColumnCount();
            ArrayList<String> columns = new ArrayList<String>();
            for (int i = 1; i <= columnCount; i++)
                columns.add(meta.getColumnName(i));

            //fetch out rows
            List<Map<String, Object>> rows =
                    new ArrayList<>();

            while (result.next()) {
                Map<String, Object> row = new HashMap<String, Object>();
                for (String columnName : columns) {
                    Object value = result.getObject(columnName);
                    row.put(columnName, value);
                }
                rows.add(row);
            }

            //close statement
            stmt.close();

            //pass back rows
            return rows;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return new ArrayList<>();
    }


    /**
     * Executes the given select SQL statement and returns the result in List with Integer.
     *
     * @param sql         This is a given select SQL Statement.
     * @param columnLabel This is the label of the column which is used as the result.
     * @return List with Integer.
     */
    public static List<Integer> executeStatementReturnsListInt(String sql, String columnLabel) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        ResultSet rs;
        List<Integer> results = new ArrayList<>();
        if (conn == null) {
            return results;
        }

        try {
            //Execute a query
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                results.add(rs.getInt(columnLabel));
            }
            //Clean-up environment
            rs.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return results;
    }

    /**
     * Executes the given select SQL statement and returns the result as String.
     *
     * @param sql         This is a given select SQL Statement.
     * @param columnLabel This is the label of the column which is used as the result.
     * @return String.
     */
    public static String executeStatementReturnsString(String sql, String columnLabel) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        ResultSet rs;
        String results = "";
        if (conn == null) {
            return results;
        }
        try {
            //Execute a query
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                results = rs.getString(columnLabel);
            }
            //Clean-up environment
            rs.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return results;
    }

    /**
     * Executes the given select SQL statement and returns the result as int.
     *
     * @param sql         This is a given select SQL Statement.
     * @param columnLabel This is the label of the column which is used as the result.
     * @return int.
     */
    public int executeStatementReturnsInt(String sql, String columnLabel) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        ResultSet rs = null;
        int results = -1;
        if (conn == null) {
            return results;
        }

        try {
            //Execute a query
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                results = rs.getInt(columnLabel);
            }
            //Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return results;
    }

    /**
     * Executes the given select SQL statement and returns the result as boolean.
     *
     * @param sql This is a given select SQL Statement.
     * @return boolean.
     */

    public static boolean executeStatementReturnsBoolean(String sql) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        ResultSet rs = null;
        Boolean results = false;
        if (conn == null) {
            return results;
        }
        try {
            //Execute a query
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            //Clean-up environment
            rs.close();

            results = true;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return results;
    }

    /**
     * Executes the given select SQL statement.
     *
     * @param sql This is a given SQL Statement.
     * @return true if there is a result for the statement. false if not.
     */
    public boolean executeExistStatement(String sql) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        ResultSet rs = null;
        if (conn == null) {
            return false;
        }

        try {
            //Execute a query
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
            //Clean-up environment
            rs.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Executes the given insert SQL statement.
     *
     * @param sql This is a given SQL Statement.
     * @return the generated key for the insert statement.
     */
    public static int executeInsertStatement(String sql) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        ResultSet rs = null;
        if (conn == null) {
            return -1;
        }
        int result = -1;
        try {
            //Execute a query
            stmt = conn.createStatement();
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Executes the given SQL Statement. Which should be a update or insert statement.
     *
     * @param sql This is a given SQL Statement.
     */
    public static boolean executeUpdateStatement(String sql) {
        java.sql.Connection conn = Connection.getInstance().connect();
        Statement stmt = null;
        ResultSet rs = null;
        Boolean results = false;
        if (conn == null) {
            return results;
        }
        try {
            //Execute a querystmt = conn.createStatement();
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                    results = true;
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
                results = false;
            }
            try {
                if (conn != null) {
                    conn.close();
                    results = true;
                }
            } catch (SQLException se) {
                se.printStackTrace();
                results = false;
            }
        }
        return results;
    }
}
