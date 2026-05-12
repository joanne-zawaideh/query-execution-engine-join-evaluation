import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ExecutionEngine
{
    private long executionTime;
    private int blockAccesses;

    public static void ExecuteMe(String joinAlgorithm)
    {
//        System.currentTimeMillis()
        //read the query
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


        //Start execution
        Query q = new Query(query);
        try
        {
            //call parser to check syntax
            q.ParseMe();



            //Scan the tables in the query
            ArrayList<Map<String, String>> firstTable = ScanOperator.ScanMe(q.getTables()[0]);

            //we must check if there is a second table in the first place
            ArrayList<Map<String, String>> secondTable = null;
            if(q.getTables().length > 1)
                secondTable = ScanOperator.ScanMe(q.getTables()[1]);



            //Execute where condition(s)
            ArrayList<Map<String, String>> whereResult;

            //if join exists (there are two tables)
            if(q.getJoin() != null)
            {
                //if filter condition exists
                if(q.getFilter() != null)
                {
                    String filterCol = q.getFilter()[0];
                    if(firstTable.get(0).containsKey(filterCol))
                    {
                        firstTable = SelectOperator.SelectMe(firstTable, q.getFilter());
                    }
                    else
                    {
                        secondTable = SelectOperator.SelectMe(secondTable, q.getFilter());
                    }
                }
                if(joinAlgorithm.toLowerCase().equals("nested loop"))
                    whereResult = JoinAlgorithm.MeNestedLoop(firstTable, secondTable, q.getJoin());
                else
                    whereResult = JoinAlgorithm.MeHashJoin(firstTable, secondTable, q.getJoin());
            }

            //if join doesn't exist and filter exist (only firstTable exist)
            else if(q.getFilter() != null)
            {
                whereResult = SelectOperator.SelectMe(firstTable, q.getFilter());
            }


            //Neither join nor filter exist (only firstTable exist)
            else
            {
                whereResult = firstTable;
            }




            //project the selected columns from whereResult and write the result to a file
            ArrayList<Map<String, String>> finalResult = ProjectOperator.ProjectMe(whereResult,q.getSelectCols());

            if(joinAlgorithm.toLowerCase().equals("nested loop"))
                writeFile("input-files\\nestedLoopResult.txt", finalResult);
            else
                writeFile("input-files\\hashResult.txt", finalResult);


//            //TESTING PURPOSES
//            for (Map<String, String> row : finalResult) {
//
//                for (Map.Entry<String, String> entry : row.entrySet()) {
//                    System.out.print(entry.getKey() + ": " + entry.getValue() + " | ");
//                }
//                System.out.println();
//            }
//            System.out.println(finalResult.size());
        }
        catch (Exception e)
        {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void writeFile(String path, ArrayList<Map<String, String>> finalResult) throws Exception
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(fw);

            //write performance details


            //write result table
            bw.write("Result Table:");
            bw.newLine();

            //write column headings
            Map<String, String> temp = finalResult.get(0);
            int lineLength = 0;
            for(String key: temp.keySet())
            {
                bw.write(key);
                lineLength += key.length();
                //each column will be 30 characters long
                int numOfSpaces = 30 - key.length();
                if(numOfSpaces > 0)
                {
                    bw.write(" ".repeat(numOfSpaces));
                    lineLength += numOfSpaces;
                }
            }
            bw.newLine();
            bw.write("-".repeat(lineLength));
            bw.newLine();

            //write values of each record
            for(Map<String, String> record: finalResult)
            {
                for(String value: record.values())
                {
                    bw.write(value);
                    int numOfSpaces = 30 - value.length();
                    if(numOfSpaces > 0)
                    {
                        bw.write(" ".repeat(numOfSpaces));
                    }
                }
                bw.newLine();
            }







        }
        catch (IOException e)
        {
            System.out.println("WRITING ERROR: " + e.getMessage());
        }

    }
}
