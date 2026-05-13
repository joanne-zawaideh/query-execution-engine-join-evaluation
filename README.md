# query-execution-engine-join-evaluation
a mini query execution engine that simulates how a database system processes queries internally using Java. It supports queries like "SELECT A.x, B.y FROM A, B WHERE A.id = B.id AND A.value > 50;".

Note: This is a group project for the DBMS course at PSUT.


*Implementation Details*

Query class:

ParseMe() -> checks syntax and keywords, and assigns the data members of the Query object.

ScanOperator class:

ScanMe(String table) -> retrieves a table’s records, stores them in an ArrayList<Map<String, String>> and returns it.
  - LinkedHashMap: used to store the record’s fields and values and keep their order
    
	ex. In CSV file: 1, joanne, joanne@gmail.com

    In map: { “id” -> 1, “name” -> “joanne”, “email” -> “joanne@gmail.com” }
  - BufferedReader: read files (usuallu big) by reading blocks into memory instead of reading one char at a time. 
  - FileReader: opens the file


SelectOperator class:

SelectMe(...) -> filters the records returned by ScanMe() 
  - Map.Entry<String, String>: each pair<String, String> in the map (field, value)

	
Main class:
  - mapName.entrySet(): all pairs in the map { (field1, val1), (field2, val2), … }

JoinAlgorithm class:

MeNestedLoop() ->
	- map1.putAll(map2): copies the entries in map2 into map1
