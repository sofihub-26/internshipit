import java.sql.Connection;
import java.sql.Statement;

public class DBmigration {
    public static void main(String[] args) {
        try (Connection con = DBconnection.getConnection();
             Statement st = con.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS clients ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "client_name VARCHAR(100) NOT NULL,"
                    + "phone VARCHAR(15),"
                    + "email VARCHAR(100),"
                    + "group_name VARCHAR(50),"
                    + "chain VARCHAR(100),"
                    + "brand VARCHAR(100),"
                    + "address VARCHAR(255)"
                    + ")");

            st.execute("CREATE TABLE IF NOT EXISTS estimates ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "client_name VARCHAR(100),"
                    + "date VARCHAR(20),"
                    + "amount DOUBLE,"
                    + "gst DOUBLE,"
                    + "total DOUBLE,"
                    + "status VARCHAR(50),"
                    + "remarks VARCHAR(255)"
                    + ")");

            System.out.println("Database migration completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
