import java.util.ArrayList;
import java.util.Map;
public class JoinAlgorithm
{
    public static ArrayList<Map<String, String>> MeNestedLoop(ArrayList<Map<String, String>> table1, ArrayList<Map<String, String>> table2, String[] join)
    {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        //assumptions: block size = 100 record
        //1 for table1, 2 for table2
        ArrayList<Map<String, String>> outerTable = table1.size() < table2.size() ? table1 : table2;
        ArrayList<Map<String, String>> innerTable = table1.size() > table2.size() ? table1 : table2;
        for(Map<String, String> record1: outerTable)
        {
            for(Map<String, String> record2: innerTable)
            {

            }
        }


        return result;
    }
}
