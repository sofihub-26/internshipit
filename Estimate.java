import java.sql.*;
import java.util.Scanner;

public class Estimate {

    Scanner sc = new Scanner(System.in);

    public void addEstimate() {
        try {
            Connection con = DBconnection.getConnection();

            System.out.print("Client Name : ");
            String client = sc.nextLine();

            System.out.print("Amount : ");
            double amount = sc.nextDouble();
            sc.nextLine();

            double gst = amount * 0.18;
            double total = amount + gst;

            String sql = "INSERT INTO estimates(client_name,amount,gst,total) VALUES(?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, client);
            ps.setDouble(2, amount);
            ps.setDouble(3, gst);
            ps.setDouble(4, total);
            ps.executeUpdate();

            System.out.println("Estimate Saved Successfully");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void viewEstimates() {
        try {
            Connection con = DBconnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM estimates");

            System.out.println("\n-------------------------------");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + " | "
                        + rs.getString("client_name") + " | "
                        + rs.getDouble("amount") + " | "
                        + rs.getDouble("gst") + " | "
                        + rs.getDouble("total"));
            }
            System.out.println("-------------------------------");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
