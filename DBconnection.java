import java.sql.Connection;
import java.sql.DriverManager;

public class DBconnection {

    static final String URL = "jdbc:mysql://localhost:3306/imsdb";
    static final String USER = "root";
    static final String PASSWORD = "your_password";

    public static Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            return con;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
