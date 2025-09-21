package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.*;
import java.util.List;

public class Converter {
    
    /*
        
        Consider the following CSV data, a portion of a database of episodes of
        the classic "Star Trek" television series:
        
        "ProdNum","Title","Season","Episode","Stardate","OriginalAirdate","RemasteredAirdate"
        "6149-02","Where No Man Has Gone Before","1","01","1312.4 - 1313.8","9/22/1966","1/20/2007"
        "6149-03","The Corbomite Maneuver","1","02","1512.2 - 1514.1","11/10/1966","12/9/2006"
        
        (For brevity, only the header row plus the first two episodes are shown
        in this sample.)
    
        The corresponding JSON data would be similar to the following; tabs and
        other whitespace have been added for clarity.  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings and which values should be encoded as integers, as
        well as the overall structure of the data:
        
        {
            "ProdNums": [
                "6149-02",
                "6149-03"
            ],
            "ColHeadings": [
                "ProdNum",
                "Title",
                "Season",
                "Episode",
                "Stardate",
                "OriginalAirdate",
                "RemasteredAirdate"
            ],
            "Data": [
                [
                    "Where No Man Has Gone Before",
                    1,
                    1,
                    "1312.4 - 1313.8",
                    "9/22/1966",
                    "1/20/2007"
                ],
                [
                    "The Corbomite Maneuver",
                    1,
                    2,
                    "1512.2 - 1514.1",
                    "11/10/1966",
                    "12/9/2006"
                ]
            ]
        }
        
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
        
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including examples.
        
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String result = "{}"; // default return value; replace later!
        
        try {
            // Reading csv rows // added a try-with-resources because it was suggested when I initially didn't have a try-with-resources
            try (CSVReader reader = new CSVReader(new java.io.StringReader(csvString))) {
                List<String[]> csvData = reader.readAll(); // Reads all rows, each array is a row, where the first row is the column headers
            
                String[] header = csvData.get(0); // Gets the headers for each column
            
                JsonArray colHeadings = new JsonArray(); // Holds the headers
                JsonArray prodNums = new JsonArray(); // holds the prodNums
                JsonArray data = new JsonArray(); // holds the rest of the data rows
            
                colHeadings.addAll(java.util.Arrays.asList(header)); // adds all the headers
            
                for (int i = 1; i < csvData.size(); i++) {
                    String[] row = csvData.get(i);
                    
                    prodNums.add(row[0]); // Adds the ProdNum column to the array
            
                    JsonArray dataRow = new JsonArray();
            
                    for (int j = 1; j < row.length; j++) {
                        String cell = row[j];
                
                        if (j == 2 || j == 3) { // parsing the season and episode columns that are in index 1 and 2 as integers
                            try {
                                int intValue = Integer.parseInt(cell);
                                dataRow.add(intValue);
                            }
                            catch (NumberFormatException e) {
                                dataRow.add(cell); // Adds the data as a string if it isn't a parseable int
                            }
                        } else {
                            dataRow.add(cell); // Adds the rest of the remaining columns as strings
                        }
                    }
                
                    data.add(dataRow); // Adds the row data array to the data JsonArray
                
                }
            
                // Creating a json object with the three keys for prodnums, headings, and the rest of the data
                JsonObject json = new JsonObject();
                json.put("ProdNums", prodNums);
                json.put("ColHeadings", colHeadings);
                json.put("Data", data);
            
                result = Jsoner.serialize(json); // Serializing the json object to a string
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {
        
        String result = ""; // default return value; replace later!
        
        try {
            // Parsing a json string into a json object and getting the prodnums, headings, and data arrays
            JsonObject json = (JsonObject) Jsoner.deserialize(jsonString);
            JsonArray colHeadings = (JsonArray) json.get("ColHeadings");
            JsonArray prodNums = (JsonArray) json.get("ProdNums");
            JsonArray data = (JsonArray) json.get("Data");
            
            // Using StringWriter and CSVWriter to build the CSV output
            StringWriter sw = new StringWriter();
            CSVWriter writer = new CSVWriter(sw);
            
            // Creating the header row and getting the headings
            String[] headerRow = new String[colHeadings.size()];
            // Getting each heading
            for (int i = 0; i < colHeadings.size(); i++) {
                headerRow[i] = colHeadings.getString(i);
            }
            // Writing the header rows
            writer.writeNext(headerRow);
            
            // Going over each row of data
            for (int i = 0; i < data.size(); i++) {
                JsonArray rowData = (JsonArray) data.get(i);
                String[] row = new String[rowData.size() + 1]; // Skipping index 0 because PodNum is stored differently
                
                row[0] = prodNums.get(i).toString(); // Adding ProdNum to the start of the row
                
                // Going through each cell in the data row and converting that data to a string for the CSV output
                for (int j = 0; j < rowData.size(); j++) {
                    Object cell = rowData.get(j);
                    String value = "";
                    
                    if (cell == null) {
                        value = "";
                    } else if (cell instanceof String){
                        value = (String) cell;
                    } else if (cell instanceof Number) {
                        value = cell.toString();
                    }
                    
                    // Special for episode number
                    if (j == 2) {
                        try {
                            int episodeNumber = Integer.parseInt(value);
                            value = String.format("%02d", episodeNumber); // Adds padding for episodes that start with a leading zero
                        }
                        catch (NumberFormatException e){
                            // Ignores if it doesn't need to have a leading zero
                        }
                    }
                
                    // Stores the values in the correct spot excluding the 0 index since that is ProdNum
                    row[j + 1] = value;
                }
                
                // Writes the complete row for CSV
                writer.writeNext(row);
            }
            
            // Closes the writer and gets the full CSV output
            writer.close();
            result = sw.toString();
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
}
