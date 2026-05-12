import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
public class Main
{

    public static void main(String[] args)
    {
        String query = "";
        try
        {
            query = Files.readString(Paths.get("input-files\\input.txt"));
            System.out.println(query);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        //call parser to check syntax
        Query q1 = new Query(query);
        try
        {
            q1.ParseMe();
        }
        catch (Exception e)
        {
            System.out.println("ERROR: " + e.getMessage());
        }
        System.out.println(q1);
        try
        {
            ArrayList<Map<String, String>> table1 = ScanOperator.ScanMe("Customers");
            ArrayList<Map<String, String>> table2 = ScanOperator.ScanMe("Orders");


            ArrayList<Map<String, String>> table = JoinAlgorithm.MeNestedLoop(table1,table2, q1.getJoin());
            ArrayList<Map<String, String>> result = ProjectOperator.ProjectMe(table, q1.getSelectCols());
            for (Map<String, String> row : result) {

                for (Map.Entry<String, String> entry : row.entrySet()) {
                    System.out.print(entry.getKey() + ": " + entry.getValue() + " | ");
                }
                System.out.println();
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }


//            System.out.println();
//        }
//        ArrayList<Map<String, String>> result = SelectOperator.SelectMe(table, q1.getFilter());
//        for(Map<String, String> record: result)
//        {
//            for (Map.Entry<String, String> pair: record.entrySet())
//            {
//                System.out.print(pair.getKey() + ": " + pair.getValue() + " | ");
//            }
//            System.out.println();
//        }


    }


}

