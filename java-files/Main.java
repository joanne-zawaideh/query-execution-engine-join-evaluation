import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
//        String query = "";
//        try {
//            query = Files.readString(Paths.get("C:\\Users\\PC\\Documents\\DBMS\\DBMS-Project\\src\\input.txt"));
//            System.out.println(query);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        //call parser to check syntax
//        Query q1 = new Query(query);
//        try {
//            boolean validSyntax = q1.ParseMe();
//        } catch (Exception e){
//            System.out.println("ERROR: " + e.getMessage());
        String wherePart="cust.id=ord.id AND a.value>50";
        String[] conditions = wherePart.split("AND");
        for(String s: conditions)
            System.out.println(s);
//        }

    }


}