import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
public class JoinAlgorithm
{
    public static ArrayList<Map<String, String>> MeNestedLoop(ArrayList<Map<String, String>> customers, ArrayList<Map<String, String>> orders, String[] join)
    {
        //assumptions: block size = 100 record

        String left = join[0];
        String op = join[1];
        String right = join[2];



        ArrayList<Map<String, String>> result = new ArrayList<>();

        ArrayList<Map<String, String>> outerTable; //= customers.size() < orders.size() ? customers : orders;
        ArrayList<Map<String, String>> innerTable; //= customers.size() > orders.size() ? customers : orders;

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



        for(Map<String, String> record1: outerTable)
        {

            String val1 = record1.get(left);

            for(Map<String, String> record2: innerTable)
            {
                boolean valid = false;
                String val2 = record2.get(right);
                if(val1 != null && val2 != null)
                {
                    if (op.equals("="))
                    {
                        valid = val1.equals(val2);
                    }
                    else
                    {
                        //No need to check if it's a string as the operator is not =
                        double numericVal1, numericVal2;
                        numericVal1 = Double.parseDouble(val1);
                        numericVal2 = Double.parseDouble(val2);

                        if (op.equals(">")) valid = (numericVal1 > numericVal2);
                        else if (op.equals("<")) valid = (numericVal1 < numericVal2);
                        else if (op.equals(">=")) valid = (numericVal1 >= numericVal2);
                        else if (op.equals("<=")) valid = (numericVal1 <= numericVal2);
                    }

                    if (valid)
                    {
                        Map<String, String> joinedRows = new LinkedHashMap<>();
                        joinedRows.putAll(record1);
                        joinedRows.putAll(record2);
                        result.add(joinedRows);
                    }
                }
            }
        }


        return result;
    }
}
