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

        tableName = tableName.trim();
        try
        {
            FileReader fr = new FileReader("input-files\\" + tableName + ".csv");
            BufferedReader br = new BufferedReader(fr);

            String[] headers = br.readLine().split(",");

            String line = br.readLine();
            while (line != null)
            {
                String[] values = line.split(",");

                Map<String, String> row = new LinkedHashMap<>();

                for(int i = 0; i< headers.length; i++)
                {
                    row.put(tableName + "." + headers[i].trim(), values[i].trim());
                }

                table.add(row);
                line = br.readLine();
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
