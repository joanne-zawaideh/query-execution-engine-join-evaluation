public class Query
{
    private String query;
    private String[] selectCols;
    private String[] tables;
    //join[0] = left, join[1] = operator, join[2] = right
    private String[] join;
    //filter[0] = left, filter[1] = operator, filter[2] = right
    private String[] filter;


    Query(String query)
    {
        this.query = query;
        selectCols = tables = join = filter = null;
    }

    public String[] getFilter() { return filter; }
    public String[] getJoin() { return join; }
    public String[] getSelectCols() { return selectCols; }
    public String[] getTables() { return tables; }

    public void ParseMe() throws Exception
    {
        //store query
        query = query.replace("\n", " ").replace("\r", " ").trim(); //line endings in Windows = \r\n
        String upperQuery = query.toUpperCase().trim();

        //check select and from
        int selectIndex = upperQuery.indexOf("SELECT");
        int fromIndex = upperQuery.indexOf("FROM");
        int whereIndex = upperQuery.indexOf("WHERE");

        if (selectIndex != 0)
        {
            throw new Exception("Query must start with SELECT");
        }

        if (fromIndex == -1 || fromIndex < selectIndex)
        {
            throw new Exception("FROM is missing or in wrong place");
        }

        if(whereIndex != -1 && whereIndex < fromIndex)
        {
            throw new Exception("WHERE is in wrong place");
        }

        if(!query.endsWith(";"))
        {
            throw new Exception("Missing semicolon");
        }

        //remove ;
        query = query.substring(0, query.length() - 1);


        //split the query into parts
        //check cols exist
        if(query.substring(selectIndex + 6, fromIndex).trim().isEmpty())
        {
            throw new Exception("No columns in SELECT");
        }
        //assign select columns
        if(query.substring(selectIndex + 6, fromIndex).trim().equals("*"))
        {
            selectCols = new String[]{"*"};
            //note: selectCols is reassigned in ProjectMe() with the proper col values
        }
        else
        {
            selectCols = query.substring(selectIndex + 6, fromIndex).trim().split(",");
            for (int i = 0; i < selectCols.length; i++) {
                selectCols[i] = selectCols[i].trim();
            }
        }

        //check if tables in FROM exist
        if(whereIndex != -1)
        {
            if(query.substring(fromIndex + 4, whereIndex).trim().isEmpty())
            {
                throw new Exception("No tables in FROM");
            }
        }
        else
        {
            if(query.substring(fromIndex + 4).trim().isEmpty())
            {
                throw new Exception("No tables in FROM");
            }
        }

        //assign tables
        if(whereIndex != -1)
        {
            tables = query.substring(fromIndex + 4, whereIndex).trim().split(",");
        }
        else
        {
            tables = query.substring(fromIndex + 4).trim().split(",");
        }
        for(int i = 0; i < tables.length; i++)
        {
            tables[i] = tables[i].trim();
        }


        //check if conditions exist
        String wherePart = "";
        if(whereIndex != -1)
        {
            wherePart = query.substring(whereIndex + 5).trim();

            if(wherePart.contains("AND"))
            {
                //multiple conditions
                String[] conditions = wherePart.split("AND");
                for (int i = 0; i < conditions.length; i++)
                {
                    conditions[i] = conditions[i].trim();
                }

                for (String condition : conditions)
                {
                    String op = null;

                    if (condition.contains(">=")) op = ">=";
                    else if (condition.contains("<=")) op = "<=";
                    else if (condition.contains(">")) op = ">";
                    else if (condition.contains("<")) op = "<";
                    else if (condition.contains("=")) op = "=";
                    else throw new Exception("Invalid condition");

                    int opIndex = condition.indexOf(op);

                    String left = condition.substring(0, opIndex).trim();
                    String right = condition.substring(opIndex + 1).trim();

                    //join condition
                    if (left.contains(".") && right.contains("."))
                    {
                        join = new String[3];
                        join[0] = left;
                        join[1] = op;
                        join[2] = right;

                    }
                    //filter condition
                    else
                    {
                        filter = new String[3];
                        filter[0] = left;
                        filter[1] = op;
                        filter[2] = right;
                    }
                }

            }
            else
            {
                String op = null;
                if (wherePart.contains(">=")) op = ">=";
                else if (wherePart.contains("<=")) op = "<=";
                else if (wherePart.contains(">")) op = ">";
                else if (wherePart.contains("<")) op = "<";
                else if (wherePart.contains("=")) op = "=";
                else throw new Exception("Invalid condition");

                int opIndex = wherePart.indexOf(op);

                // only one condition
                if (wherePart.substring(0, opIndex).contains(".") && wherePart.substring(opIndex + 1).contains("."))
                {
                    join = new String[3];
                    join[0] = wherePart.substring(0, opIndex).trim();
                    join[1] = op;
                    join[2] = wherePart.substring(opIndex + 1).trim();
                    //no filter condition
                    filter = null;
                }
                else
                {
                    filter = new String[3];
                    filter[0] = wherePart.substring(0, opIndex).trim();
                    filter[1] = op;
                    filter[2] = wherePart.substring(opIndex + 1).trim();
                    //no join condition
                    join = null;
                }
            }
        }
    }

    public String toString()
    {
        String s = "";
        s += "Select Columns: ";
        for(String c: selectCols)
            s += c + ", ";
        s += "\n";

        s += "Tables: ";
        for(String t: tables)
            s += t + ", ";
        s += "\n";

        s += "Join Conditon: ";
        for(String j: join)
            s += j + " ";
        s += "\n";

        s += "Filter Condition: ";
        for(String f: filter)
            s += f + " ";
        s += "\n";

        return s;
    }
}
