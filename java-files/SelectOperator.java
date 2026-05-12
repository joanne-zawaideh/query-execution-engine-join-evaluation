import java.util.ArrayList;
import java.util.Map;

public class SelectOperator
{
    public static ArrayList<Map<String, String>> SelectMe(ArrayList<Map<String, String>> table, String[] filter) throws Exception
    {
        //filter[0] = col, filter[1] = op, filter[2] = value
        ArrayList<Map<String, String>> result = new ArrayList<>();
        String col = filter[0];
        String op = filter[1];
        String value = filter[2];

        //check col validity
        Map<String, String> temp = table.get(0);

        if(!temp.containsKey(col))
            throw new Exception("ERROR: Filter on Invalid Column");


        boolean isNumeric;
        double numericValue = 0;

        //convert value to its numeric data type if possible
        try
        {
            numericValue = Double.parseDouble(value);
            isNumeric = true;
        }
        catch(NumberFormatException e)
        {
            isNumeric = false;
            //remove single quotes
            value = value.substring(1, value.length() - 1);
        }

        for(Map<String, String> record: table)
        {
            String val = record.get(col);

            if(val != null)
            {
                boolean valid = false;
                if (isNumeric)
                {
                    double numericVal = Double.parseDouble(val);
                    if (op.equals("=")) valid = (numericVal == numericValue);
                    else if (op.equals("<")) valid = (numericVal < numericValue);
                    else if (op.equals(">")) valid = (numericVal > numericValue);
                    else if (op.equals("<=")) valid = (numericVal <= numericValue);
                    else if (op.equals(">=")) valid = (numericVal >= numericValue);
                }
                else
                {
                    if (op.equals("=")) valid = (value.equals(val));
                }
                if (valid) result.add(record);
            }
        }

        return result;
    }

}