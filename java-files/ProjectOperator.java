import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

public class ProjectOperator
{
    public static ArrayList<Map<String, String>> ProjectMe(ArrayList<Map<String, String>> whereResult, String[] selectCols) throws Exception
    {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        if(selectCols[0] == "*")
        {
            Map<String, String> temp = whereResult.get(0);
            //must use toArray() to access length property
            selectCols = new String[temp.keySet().toArray().length];
            for(int i = 0; i < selectCols.length; i++)
                selectCols[i] = temp.keySet().toArray()[i].toString(); //must use toArray() to use [i], also toString() as keySet returns references
        }

        else
        {
            //check if cols are valid
            Map<String, String> temp = whereResult.get(0);
            for (String col : selectCols) {
                if (!temp.containsKey(col))
                    throw new Exception("Column " + col + " does not exist in the current table");
            }
        }

        for(Map<String, String> record: whereResult)
        {
            Map<String, String> projectedRow = new LinkedHashMap<>();
            for(String col: selectCols)
            {
                projectedRow.put(col, record.get(col));
            }
            result.add(projectedRow);
        }
        return result;
    }

}
