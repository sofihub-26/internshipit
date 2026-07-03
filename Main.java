import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        Client client = new Client();

        while (true) {

            System.out.println("\n===== IMS MENU =====");
            System.out.println("1. Add Client");
            System.out.println("2. View Clients");
            System.out.println("3. Delete Client");
            System.out.println("4. Exit");

            System.out.print("Enter Choice : ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    sc.nextLine();
                    client.addClient();
                    break;

                case 2:
                    client.viewClients();
                    break;

                case 3:
                    client.deleteClient();
                    break;

                case 4:
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid Choice");
                    break;

            }

        }

    }

}
