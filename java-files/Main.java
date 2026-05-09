import java.nio.file.Files;
import java.nio.file.Paths;

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

    }


}

