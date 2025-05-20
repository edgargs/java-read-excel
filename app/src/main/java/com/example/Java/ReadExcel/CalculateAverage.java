package com.example.Java.ReadExcel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
//import tech.tablesaw.io.csv.CsvReadOptions;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static tech.tablesaw.aggregate.AggregateFunctions.max;
import static tech.tablesaw.aggregate.AggregateFunctions.mean;
import static tech.tablesaw.aggregate.AggregateFunctions.min;
import static tech.tablesaw.aggregate.AggregateFunctions.median;
import static tech.tablesaw.aggregate.AggregateFunctions.count;
import static tech.tablesaw.api.ColumnType.FLOAT;
import static tech.tablesaw.api.ColumnType.STRING;
import static tech.tablesaw.api.ColumnType.LOCAL_DATE;

import tech.tablesaw.io.xlsx.XlsxReadOptions;
import tech.tablesaw.io.xlsx.XlsxReader;
import tech.tablesaw.joining.DataFrameJoiner;

public class CalculateAverage
{
    
    static ResourceBundle rd
            = ResourceBundle.getBundle("system");
    
    static private final String FILE = "/home/eriosn/Descargas/Reporte 17-05-2025.xlsx";

    public static void main(String[] args) throws Exception
    {
        URL measurementFile = CalculateAverage.class.getClassLoader()
                                    .getResource(FILE);

        XlsxReadOptions options = XlsxReadOptions
                .builder(FILE)
                .header(true)
                .build();

        var tables = new XlsxReader().readMultiple(options);
        Table reporteTiempoMediano = tables.stream()
                .filter( table -> table.name().endsWith("Reporte Tiempo Desarrollo") )
                .findFirst()
                .get();
        
        reporteTiempoMediano = reporteTiempoMediano
                .where(
                        reporteTiempoMediano.stringColumn("Periodo").isEqualTo("2025-Q2")
                );
        
        Table baseDeActivos = tables.stream()
                .filter( table -> table.name().endsWith("Base de Activos") )
                .findFirst()
                .get();
        
        Table measurements = new DataFrameJoiner(reporteTiempoMediano, "LT")
                .with(baseDeActivos)
                .rightJoinColumns("LT")
                .allowDuplicateColumnNames(true)
                .join();                
        
        measurements = measurements
                .where( measurements.stringColumn("CAL").isEqualTo("Checo Perez") );
        
        Gson gson = new Gson();
        TMDTemplate tmdTemplate = new TMDTemplate();
        tmdTemplate.CAL = "Checho Perez";        
                
        // TMD CAL
        var tmdCAL = measurements
                .summarize("Tiempo Desarrollo", min, mean, max, count, median)
                .by("CAL")
                .sortOn("CAL");
        
        tmdCAL.forEach(row -> {
                System.out.printf(
                        "%s=%2.1f / %2.1f / %2.1f / %2.1f / %2.1f \n",
                        row.getString("CAL"),
                        row.getDouble("Min [Tiempo Desarrollo]"),
                        row.getDouble("Mean [Tiempo Desarrollo]"),
                        row.getDouble("Max [Tiempo Desarrollo]"),
                        row.getDouble("Count [Tiempo Desarrollo]"),
                        row.getDouble("Median [Tiempo Desarrollo]")
                );
                tmdTemplate.TMD = row.getDouble("Median [Tiempo Desarrollo]");
            }
        );
        
        // TMD CL
        var tmdCL = measurements
                .summarize("Tiempo Desarrollo", min, mean, max, count, median)
                .by("CL")
                .sortOn("CL");
        
        List<CLTemplate> listCLs = new ArrayList<>();
        tmdCL.forEach(row -> {
                System.out.printf(
                        "%s=%2.1f / %2.1f / %2.1f / %2.1f / %2.1f \n",
                        row.getString("CL"),
                        row.getDouble("Min [Tiempo Desarrollo]"),
                        row.getDouble("Mean [Tiempo Desarrollo]"),
                        row.getDouble("Max [Tiempo Desarrollo]"),
                        row.getDouble("Count [Tiempo Desarrollo]"),
                        row.getDouble("Median [Tiempo Desarrollo]")
                );
                var clTemplate = new CLTemplate();
                clTemplate.CL = row.getString("CL");
                clTemplate.TMD = row.getDouble("Median [Tiempo Desarrollo]");
                listCLs.add(clTemplate);
                }
        );
        tmdTemplate.CLs = listCLs;
        
        //TMD CLxAPP
        var tmdCLxAPP = measurements
                .summarize("Tiempo Desarrollo", min, mean, max, count, median)
                .by("CL","MVP App")
                .sortOn("CL","MVP App");
        
        List<APPTemplate> listAPPs = new ArrayList<>();
        tmdCLxAPP.forEach(row -> {
                System.out.printf(
                        "%s=%2.1f / %2.1f / %2.1f / %2.1f / %2.1f \n",
                        row.getString("MVP App"),
                        row.getDouble("Min [Tiempo Desarrollo]"),
                        row.getDouble("Mean [Tiempo Desarrollo]"),
                        row.getDouble("Max [Tiempo Desarrollo]"),
                        row.getDouble("Count [Tiempo Desarrollo]"),
                        row.getDouble("Median [Tiempo Desarrollo]")
                );
                var appTemplate = new APPTemplate();
                appTemplate.CL = row.getString("CL");
                appTemplate.APP = row.getString("MVP App");
                appTemplate.TMD = row.getDouble("Median [Tiempo Desarrollo]");
                listAPPs.add(appTemplate);
                }
        );
        tmdTemplate.CLxAPPs = listAPPs;
        
        String calculate = gson.toJson(tmdTemplate);

        saveTableToSqlite(calculate);
    }

    static void saveTableToSqlite(String data)
    {
        String database = rd.getString("database");
        // SQLite connection string
        String url = "jdbc:sqlite:"+database;

        // SQL statement for creating a new table
        String sql = """
                CREATE TABLE IF NOT EXISTS TMDxCALxCLxAPP (
                 id integer PRIMARY KEY,
                 tmd TEXT NOT NULL
                );
                """;

        // try-with-resources statement will auto close the connection.
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
            stmt.executeUpdate("INSERT INTO TMDxCALxCLxAPP(tmd) VALUES('"+data+"')");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}