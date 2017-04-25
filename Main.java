package org.mysqltutorial;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {

	public void update() {
		//--Updating Data in MySQL Using JDBC PreparedStatement
        //update candidate demo
		
        String sqlUpdate = "UPDATE candidates " + "SET last_name = ? " + "WHERE id = ?";
 
        try (Connection conn = MySQLJDBCUtil.getconnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
 
            //prepare data for update
            String lastName = "William";
            int id = 100;
            pstmt.setString(1, lastName);
            pstmt.setInt(2, id);
 
            int rowAffected = pstmt.executeUpdate();
            System.out.println(String.format("Row affected %d", rowAffected));
 
            //reuse the prepared statement
            lastName = "Grohe";
            id = 101;
            pstmt.setString(1, lastName);
            pstmt.setInt(2, id);
 
            rowAffected = pstmt.executeUpdate();
            System.out.println(String.format("Row affected %d", rowAffected));
 
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
	
	
    public static int insertCandidate(String firstName,String lastName,Date dob,String email, String phone) {
        //for insert a new candidate
    	//firstName,lastName,dob,email,phone
    	
        ResultSet rs = null;
        int candidateId = 0;
        
        String sql = "INSERT INTO candidates(first_name,last_name,dob,phone,email) "
                   + "VALUES(?,?,?,?,?)";
        
        try (Connection conn = MySQLJDBCUtil.getconnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);) {
            
            //set parameters for statement
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setDate(3, dob);
            pstmt.setString(4, phone);
            pstmt.setString(5, email);
 
            int rowAffected = pstmt.executeUpdate();
            if(rowAffected == 1)
            {
                //get candidate id
                rs = pstmt.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
 
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if(rs != null)  rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        
        return candidateId;
    }
	
    
    public static void addCandidate(String firstName,String lastName,Date dob,String email, String phone, int[] skills) {
    	//--MySQL JDBC Transaction
    	//insert and assign to a specific candidates
    	//firstName,lastName,dob,email,phone,skills
    	
    	Connection conn = null;
    	
        //for insert a new candidate
        PreparedStatement pstmt = null;  
        
        //for assign skills to candidate
        PreparedStatement pstmtAssignment = null;
        
        //for getting candidate id
        ResultSet rs = null;
        
        try {
            conn = MySQLJDBCUtil.getconnection();
            //set auto commit to false
            conn.setAutoCommit(false);
            
            //Insert candidate
            String sqlInsert = "INSERT INTO candidates(first_name,last_name,dob,phone,email) "
                              + "VALUES(?,?,?,?,?)";
            
            pstmt = conn.prepareStatement(sqlInsert,Statement.RETURN_GENERATED_KEYS);
 
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setDate(3, dob);
            pstmt.setString(4, phone);
            pstmt.setString(5, email);
 
            int rowAffected = pstmt.executeUpdate();
            
            //get candidate id
            rs = pstmt.getGeneratedKeys();
            int candidateId = 0;
            if(rs.next())
                candidateId = rs.getInt(1);
    
            //in case the insert operation successes, assign skills to candidate
            if(rowAffected == 1)
            {
                //assign skills to candidates
                String sqlPivot = "INSERT INTO candidate_skills(candidate_id,skill_id) "
                                 + "VALUES(?,?)";
                
                pstmtAssignment = conn.prepareStatement(sqlPivot);
                for(int skillId : skills) {
                    
                    pstmtAssignment.setInt(1, candidateId);
                    pstmtAssignment.setInt(2, skillId);
                    
                    pstmtAssignment.executeUpdate();
                }
                conn.commit();
            } else {
                conn.rollback();
            }
        } catch (SQLException ex) {//roll back the transaction
            try{
                if(conn != null)
                    conn.rollback();
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }           
            
            System.out.println(ex.getMessage());
        } finally {
            try {
                if(rs != null)  rs.close();
                if(pstmt != null) pstmt.close();
                if(pstmtAssignment != null) pstmtAssignment.close();
                if(conn != null) conn.close();                
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        
    }
    
    
    public static void getSkills(int candidateId) {
        //--Calling MySQL Stored Procedures from JDBC
    	//get skills by candidate id
    	
        String query = "{ call get_candidate_skill(?) }";
        ResultSet rs;
 
        try (Connection conn = MySQLJDBCUtil.getconnection();
                CallableStatement stmt = conn.prepareCall(query)) {
 
            stmt.setInt(1, candidateId);
 
            rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println(String.format("%s - %s",
                        rs.getString("first_name") + " "
                        + rs.getString("last_name"),
                        rs.getString("skill")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    
    public static void writeBlob(int candidateId, String filename) {
    	//--Writing and Reading MySQL BLOB Using JDBC
        //update sql
    	
        String updateSQL = "UPDATE candidates " + "SET resume = ? " + "WHERE id=?";
 
        try (Connection conn = MySQLJDBCUtil.getconnection();
                PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
 
            //read the file
            File file = new File(filename);
            FileInputStream input = new FileInputStream(file);
 
            //set parameters
            pstmt.setBinaryStream(1, input);
            pstmt.setInt(2, candidateId);
 
            //store the resume file in database
            System.out.println("Reading file " + file.getAbsolutePath());
            System.out.println("Store file in the database.");
            pstmt.executeUpdate();
 
        } catch (SQLException | FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    public static void readBlob(int candidateId, String filename) {
    	//--Writing and Reading MySQL BLOB Using JDBC
        //update sql
    	
        String selectSQL = "SELECT resume FROM candidates WHERE id=?";
        ResultSet rs = null;
 
        try (Connection conn = MySQLJDBCUtil.getconnection();
                PreparedStatement pstmt = conn.prepareStatement(selectSQL);) {
            //set parameter;
            pstmt.setInt(1, candidateId);
            rs = pstmt.executeQuery();
 
            //write binary stream into file
            File file = new File(filename);
            FileOutputStream output = new FileOutputStream(file);
 
            System.out.println("Writing to file " + file.getAbsolutePath());
            while (rs.next()) {
                InputStream input = rs.getBinaryStream("resume");
                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    output.write(buffer);
                }
            }
        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
 
    }
     
    
    
	public static void main(String[] args) {
	
		//--Connecting to MySQL Using JDBC Driver
		// create a new connection from MySQLJDBCUtil
/*		try(Connection conn = MySQLJDBCUtil.getconnection()){
			//print out a message
			System.out.println(String.format("Connected to database %s "
					+ "successfully.", conn.getCatalog()));
		} catch (SQLException ex){
			System.out.println(ex.getMessage());
		}*/
		
		
		//--Querying Data From MySQL Using JDBC
/*        String sql = "SELECT id, first_name, last_name, email " +
                "FROM candidates";
   
        try (Connection conn = MySQLJDBCUtil.getconnection();
        		Statement stmt  = conn.createStatement();
        		ResultSet rs    = stmt.executeQuery(sql)) {
      
        	// loop through the result set
        	while (rs.next()) {
        		System.out.println(rs.getString("id") + "\t" + 
        						   rs.getString("first_name") + "\t" + 
        						   rs.getString("last_name")  + "\t" +
        						   rs.getString("email"));
               
        	}
        } catch (SQLException ex) {
        	System.out.println(ex.getMessage());
        }*/
		
		
		//--Updating Data in MySQL Using JDBC PreparedStatement
        //update candidate demo
/*        Main a = new Main();
        a.update();*/
		
		//--Inserting Data Into Table Using JDBC PreparedStatement
        // insert a new candidate
        
/*        int id = insertCandidate("Bush", "Lily", Date.valueOf("1980-01-04"),"bush.l@yahoo.com","(408) 898-6666");       
        System.out.println(String.format("A new candidate with id %d has been inserted.",id));
           */
		
        //--MySQL JDBC Transaction
    	//insert and assign to a specific candidates
    	//firstName,lastName,dob,email,phone,skills
/*        int[] skills = {1,2,3};
        addCandidate("John", "Doe", Date.valueOf("1990-01-04"),"john.d@yahoo.com", "(408) 898-5641", skills);
		
        //--Calling MySQL Stored Procedures from JDBC
    	//get skills by candidate id
/*        getSkills(122);*/
        
    	//--Writing and Reading MySQL BLOB Using JDBC
        //update sql
        writeBlob(122, "johndoe_resume.pdf");
        readBlob(122, "johndoe_resume_from_db.pdf");

	}
	
	

}
