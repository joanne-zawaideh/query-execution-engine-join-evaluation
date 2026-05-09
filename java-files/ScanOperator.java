import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;

public class ScanOperator
{
    public static ArrayList<Map<String, String>> ScanMe(String tableName) throws FileNotFoundException
    {
        ArrayList<Map<String, String>> table = new ArrayList<>();

        tableName = tableName.toLowerCase().trim();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("input-files\\" + tableName + ".csv"));

            String[] headers = br.readLine().split(",");

            String line;
            while ((line = br.readLine()) != null)
            {
                String[] values = line.split(",");

                Map<String, String> row = new LinkedHashMap<>();

                for(int i = 0; i< headers.length; i++)
                {
                    row.put(headers[i].trim(), values[i].trim()); // here you can add the table name: tableName+"." ...
                }

                table.add(row);
            }
            br.close();
        }
        catch(IOException ex)
        {
            System.out.println(ex.getMessage());
        }

        return table;
    }
}
