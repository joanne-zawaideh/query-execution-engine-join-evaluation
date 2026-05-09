public class Query {
    private String query;
    private String[] selectCols;
    private String[] tables;
    private String[] joinCols;
    //filter[0] = left, filter[1] = operator, filter[2] = right
    private String[] filter;
    Query(String query) {
        this.query = query;
        selectCols = tables = joinCols = filter = null;
    }
    public void ParseMe() throws Exception {
        //store query
        query = query.replace("\n", " ").replace("\r", " ").trim();
        String upperQuery = query.toUpperCase().trim();
        //check select and from
        int selectIndex = upperQuery.indexOf("SELECT");
        int fromIndex = upperQuery.indexOf("FROM");
        int whereIndex = upperQuery.indexOf("WHERE");
        if (selectIndex != 0) {
            throw new Exception("Query must start with SELECT");
        }
        if (fromIndex == -1 || fromIndex < selectIndex) {
            throw new Exception("FROM is missing or in wrong place");
        }
        if(whereIndex != -1 && whereIndex < fromIndex) {
            throw new Exception("WHERE is in wrong place");
        }
        if(!query.endsWith(";")) {
            throw new Exception("Missing semicolon");
        }
        //remove ;
        query = query.substring(0, query.length() - 1);

        //split the query into parts
        //check cols exist
        if(query.substring(selectIndex + 6, fromIndex).trim().isEmpty()) {
            throw new Exception("No columns in SELECT");
        }
        //assign select columns
        selectCols = query.substring(selectIndex + 6, fromIndex).trim().split(",");
        for(int i = 0; i < selectCols.length; i++) {
            selectCols[i] = selectCols[i].trim();
        }
        //check tables exist
        if(whereIndex != -1) {
            if(query.substring(fromIndex + 4, whereIndex).trim().isEmpty()) {
                throw new Exception("No tables in FROM");
            }
        } else {
            if(query.substring(fromIndex + 4).trim().isEmpty()) {
                throw new Exception("No tables in FROM");
            }
        }
        //assign tables
        if(whereIndex != -1) {
            tables = query.substring(fromIndex + 4, whereIndex).trim().split(",");
        } else {
            tables = query.substring(fromIndex + 4).trim().split(",");
        }
        for(int i = 0; i < tables.length; i++) {
            tables[i] = tables[i].trim();
        }
        //check if conditions exist
        String wherePart = "";
        if(whereIndex != -1) {
            wherePart = query.substring(whereIndex + 5).trim();

            if(wherePart.contains("AND")) {
                //multiple conditions
                String[] conditions = wherePart.split("AND");
                for (int i = 0; i < conditions.length; i++) {
                    conditions[i] = conditions[i].trim();
                }

                for (String condition : conditions) {
                    String op = null;

                    if (condition.contains(">=")) op = ">=";
                    else if (condition.contains("<=")) op = "<=";
                    else if (condition.contains(">")) op = ">";
                    else if (condition.contains("<")) op = "<";
                    else if (condition.contains("=")) op = "=";
                    else throw new Exception("Invalid condition");

                    if (op.equals("=")) {
                        // CHANGED eqIndex TO opIndex
                        int opIndex = condition.indexOf("=");

                        String left = condition.substring(0, opIndex).trim();
                        String right = condition.substring(opIndex + 1).trim();

                        //join condition
                        if (left.contains(".") && right.contains(".")) {
                            joinCols = new String[2];
                            joinCols[0] = left;
                            joinCols[1] = right;
                        }
                        //filter condition
                        else {
                            filter = new String[3];
                            filter[0] = left;
                            filter[1] = "=";
                            filter[2] = right;
                        }
                    }
                    //no equality; it's a filter
                    else {
                        filter = new String[3];
                        filter[0] = condition.substring(0, condition.indexOf(op)).trim();
                        filter[1] = op;
                        filter[2] = condition.substring(condition.indexOf(op) + op.length()).trim();
                    }
                }

            }
            else {
                String op = null;

                if (wherePart.contains(">=")) op = ">=";
                else if (wherePart.contains("<=")) op = "<=";
                else if (wherePart.contains(">")) op = ">";
                else if (wherePart.contains("<")) op = "<";
                else if (wherePart.contains("=")) op = "=";
                else throw new Exception("Invalid condition");

                // only one condition
                if (op.equals("=")) {
                    if (wherePart.substring(0, wherePart.indexOf("=")).contains(".") && wherePart.substring(wherePart.indexOf("=") + 1).contains(".")) {
                        joinCols = wherePart.split("=");
                        for (int i = 0; i < joinCols.length; i++) {
                            joinCols[i] = joinCols[i].trim();
                        }
                        //no filter condition
                        filter = null;
                    }
                    else {
                        filter = new String[3];
                        filter[0] = wherePart.substring(0, wherePart.indexOf("=")).trim();
                        filter[1] = "=";
                        filter[2] = wherePart.substring(wherePart.indexOf("=") + 1).trim();
                        //no join condition
                        joinCols = null;
                    }
                }
                else {
                    filter=new String[3];
                    filter[0]= wherePart.substring(0,wherePart.indexOf(op)).trim();
                    filter[1]= op;
                    filter[2]= wherePart.substring(wherePart.indexOf(op)+op.length()).trim();
                    //no join condition
                    joinCols = null;
                }
            }
        }

        return false;
    }
}
