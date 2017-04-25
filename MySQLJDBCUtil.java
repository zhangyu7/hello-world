package org.mysqltutorial;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQLJDBCUtil {
	
	/*
	 * get database connection
	 * return a connection object
	 * throws SQLException
	 */
	public static Connection getconnection() throws SQLException{
		Connection conn = null;
		
/*		try {
		    //db parameters
		    String url       = "jdbc:mysql://localhost:3306/mysqljdbc";
		    String user      = "root";
		    String password  = "secret";
		 
		    //create a connection to the database
		    conn = DriverManager.getConnection(url, user, password);
		    //more processing here
		} catch(SQLException e) {
		   System.out.println(e.getMessage());
		} finally {
		 try{
		         if(conn != null)
		             conn.close();
		 }catch(SQLException ex){
		           System.out.println(ex.getMessage());
		 }
		
		}*/
		
		try(FileInputStream f = new FileInputStream("E:/Èí¼þ¹¤³Ì/Java/DBJ/mysqljdbc/mysqljdbc/db.properties.txt")){
			//load the properties file
			Properties pros = new Properties();
			pros.load(f);
			
			//assign db parameters
			String url = pros.getProperty("url");
			String user = pros.getProperty("user");
			String password = pros.getProperty("password");
			//String url = pros.getProperty("jdbc:mysql://localhost:3306/mysqljdbc");
			//String user = pros.getProperty("root");
			//String password = pros.getProperty("789789");
			
			//create a connection to the database
			conn = DriverManager.getConnection(url, user, password);
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		return conn;
	}
	
	

}
