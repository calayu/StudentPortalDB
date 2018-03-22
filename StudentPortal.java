/* This is the driving engine of the program. It parses the command-line
 * arguments and calls the appropriate methods in the other classes.
 *
 * You should edit this file in two ways:
 * 1) Insert your database username and password in the proper places.
 * 2) Implement the three functions getInformation, registerStudent
 *    and unregisterStudent.
 */
import java.sql.*; // JDBC stuff.
import java.util.Properties;
import java.util.Scanner;
import java.io.*;  // Reading user input.

public class StudentPortal
{
    /* TODO Here you should put your database name, username and password */
    static final String DATABASE = "jdbc:postgresql://ate.ita.chalmers.se/";
    static final String USERNAME = "tda357_060";
    static final String PASSWORD = "pineapple";

    /* Print command usage.
     * /!\ you don't need to change this function! */
    public static void usage () {
        System.out.println("Usage:");
        System.out.println("    i[nformation]");
        System.out.println("    r[egister] <course>");
        System.out.println("    u[nregister] <course>");
        System.out.println("    q[uit]");
    }

    /* main: parses the input commands.
     * /!\ You don't need to change this function! */
    public static void main(String[] args) throws Exception
    {
        try {
            Class.forName("org.postgresql.Driver");
            String url = DATABASE;
            Properties props = new Properties();
            props.setProperty("user",USERNAME);
            props.setProperty("password",PASSWORD);
            Connection conn = DriverManager.getConnection(url, props);

            String student = args[0]; // This is the identifier for the student.

            //Console console = System.console();
	    // In Eclipse. System.console() returns null due to a bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429)
	    // In that case, use the following line instead:
	     BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            usage();
            System.out.println("Welcome!");
            while(true) {
	        System.out.print("? > ");
                String mode = console.readLine();
                String[] cmd = mode.split(" +");
                cmd[0] = cmd[0].toLowerCase();
                if ("information".startsWith(cmd[0]) && cmd.length == 1) {
                    /* Information mode */
                    getInformation(conn, student);
                } else if ("register".startsWith(cmd[0]) && cmd.length == 2) {
                    /* Register student mode */
                    registerStudent(conn, student, cmd[1]);
                } else if ("unregister".startsWith(cmd[0]) && cmd.length == 2) {
                    /* Unregister student mode */
                    unregisterStudent(conn, student, cmd[1]);
                } else if ("quit".startsWith(cmd[0])) {
                    break;
                } else usage();
            }
            System.out.println("Goodbye!");
            conn.close();
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(2);
        }
    }

    /* Given a student identification number, ths function should print
     * - the name of the student, the students national identification number
     *   and their issued login name (something similar to a CID)
     * - the programme and branch (if any) that the student is following.
     * - the courses that the student has read, along with the grade.
     * - the courses that the student is registered to. (queue position if the student is waiting for the course)
     * - the number of mandatory courses that the student has yet to read.
     * - whether or not the student fulfills the requirements for graduation
     */
    static void getInformation(Connection conn, String student) throws SQLException
    {
        String branch;
        PreparedStatement st = conn.prepareStatement("SELECT name, login, Student.program, branch FROM Student, StudentsFollowing WHERE ssn = ? AND student = ?") ;
        st.setString(1,student) ;
        st.setString(2,student) ;
        ResultSet rs = st.executeQuery() ;
        if (rs.next()){
            System.out.println("Name: " + rs.getString(1)) ;
            System.out.println("National identification number: "+ student);
            System.out.println("Login:" + rs.getString(2)) ;
            System.out.println("Program:" + rs.getString(3)) ;
            branch = rs.getString(4);
            if(branch == null)
                System.out.println("Branch: Student does not belong to a branch.") ;
            else
                System.out.println("Branch: " + branch) ;         

        }
        st = conn.prepareStatement("SELECT course, grade, credits FROM FinishedCourses WHERE student = ?") ;
        st.setString(1,student) ;
        rs = st.executeQuery() ;
        System.out.println("Finished courses");
        while (rs.next())
            System.out.println("Course: " + rs.getString(1)+"Grade: " + rs.getString(2) + 
                "Credits: " +  rs.getString(3)) ;

        st = conn.prepareStatement("SELECT course, status, place FROM (SELECT Registrations.student, Registrations.course, Registrations.status, CourseQueuePositions.place FROM Registrations LEFT OUTER JOIN CourseQueuePositions ON Registrations.student = CourseQueuePositions.student AND Registrations.course = CourseQueuePositions.course) AS tmp WHERE student = ?") ;
        st.setString(1,student) ;
        rs = st.executeQuery() ;
        System.out.println("Registered courses");
        while (rs.next()){
            System.out.print("Course: " + rs.getString(1)+"status: " + rs.getString(2)) ;
            if(rs.getString(3) != null)
                System.out.println(" position: "+ rs.getString(3));
            else
                System.out.println(" ");
        }

        st = conn.prepareStatement("SELECT mandatoryLeft, status FROM PathToGraduation WHERE student = ?") ;
        st.setString(1,student) ;
        rs = st.executeQuery() ;
        while (rs.next()){
            System.out.println("Mandatory courses left: " + rs.getString(1)) ;
            System.out.println("Ready for graduation: " + rs.getString(2)) ;
        }
        rs.close() ;
        st.close() ;
    }

    /* Register: Given a student id number and a course code, this function
     * should try to register the student for that course.
     */
    static void registerStudent(Connection conn, String student, String course)
    throws SQLException
    {
        try{
        	PreparedStatement st =
        conn.prepareStatement("INSERT INTO Registrations VALUES (?,?)") ;
        st.setString(1,student) ;
        st.setString(2,course) ;
        st.executeUpdate() ;    
        st.close() ;
        System.out.println(" Successful registration");
        }catch(SQLException e) {
        	System.out.print(e);
        }


    }

    /* Unregister: Given a student id number and a course code, this function
     * should unregister the student from that course.
     */
    static void unregisterStudent(Connection conn, String student, String course)
    throws SQLException
    {
        try{
            PreparedStatement st =
        conn.prepareStatement("DELETE FROM Registrations WHERE student = ? AND course = ?") ;
        st.setString(1,student) ;
        st.setString(2,course) ;
        st.executeUpdate() ;    
        st.close() ;
        System.out.println(" Successful unregistration");
        }catch(SQLException e) {
            System.out.print(e);
        }

    }
    
}