import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
public class JoinAlgorithm
{
    //assumptions: BFR = 100 record
    //number of buffers = 7
    //unspanned structure
    //fixed-sized record

    // customers and orders after applying SelectMe()
    public static ArrayList<Map<String, String>> MeNestedLoop(ArrayList<Map<String, String>> table1, ArrayList<Map<String, String>> table2, String[] join) throws Exception
    {
        int numberOfBuffers=7;
        int BFR = 100;

        String col1 = join[0];
        String col2 = join[2];
        String left, right;

        ArrayList<Map<String, String>> result = new ArrayList<>();

        ArrayList<Map<String, String>> outerTable;
        ArrayList<Map<String, String>> innerTable;

        //we are assuming that left is the outer and right is the inner
        if(table1.size() < table2.size())
        {
            outerTable = table1;
            innerTable = table2;
        }
        else
        {
            outerTable = table2;
            innerTable = table1;
        }

        Map<String, String> outerTemp = outerTable.get(0);
        Map<String, String> innerTemp = innerTable.get(0);

        if(outerTemp.containsKey(col1) && innerTemp.containsKey(col2))
        {
           left = col1;
           right = col2;
        }
        else if(outerTemp.containsKey(col2) && innerTemp.containsKey(col1))
        {
            left = col2;
            right = col1;
        }
        else
        {
            //invalid cols (not in any of the tables)
            System.out.println("nested");
            throw new Exception("Join on Invalid Columns.");
        }


        //outer loop accessing the smaller table nB-2 blocks at a time (every ((numberOfBuffers-2)*BFR) records)
        for(int i = 0; i < outerTable.size(); i += ((numberOfBuffers-2)*BFR))
        {
            //so we don't go out of bounds:
            int lastRecordIndexChunk = Math.min(outerTable.size(), i + ((numberOfBuffers-2)*BFR));

            ArrayList<Map<String, String>> Chunk = new ArrayList<>();

            for(int j = i; j < lastRecordIndexChunk; j++)
                Chunk.add(outerTable.get(j));

            for(Map<String, String> record1 : Chunk)
            {
                //accessing the larger table one block at a time
                for(int k = 0; k < innerTable.size(); k+=BFR)
                {
                    //so we don't go out of bounds:
                    int lastRecordIndexBlock = Math.min(innerTable.size(), k+BFR);

                    ArrayList<Map<String, String>> block = new ArrayList<>();

                    for(int u = k; u < lastRecordIndexBlock; u++)
                        block.add(innerTable.get(u));

                    for(Map<String, String> record2 : block)
                    {
                        //left has the outerTable join column, right has the innerTable join columnm
                        String val1 = record1.get(left);
                        String val2 = record2.get(right);

                        if(val1 != null && val2 != null)
                        {
                            if(val1.equals(val2))
                            {
                                Map<String, String> joinedRow =new LinkedHashMap<>();
                                joinedRow.putAll(record1);
                                joinedRow.putAll(record2);
                                result.add(joinedRow);
                            }
                        }
                    }
                }


            }
        }
        return result;
    }

    public static ArrayList<Map<String, String>> MeHashJoin(ArrayList<Map<String, String>> table1, ArrayList<Map<String, String>> table2, String[] join) throws Exception
    {
        String col1 = join[0];
        String col2 = join[2];
        String left, right;

        ArrayList<Map<String, String>> result = new ArrayList<>();

        ArrayList<Map<String, String>> smallerTable;
        ArrayList<Map<String, String>> largerTable;

        //we are assuming that left is the outer and right is the inner
        if(table1.size() < table2.size())
        {
            smallerTable = table1;
            largerTable = table2;
        }
        else
        {
            smallerTable = table2;
            largerTable = table1;
        }

        Map<String, String> smallerTemp = smallerTable.get(0);
        Map<String, String> largerTemp = largerTable.get(0);
        if(smallerTemp.containsKey(col1) && largerTemp.containsKey(col2))
        {
            left = col1;
            right = col2;
        }
        else if(smallerTemp.containsKey(col2) && largerTemp.containsKey(col1))
        {
            left = col2;
            right = col1;
        }
        else
        {
            //invalid cols (not in any of the tables)
            System.out.println("hash");
            throw new Exception("Join on Invalid Columns.");
        }

        //now left has smallerTable join column and right has largerTable join column

        //map each record to a numbered bucket
        HashMap<Integer, ArrayList<Map<String, String>>> hashTable = new HashMap<>();
        int numberOfBuckets = 1000;

        //first pass: iterate through smallerTable and fill hash table
        for(Map<String, String> record: smallerTable)
        {
            String value = record.get(left);
            if(value != null)
            {
                //hashValue = bucket number
                int hashValue = Math.abs(value.hashCode()) % numberOfBuckets;

                if(!hashTable.containsKey(hashValue))
                    hashTable.put(hashValue, new ArrayList<Map<String, String>>());

                hashTable.get(hashValue).add(record);
            }
        }

        //second pass:  compare each record in largerTable to its hashed bucket
        for(Map<String, String> record1: largerTable)
        {
            String value = record1.get(right);
            if(value != null)
            {
                int hashValue = Math.abs(value.hashCode()) % numberOfBuckets;
                if(hashTable.containsKey(hashValue))
                {
                    //compare with every record w/the same hashValue
                    for(Map<String, String> record2: hashTable.get(hashValue))
                    {
                        String val1 = record1.get(right);
                        String val2 = record2.get(left);

                        if(val1.equals(val2))
                        {
                            Map<String,String> joinedRow = new LinkedHashMap<>();
                            joinedRow.putAll(record1);
                            joinedRow.putAll(record2);
                            result.add(joinedRow);
                        }
                    }
                }
            }
        }
        return result;
    }
}
