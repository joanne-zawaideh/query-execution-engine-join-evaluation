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
    private static int bfr;
    private static int numberOfBuffers;

    private static long totalTime;
    private static long scanTime;
    private static long filterTime;
    private static long nestedJoinTime;
    private static long hashJoinTime;
    private static long projectTime;

    private static int scanBlock;
    private static int filterBlock;
    private static int nestedJoinBlock;
    private static int hashJoinBlock;
    private static int projectBlock;
    private static int totalBlock;

    private static long joinTime;
    private static int joinBlock;

    public static void ExecuteMe()
    {
        numberOfBuffers = 7;
        bfr = 100;


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
            long startOfScan = System.currentTimeMillis();
            ArrayList<Map<String, String>> firstTable = ScanOperator.ScanMe(q.getTables()[0]);
            scanBlock = (int) Math.ceil((float)firstTable.size() / bfr);


            //we must check if there is a second table in the first place
            ArrayList<Map<String, String>> secondTable = null;
            if(q.getTables().length > 1)
            {
                secondTable = ScanOperator.ScanMe(q.getTables()[1]);
                scanBlock = (int) Math.ceil((float)secondTable.size() / bfr);
            }
            scanTime = System.currentTimeMillis() - startOfScan;



            //Execute where condition(s)
            ArrayList<Map<String, String>> whereResult = null;
            ArrayList<Map<String, String>> nestedResult = null;
            ArrayList<Map<String, String>> hashResult = null;

            boolean joinExist = false;

            long startOfFilter, startOfJoin;

            //if join exists (there are two tables)
            if(q.getJoin() != null)
            {
                joinExist = true;
                //if filter condition exists
                if(q.getFilter() != null)
                {
                    startOfFilter = System.currentTimeMillis();
                    String filterCol = q.getFilter()[0];
                    if(firstTable.get(0).containsKey(filterCol))
                    {
                        filterBlock = (int) Math.ceil((float)firstTable.size() / bfr);
                        firstTable = SelectOperator.SelectMe(firstTable, q.getFilter());
                    }
                    else
                    {
                        filterBlock = (int) Math.ceil((float)secondTable.size() / bfr);
                        secondTable = SelectOperator.SelectMe(secondTable, q.getFilter());
                    }
                    filterTime = System.currentTimeMillis() - startOfFilter;
                }

                //nested loop block calculations
                if(firstTable.size() < secondTable.size())
                {
                    //firstTable is the outerTable
                     nestedJoinBlock = (int) (Math.ceil((float)firstTable.size() / bfr)
                             + (Math.ceil((float)Math.ceil((float)firstTable.size() / bfr) / (numberOfBuffers - 2)) * Math.ceil((float)secondTable.size() / bfr)));
                }
                else
                {
                    //secondTable is the outerTable
                    nestedJoinBlock = (int) (Math.ceil((float)secondTable.size() / bfr)
                            + (Math.ceil((float)Math.ceil((float)secondTable.size() / bfr) / (numberOfBuffers - 2)) * Math.ceil((float)firstTable.size() / bfr)));
                }

                //hash join
                hashJoinBlock = (int) (Math.ceil((float)firstTable.size() / bfr) + Math.ceil((float)secondTable.size() / bfr));
                startOfJoin = System.currentTimeMillis();
                hashResult = JoinAlgorithm.MeHashJoin(firstTable, secondTable, q.getJoin());
                hashJoinTime = System.currentTimeMillis() - startOfJoin;

                //nested loop join
                startOfJoin = System.currentTimeMillis();
                nestedResult = JoinAlgorithm.MeNestedLoop(firstTable, secondTable, q.getJoin());
                nestedJoinTime = System.currentTimeMillis() - startOfJoin;
            }

            //if join doesn't exist and filter exist (only firstTable exist)
            else if(q.getFilter() != null)
            {
                filterBlock = (int) Math.ceil((float)firstTable.size() / bfr);
                startOfFilter = System.currentTimeMillis();
                whereResult = SelectOperator.SelectMe(firstTable, q.getFilter());
                filterTime = System.currentTimeMillis() - startOfFilter;
            }


            //Neither join nor filter exist (only firstTable exist)
            else
            {
                whereResult = firstTable;
                filterTime = 0;
                hashJoinTime = 0;
                nestedJoinTime = 0;
            }

            ArrayList<Map<String, String>> nestedFinalResult = null;
            ArrayList<Map<String, String>> hashFinalResult = null;
            if(joinExist)
            {
                projectBlock = (int) Math.ceil((float)nestedResult.size() / bfr);
                long startOfProject = System.currentTimeMillis();
                nestedFinalResult = ProjectOperator.ProjectMe(nestedResult,q.getSelectCols());
                projectTime = System.currentTimeMillis() - startOfProject;


                hashFinalResult = ProjectOperator.ProjectMe(hashResult,q.getSelectCols());
            }
            else
            {
                projectBlock = (int) Math.ceil((float)whereResult.size() / bfr);
                long startOfProject = System.currentTimeMillis();
                ArrayList<Map<String, String>> finalResult = ProjectOperator.ProjectMe(whereResult,q.getSelectCols());
                projectTime = System.currentTimeMillis() - startOfProject;
            }



            if(joinExist)
            {
                totalTime = scanTime + filterTime + nestedJoinTime + projectTime;
                totalBlock = scanBlock + filterBlock + nestedJoinBlock + projectBlock;
                joinTime = nestedJoinTime;
                joinBlock = nestedJoinBlock;
                writeFile("input-files\\nestedLoopResult.txt", nestedFinalResult);

                totalTime = scanTime + filterTime + hashJoinTime + projectTime;
                totalBlock = scanBlock + filterBlock + hashJoinBlock + projectBlock;
                joinTime = hashJoinTime;
                joinBlock = hashJoinBlock;
                writeFile("input-files\\hashResult.txt", hashFinalResult);
            }
            else
            {
                totalTime = scanTime + filterTime + projectTime;
                totalBlock = scanBlock + filterBlock + projectBlock;
                joinTime = 0;
                joinBlock = 0;
                writeFile("input-files\\nestedLoopResult.txt", whereResult);
                writeFile("input-files\\hashResult.txt", whereResult);
            }

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
                    + "- Total Execution Time = " + totalTime + " ms \n\t"
                    + "1) Scan Time = " + scanTime + " ms \n\t"
                    + "2) Select Time = " + filterTime + " ms \n\t"
                    + "3) Join Time = " + joinTime + " ms \n\t"
                    + "4) Project Time = " + projectTime + " ms \n"
                    + "- Total Number of Blocks Accessed = " + totalBlock + "\n\t"
                    + "1) Scan Block Accesses = " + scanBlock + "\n\t"
                    + "2) Select Block Accesses = " + filterBlock + "\n\t"
                    + "3) Join Block Accesses = " + joinBlock + "\n\t"
                    + "4) Project Block Accesses = " + projectBlock + "\n"
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
