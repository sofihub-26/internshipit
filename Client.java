import java.sql.*;
import java.util.Scanner;

public class Client {

    Scanner sc = new Scanner(System.in);

    public void addClient() {
        try {
            Connection con = DBconnection.getConnection();

            System.out.print("Client Name : ");
            String name = sc.nextLine();

            System.out.print("Phone : ");
            String phone = sc.nextLine();

            System.out.print("Email : ");
            String email = sc.nextLine();

            String sql = "INSERT INTO clients(client_name,phone,email) VALUES(?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);

            ps.executeUpdate();
            System.out.println("Client Added Successfully");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void viewClients() {
        try {
            Connection con = DBconnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM clients");

            System.out.println("--------------------------------------------");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + "  "
                        + rs.getString("client_name") + "  "
                        + rs.getString("phone") + "  "
                        + rs.getString("email"));
            }
            System.out.println("--------------------------------------------");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void deleteClient() {
        try {
            Connection con = DBconnection.getConnection();

            System.out.print("Enter Client ID : ");
            int id = sc.nextInt();

            String sql = "DELETE FROM clients WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("Client Deleted Successfully");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
