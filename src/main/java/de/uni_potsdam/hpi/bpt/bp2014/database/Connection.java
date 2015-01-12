package de.uni_potsdam.hpi.bpt.bp2014.database;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.*;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connection {
    private static Connection instance = null;
    private static File file;
    private static String username;
    private static String password;
    private static String url;
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    @Resource(name="jdbc/jengine")
    private DataSource ds;

    private Connection(String path){

        if(ds == null) {
            file = new File(path);
            file = file.getAbsoluteFile();
            username = this.getUsername();
            password = this.getPassword();
            url = this.getUrl();
        }
    }

    public static Connection getInstance(String path) {
        if (instance == null) {
            instance = new Connection(path);
        }
        return instance;
    }

    public static Connection getInstance() {
        if (instance == null) {
            //instance = new Connection("C:/xampp/tomcat/webapps/JEngine/WEB-INF/classes/database_connection");
            instance = new Connection("tomcat/webapps/JEngine/WEB-INF/classes/database_connection");
        }
        return instance;
    }
    private static String getUsername(){
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String username = "";
        try {
            username = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return username;
    }
    private static String getPassword(){
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String password = "";
        try {
            br.readLine();
            password = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return password;
    }
    private static String getUrl(){
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //System.err.println(file.getAbsoluteFile());
        BufferedReader br = new BufferedReader(fr);
        String url = "";
        try {
            br.readLine();
            br.readLine();
            url = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }

    public java.sql.Connection connect() {
        java.sql.Connection conn = null;
        try {
            if(ds != null) {
                return ds.getConnection();
            }//Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            //Open a connection
            conn = DriverManager.getConnection(url, password, username);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return conn;
    }
}
