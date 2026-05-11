import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
public class JoinAlgorithm
{
    //assumptions: BFR = 100 record
    //number of buffers = 7
    //unspanned structure
    //fixed-sized record

    public static ArrayList<Map<String, String>> MeNestedLoop(ArrayList<Map<String, String>> customers, ArrayList<Map<String, String>> orders, String[] join)
    {
        int numberOfBuffers=7;
        int BFR = 100;

        String left = join[0];
        String op = join[1];
        String right = join[2];

        ArrayList<Map<String, String>> result = new ArrayList<>();

        ArrayList<Map<String, String>> outerTable;
        ArrayList<Map<String, String>> innerTable;

        //we are assuming that left is the outer and right is the inner
        if(customers.size() < orders.size())
        {
            outerTable = customers;
            innerTable = orders;

            String leftTable = join[0].split("\\.")[0].trim().toLowerCase();
            if(!leftTable.equals("Customers"))
            {
                left = join[2];
                right = join[0];
            }
        }
        else
        {
            outerTable = orders;
            innerTable = customers;

            String leftTable = join[0].split("\\.")[0].trim().toLowerCase();
            if(!leftTable.equals("Orders"))
            {
                left = join[2];
                right = join[0];
            }
        }


        //outer loop accessing the smaller table nB-2 blocks at a time (every ((numberOfBuffers-2)*BFR) records)
        for(int i = 0; i < outerTable.size(); i += ((numberOfBuffers-2)*BFR))
        {
            //so we don't go out of bounds:
            int lastRecordIndex = Math.min(outerTable.size(), i + ((numberOfBuffers-2)*BFR));

            ArrayList<Map<String, String>> Chunk = new ArrayList<>();

            for(int j = i; j < lastRecordIndex; j++)
                Chunk.add(outerTable.get(j));

            for(Map<String, String> record1 : Chunk)
            {
                for(Map<String, String> record2 : innerTable)
                {
                    //left has the outerTable join column, right has the innerTable join columnm
                    String val1 = record1.get(left);
                    String val2 = record2.get(right);

                    if(val1 != null && val2 != null)
                    {
                        if(val1.equals(val2))
                        {
                            Map<String, String> joinedRow =new LinkedHashMap<>();
                            joinedRow.putAll(record1);
                            joinedRow.putAll(record2);
                            result.add(joinedRow);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static ArrayList<Map<String, String>> MeHashJoin(ArrayList<Map<String, String>> customers, ArrayList<Map<String, String>> orders, String[] join)
    {
        String left = join[0];
        String op = join[1];
        String right = join[2];

        ArrayList<Map<String, String>> result = new ArrayList<>();

        ArrayList<Map<String, String>> smallerTable;
        ArrayList<Map<String, String>> largerTable;

        //we are assuming that left is the outer and right is the inner
        if(customers.size() < orders.size())
        {
            smallerTable = customers;
            largerTable = orders;

            String leftTable = join[0].split("\\.")[0].trim().toLowerCase();
            if(!leftTable.equals("Customers"))
            {
                left = join[2];
                right = join[0];
            }
        }
        else
        {
            smallerTable = orders;
            largerTable = customers;

            String leftTable = join[0].split("\\.")[0].trim().toLowerCase();
            if(!leftTable.equals("Orders"))
            {
                left = join[2];
                right = join[0];
            }
        }
        //now left has smallerTable join column and right has largerTable

        HashMap<String, ArrayList<Map<String, String>>> hashTable = new HashMap<>();
        //first pass: iterate through smallerTable and fill hash table
        for(Map<String, String> record: smallerTable)
        {

        }
        return result;
    }
}
