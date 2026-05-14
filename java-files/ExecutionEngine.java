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
    private static long executionTime;
    private static int blockAccesses;
    private static int bfr;
    private static int numberOfBuffers;

    private static long scanTime;
    private static long filterTime;
    private static long joinTime;
    private static long projectTime;

    private static int scanBlock;
    private static int filterBlock;
    private static int joinBlock;
    private static int projectBlock;


    public static void ExecuteMe(String joinAlgorithm)
    {
        numberOfBuffers = 7;
        blockAccesses = 0;
        bfr = 100;

        scanBlock = 0;
        filterBlock = 0;
        joinBlock = 0;
        projectBlock = 0;
        long startTime = System.currentTimeMillis();
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
            blockAccesses += Math.ceil(firstTable.size() / bfr);
            scanBlock += Math.ceil(firstTable.size() / bfr);

            //we must check if there is a second table in the first place
            ArrayList<Map<String, String>> secondTable = null;
            if(q.getTables().length > 1)
            {
                secondTable = ScanOperator.ScanMe(q.getTables()[1]);
                blockAccesses += Math.ceil(secondTable.size() / bfr);
                scanBlock += Math.ceil(secondTable.size() / bfr);
            }
            System.out.println("After scanning: " + blockAccesses);


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
                        filterBlock += Math.ceil((float)firstTable.size() / bfr);
                        blockAccesses += Math.ceil((float)firstTable.size() / bfr);
                        firstTable = SelectOperator.SelectMe(firstTable, q.getFilter());
                    }
                    else
                    {
                        filterBlock += Math.ceil((float)secondTable.size() / bfr);
                        blockAccesses += Math.ceil((float)secondTable.size() / bfr);
                        secondTable = SelectOperator.SelectMe(secondTable, q.getFilter());
                    }
                    System.out.println("After Filter: " + blockAccesses);
                }
                if(joinAlgorithm.toLowerCase().equals("nested loop"))
                {
                    if(firstTable.size() < secondTable.size())
                    {
                        //firstTable is the outerTable
                        blockAccesses += Math.ceil((float)firstTable.size() / bfr)
                                + (Math.ceil((float)Math.ceil((float)firstTable.size() / bfr) / (numberOfBuffers - 2)) * Math.ceil((float)secondTable.size() / bfr));

                        joinBlock += Math.ceil((float)firstTable.size() / bfr)
                                + (Math.ceil((float)Math.ceil((float)firstTable.size() / bfr) / (numberOfBuffers - 2)) * Math.ceil((float)secondTable.size() / bfr));
                    }
                    else
                    {
                        //secondTable is the outerTable
                        blockAccesses += Math.ceil((float)secondTable.size() / bfr)
                                + (Math.ceil((float)Math.ceil((float)secondTable.size() / bfr) / (numberOfBuffers - 2)) * Math.ceil((float)firstTable.size() / bfr));

                        joinBlock += Math.ceil((float)secondTable.size() / bfr)
                                + (Math.ceil((float)Math.ceil((float)secondTable.size() / bfr) / (numberOfBuffers - 2)) * Math.ceil((float)firstTable.size() / bfr));
                    }
                    whereResult = JoinAlgorithm.MeNestedLoop(firstTable, secondTable, q.getJoin());
                }
                else
                {
                    joinBlock += Math.ceil((float)firstTable.size() / bfr) + Math.ceil((float)secondTable.size() / bfr);
                    blockAccesses += Math.ceil((float)firstTable.size() / bfr) + Math.ceil((float)secondTable.size() / bfr);
                    whereResult = JoinAlgorithm.MeHashJoin(firstTable, secondTable, q.getJoin());
                }
                System.out.println("After Join: " + blockAccesses);
            }

            //if join doesn't exist and filter exist (only firstTable exist)
            else if(q.getFilter() != null)
            {
                filterBlock += Math.ceil((float)firstTable.size() / bfr);
                blockAccesses += Math.ceil((float)firstTable.size() / bfr);
                whereResult = SelectOperator.SelectMe(firstTable, q.getFilter());
                System.out.println("After Filter: " + blockAccesses);
            }


            //Neither join nor filter exist (only firstTable exist)
            else
            {
                whereResult = firstTable;
            }


            //project the selected columns from whereResult and write the result to a filter
            projectBlock += Math.ceil((float)whereResult.size() / bfr);
            blockAccesses += Math.ceil((float)whereResult.size() / bfr);
            ArrayList<Map<String, String>> finalResult = ProjectOperator.ProjectMe(whereResult,q.getSelectCols());

            System.out.println("After Projection: " + blockAccesses);

            long endTime = System.currentTimeMillis();
            executionTime = endTime - startTime;

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
            bw.write("Performance Details:\n");
            String details = "- BFR = " + bfr + "\n"
                    + "- Number of Buffers = " + numberOfBuffers + "\n"
                    + "- Execution Time = " + executionTime + " milliseconds \n"
                    + "- Number of Blocks Accessed = " + blockAccesses + "\n"
                    + "- Number of records returned = " + finalResult.size() + "\n\n";
            bw.write(details);
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
            bw.close();
        }
        catch (IOException e)
        {
            System.out.println("WRITING ERROR: " + e.getMessage());
        }

    }
}
