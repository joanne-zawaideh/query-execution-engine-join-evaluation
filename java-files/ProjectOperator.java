import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

public class ProjectOperator
{
    public static ArrayList<Map<String, String>> ProjectMe(ArrayList<Map<String, String>> joinResult, String[] selectCols) throws Exception
    {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        //check if cols are valid
        Map<String, String> temp = joinResult.get(0);
        for(String col: selectCols) {
            if(!temp.containsKey(col))
                throw new Exception("Column " + col + "does not exist");
        }
        for(String col: selectCols)
        {
            for(Map<String, String> record: joinResult)
            {

            }
        }
        return result;
    }

}
