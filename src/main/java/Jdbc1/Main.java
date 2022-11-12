package Jdbc1;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    // CREATE DATABASE mydb;
    static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/rental?serverTimezone=Europe/Kiev";
    static final String DB_USER = "postgres";
    static final String DB_PASSWORD = "q1w2t5y6";

    static Connection conn;

    enum Realestatecategory {
        residental,
        commercial,
        industrial,
        raw_land,
        other
    }
    enum State {
        Ukraine,
        Georgia
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            try {
                // create connection
                conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
 //               initDB();

                while (true) {
                    System.out.println("1: add object of property");
                    System.out.println("2: get certain object by parameter");
                    System.out.println("3: delete object by ID:");
//                    System.out.println("4: change object");
                    System.out.println("5: view objects");
                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addREtoDB(sc);
                            break;
//                        case "2":
//                            insertRandomClients(sc);
//                            break;
                        case "2":
                            deleteObject(sc);
                            break;
//                        case "4":
//                            changeObject(sc);
//                            break;
                        case "3":
                            System.out.println("Select parameter:");
                            System.out.println("1. All parameters:");
                            System.out.println("2. Object category type:");
                            System.out.println("3. State:");
                            System.out.println("4. City:");
                            System.out.println("-->");
                            String select = sc.nextLine();
                            switch (select) {
                                case "1":
                                    viewObjects("SELECT * FROM REALESTATE");
                                    break;
                                case "2":
                                    PreparedStatement psObjType = conn.prepareStatement("SELECT DISTINCT category FROM REALESTATE");
                                    ResultSet rsObjType = psObjType.executeQuery();
                                    Map <Integer, String> objTypeMap = new HashMap<>();
                                    int objTypeSerial = 0;
                                    while (rsObjType.next()){
                                        objTypeSerial = objTypeSerial + 1;
                                        String category = rsObjType.getString("category");
                                        objTypeMap.put(objTypeSerial, category);
                                    }
                                    System.out.println("Choose category: ");
                                    objTypeMap.entrySet().forEach(System.out::println);
                                    int categoryChoice = sc.nextInt();
                                    String sqlCategory = "SELECT * FROM REALESTATE WHERE category = \'" + objTypeMap.get(categoryChoice) + "\';";
                                    viewObjects(sqlCategory);
                                    break;
                                case "3":
                                    PreparedStatement psState = conn.prepareStatement("SELECT DISTINCT state FROM REALESTATE");
                                    ResultSet rsState = psState.executeQuery();
                                    Map <Integer, String> statesMap = new HashMap<>();
                                    int stateSerial = 0;
                                    while (rsState.next()){
                                        stateSerial = stateSerial + 1;
                                        String state = rsState.getString("state");
                                        statesMap.put(stateSerial, state);
                                    }
                                    System.out.println("Choose state: ");
                                    statesMap.entrySet().forEach(System.out::println);
                                    int stateChoice = sc.nextInt();
                                    String sqlState = "SELECT * FROM REALESTATE WHERE state = \'" + statesMap.get(stateChoice) + "\';";
                                    viewObjects(sqlState);
                                    break;
                                case "4":
                                    PreparedStatement psCity = conn.prepareStatement("SELECT DISTINCT city FROM REALESTATE");
                                    ResultSet resSet = psCity.executeQuery();
                                    Map<Integer, String> citiesMap = new HashMap<>();
                                    int citySerial = 0;
                                    while (resSet.next()){
                                        citySerial = citySerial + 1;
                                        String city = resSet.getString("city");
                                        citiesMap.put(citySerial, city);
                                    }
                                    System.out.println("Choose city: ");
                                    citiesMap.entrySet().forEach(System.out::println);
                                    int choice = sc.nextInt();
                                    String sqlCity = "SELECT * FROM REALESTATE WHERE city = \'" + citiesMap.get(choice) + "\';";
                                    viewObjects(sqlCity);
                                    break;
                            }
                            break;
                        default:
                            return;
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } finally {
                sc.close();
                if (conn != null) conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
    }

//    private static void initDB() throws SQLException {
//        Statement st = conn.createStatement();
//        try {
//            st.execute("DROP TABLE IF EXISTS realEstate ");
//            st.execute("DROP TYPE IF EXISTS realEstateCategory;");
//            st.execute("DROP TYPE IF EXISTS country;");
//            st.execute("DROP TYPE IF EXISTS proprietorshipType;");
//            st.execute("CREATE TYPE realEstateCategory AS ENUM ('residental', 'commercial', 'industrial', 'land');");
//            st.execute("CREATE TYPE country AS ENUM ('Ukraine', 'Georgia');");
//            st.execute("CREATE TYPE proprietorshipType AS ENUM ('individual ownership', 'partial ownership', 'lease');");
//            st.execute("CREATE TABLE realEstate (id SERIAL PRIMARY KEY, category realEstateCategory NOT NULL, " +
//                    "state country NOT NULL, city VARCHAR (30) NOT NULL, address VARCHAR (50) NOT NULL, square INT NOT NULL, " +
//                    "dateStartOwning DATE NOT NULL, typeOfOwning proprietorshipType NOT NULL, purchasePrice INT, rentPrice INT, " +
//                    "soldPrice INT, leasePrice INT, currentTenant VARCHAR (20), tenantPhoneNumber VARCHAR (13));");
//        } finally {
//            st.close();
//        }

        /*
        try (Statement st1 = conn.createStatement()) {
            st1.execute("DROP TABLE IF EXISTS realEstate");
            st1.execute("CREATE TABLE realEstate (id INT NOT NULL " +
                    "AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) " +
                    "NOT NULL, age INT)");
        }
         */
//    }

    private static void addREtoDB(Scanner sc) throws SQLException, ParseException {
        Realestatecategory category;
        State state;
        String city;
        String address;
        String square;
        String type_of_owning = "";
        Date start_owning_date = new Date(0);
        String purchase_price = "";
        String rent_price = "";
        String lease_price = "";
        String sale_date = "";
        String sale_price = "";
        Date date_of_sale;

        System.out.print("Enter object category: ");
        category = Realestatecategory.valueOf(sc.nextLine());

        System.out.print("Enter the state where the object is situated: ");
        state = State.valueOf(sc.nextLine());

        System.out.print("Enter the city where the object is situated: ");
        city = sc.nextLine();

        System.out.print("Enter the address of the object: ");
        address = sc.nextLine();

        System.out.print("Enter the square of object: ");
        square = sc.nextLine();

        System.out.print("Enter the type of proprietorship (property or leasing): ");
        String answer = sc.nextLine();

        if (Objects.equals(answer, "property")) {
            type_of_owning = answer;
            lease_price = "0"; //there is no lease price because it is property

            System.out.print("Enter the purchase price: ");
            purchase_price = sc.nextLine();

            System.out.print("Enter the date of purchase (yyyy-MM-dd): ");
            String start = sc.nextLine();
            start_owning_date = Date.valueOf(start);

            System.out.print("Enter sale price: ");
            sale_price = sc.nextLine();
            if (sale_price.equals("0")){
                date_of_sale = null;
            } else {
            System.out.print("Enter date of sale (yyyy-MM-dd): ");
            sale_date = sc.nextLine();
                date_of_sale = Date.valueOf(sale_date);
            }
        }

        if (Objects.equals(answer, "leasing")){
            type_of_owning = answer;
            purchase_price = "0"; //there is no purchase price, object leased by the owner

            System.out.println("Enter the leasing price: ");
            lease_price = sc.nextLine();

            System.out.println("Enter the date of leasing begin: ");
            String start = sc.nextLine();
            start_owning_date = (Date) new SimpleDateFormat("yyyy/MM/dd").parse(start);

        }

        System.out.print("Enter the rent price: ");
        rent_price = sc.nextLine();
        System.out.println("Thank you. The initialization of a real estate object accomplished.");

        //String sql = "INSERT INTO Clients (name, age) " +
          //      "VALUES(" + name + ", " + age + ")";

        PreparedStatement ps = conn.prepareStatement("INSERT INTO REALESTATE (" +
                "category, " +
                "state, " +
                "city, " +
                "address,  " +
                "square, " +
                "type_of_owning," +
                "start_owning_date," +
                "purchase_price," +
                "rent_price," +
                "lease_price," +
                "sale_price," +
                "sale_date) " +

                "VALUES(?, ?::\"country\", ?, ?, ?, ?::\"proprietorshiptype\", ?, ?::\"money\", ?::\"money\", ?::\"money\", ?::\"money\", ?::\"date\")");
        try {
            ps.setString(1, category.name());
            ps.setString(2, state.name());
            ps.setString(3, city);
            ps.setString(4, address);
            ps.setInt(5, Integer.parseInt(square));
            ps.setString(6, type_of_owning);
            ps.setDate(7, start_owning_date);
            ps.setString(8, purchase_price);
            ps.setString(9, rent_price);
            ps.setString(10, lease_price);
            ps.setString(11, sale_price);
            if (sale_date.equals("")){
                ps.setString(12, null);
            } else {
                ps.setString(12, sale_date);
            }
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE

//            ps.setString(1, name + "1");
//            ps.setInt(2, age);
//            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    private static void deleteObject(Scanner sc) throws SQLException {
        System.out.print("Enter object id: ");
        String id = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM REALESTATE WHERE id = ?");
        try {
            ps.setString(1, id);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        } finally {
            ps.close();
        }
    }

//    private static void changeObject(Scanner sc) throws SQLException {
//        System.out.print("Enter client name: ");
//        String name = sc.nextLine();
//        System.out.print("Enter new age: ");
//        String sAge = sc.nextLine();
//        int age = Integer.parseInt(sAge);
//
//        PreparedStatement ps = conn.prepareStatement("UPDATE Clients SET age = ? WHERE name = ?");
//        try {
//            ps.setInt(1, age);
//            ps.setString(2, name);
//            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
//        } finally {
//            ps.close();
//        }
//    }

//    private static void insertRandomClients(Scanner sc) throws SQLException {
//        System.out.print("Enter clients count: ");
//        String sCount = sc.nextLine();
//        int count = Integer.parseInt(sCount);
//        Random rnd = new Random();
//
//        conn.setAutoCommit(false); // enable transactions
//        try {
//            try {
//                PreparedStatement ps = conn.prepareStatement("INSERT INTO Clients (name, age) VALUES(?, ?)");
//                try {
//                    for (int i = 0; i < count; i++) {
//                        ps.setString(1, "Name" + i);
//                        ps.setInt(2, rnd.nextInt(100));
//                        ps.executeUpdate();
//                    }
//                    conn.commit();
//                } finally {
//                    ps.close();
//                }
//            } catch (Exception ex) {
//                conn.rollback();
//            }
//        } finally {
//            conn.setAutoCommit(true); // return to default mode
//        }
//    }

    /*

   -> 1 2 3
      -----
      -----
      -----

     */

    private static void viewObjects(String sqlSelect) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sqlSelect);
        try {
            // table of data representing a database result set,
            ResultSet rs = ps.executeQuery();

            try {
                // can be used to get information about the types and properties of the columns in a ResultSet object
                ResultSetMetaData md = rs.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.print(md.getColumnName(i) + "\t\t");
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.print(rs.getString(i) + "\t\t");
                    }
                    System.out.println();
                }
            } finally {
                rs.close(); // rs can't be null according to the docs
            }
        } finally {
            ps.close();
        }
    }
}
