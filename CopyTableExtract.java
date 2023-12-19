import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Scanner;
import java.util.Timer;
 
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.*;
import java.io.IOException;

 
 
public class CopyTableExtract {

    public static void main(String[] args) throws ParseException {
        // Specify the folder containing the zip files
        String folderPath = "C:/Users/Windows/Downloads/New";
        // Specify the path for the Excel file
        String excelFilePath = "C:/Excel/SFDC_COPY_TABLE_COUNT.xlsx";
        // Specify the path for the CSV file
        String csvFilePath = "C:/Excel/SFDC_COPY_TABLE_COUNT.csv";

        // Call the method to unzip files, read logs, and create the Excel and CSV files
        unzipAndReadLogsAndCreateFiles(folderPath, excelFilePath, csvFilePath);
    }

    private static void unzipAndReadLogsAndCreateFiles(String folderPath, String excelFilePath, String csvFilePath)
            throws ParseException {
        File folder = new File(folderPath);
        File[] zipFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));

        if (zipFiles != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                // Create a new sheet
                Sheet sheet = workbook.createSheet("Sheet1");

                // Create headers
                Row headerRow = sheet.createRow(0);
                String[] headers = {"RUN_DATE", "RUN_CYCLE", "TABLE_NAME", "WEEKLY_DAILY", "RECORD_COUNT", "START_DATE", "END_DATE"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                int rowNum = 1;
                boolean isUserSecond = false;
                int SFDC_USER_COUNT = 0;
                int SFDC_USERRECORD_COUNT = 0;
                Date SFDC_USERrunDate = null;
                Date SFDC_USERstartDate = null;
                Date SFDC_USERENdDate = null;
                int runCycleCount = 0;
                int i = 0;
                int j = 0;
                for (File zipFile : zipFiles) {
                    String zipFileName = zipFile.getName();
                    String targetString = "";

                    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {

                        byte[] buffer = new byte[1024];
                        ZipEntry zipEntry = zis.getNextEntry();

                        while (zipEntry != null) {
                            String entryName = zipEntry.getName();
                            File newFile = new File(folder, entryName);

                            // Create directories if they do not exist
                            if (zipEntry.isDirectory()) {
                                newFile.mkdirs();
                            } else {
                                // Create parent directories for non-directory entries
                                new File(newFile.getParent()).mkdirs();

                                // Extract the file
                                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                    int len;
                                    while ((len = zis.read(buffer)) > 0) {
                                        fos.write(buffer, 0, len);
                                    }
                                }

                                // Check if the entry is a log file and does not contain "Truncate" in its name
                                if (entryName.toLowerCase().endsWith(".log") && !entryName.toLowerCase().contains("truncate") && !entryName.toLowerCase().contains("email_report") && !entryName.contains("TaccountInfo")) {
                                    String lastLine = null, line;
                                    String keyword = "successful";
                                    String weeklyDaily = null;

                                    String tableName = mapTableName(newFile.getName());
                                    String table = null;
                                    String targetString1 = null;

                                    if ("SFDC_USER".equals(tableName) && SFDC_USER_COUNT == 0) {
                                        SFDC_USER_COUNT++;
                                        SFDC_USERrunDate = extractDateFromLogFile(newFile);
                                        SFDC_USERstartDate = extractStartDate(newFile);
                                        try (BufferedReader bufferReader = new BufferedReader(new FileReader(newFile))) {
                                            if (bufferReader != null) {
                                                while ((line = bufferReader.readLine()) != null) {
                                                    lastLine = line;
                                                }
                                            }
                                        }
                                        SFDC_USERRECORD_COUNT = extractNumberBeforeSuccessful(lastLine, keyword);

                                    } else if ("SFDC_USER".equals(tableName) && SFDC_USER_COUNT == 1) {
                                        SFDC_USER_COUNT++;
                                        SFDC_USERENdDate = extractEndDate(newFile, sheet, rowNum);
                                        weeklyDaily = mapWeeklyDaily(newFile.getName());
                                        try (BufferedReader bufferReader = new BufferedReader(new FileReader(newFile))) {
                                            if (bufferReader != null) {
                                                while ((line = bufferReader.readLine()) != null) {
                                                    lastLine = line;
                                                }
                                            }
                                        }
                                        SFDC_USERRECORD_COUNT = SFDC_USERRECORD_COUNT + extractNumberBeforeSuccessful(lastLine, keyword);
                                        table = mapTableName(newFile.getName());
                                        targetString1 = mapTargetString(table);
                                        Date runDate = extractDateFromLogFile(newFile);
                                        Map<String, Integer> occurrences = countOccurrencesInZipFileNames(folderPath, targetString1);
                                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                                        String runTimeStr = timeFormat.format(runDate);

                                        if (entryName.contains("sfdcRegistrationMapExtractProcess")) {
                                            try {
                                                runCycleCount++;
                                                if (runCycleCount == 4) {
                                                    runCycleCount = 0;
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else if (!zipFileName.contains("Copy_Tables_extract_log_files") && !zipFileName.contains("trade_review_extract_log_files")) {
                                            runCycleCount = 1;
                                        } else if (entryName.contains("sfdcClientDisclosureExtractProcess")) {
                                            i++;
                                            runCycleCount = i;
                                        }

                                        addLogDataToExcel(sheet, rowNum++, SFDC_USERrunDate, tableName, SFDC_USERstartDate, SFDC_USERENdDate, weeklyDaily, SFDC_USERRECORD_COUNT, runCycleCount);

                                    } else {
                                        Date runDate = extractDateFromLogFile(newFile);
                                        Date startDate = extractStartDate(newFile);
                                        Date endDate = extractEndDate(newFile, sheet, rowNum);
                                        weeklyDaily = mapWeeklyDaily(newFile.getName());
                                        try (BufferedReader bufferReader = new BufferedReader(new FileReader(newFile))) {
                                            if (bufferReader != null) {
                                                while ((line = bufferReader.readLine()) != null) {
                                                    lastLine = line;
                                                }
                                            }
                                        }
                                        int recordCount = extractNumberBeforeSuccessful(lastLine, keyword);
                                        table = mapTableName(newFile.getName());
                                        targetString1 = mapTargetString(table);

                                        if (entryName.contains("sfdcRegistrationMapExtractProcess")) {
                                            try {
                                                if (runCycleCount == 0) {
                                                    runCycleCount = 3;
                                                } else {
                                                    if (runCycleCount == 3) {
                                                        runCycleCount = 0;
                                                    }
                                                    runCycleCount++;
                                                    if (runCycleCount == 4) {
                                                        runCycleCount = 0;
                                                    }
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else if (zipFileName.contains("EblotterExtract__log_files")) {
                                            j = 1;
                                            runCycleCount = 1;
                                        } else if (!zipFileName.contains("Copy_Tables_extract_log_files") && !zipFileName.contains("trade_review_extract_log_files")) {
                                            if (zipFileName.contains("sfdcEBlotter") && j == 1) {
                                                runCycleCount = 2;
                                            } else {
                                                runCycleCount = 1;
                                            }
                                        } else if (entryName.contains("sfdcClientDisclosureExtractProcess")) {
                                            if (i == 0) {
                                                i = 3;
                                            } else {
                                                if (i == 3) {
                                                    i = 0;
                                                }
                                                i++;
                                            }

                                            runCycleCount = i;
                                        }

                                        addLogDataToExcel(sheet, rowNum++, runDate, table, startDate, endDate, weeklyDaily, recordCount, runCycleCount);
                                    }
                                }
                            }
                            zipEntry = zis.getNextEntry();
                        }

                        System.out.println("Unzipped and processed logs: " + zipFile.getName());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Write the workbook to an Excel file
                try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                    workbook.write(fileOut);
                    System.out.println("Excel file created successfully at: " + excelFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Write the data to a CSV file
                try (PrintWriter writer = new PrintWriter(new File(csvFilePath))) {
                    StringBuilder csvStringBuilder = new StringBuilder();
                    csvStringBuilder.append(String.join(",", headers)).append("\n");
                    for (int r = 1; r < rowNum; r++) {
                        Row row = sheet.getRow(r);
                        for (int c = 0; c < headers.length; c++) {
                            Cell cell = row.getCell(c);
                            if (cell != null) {
                                csvStringBuilder.append(cellToString(cell)).append(",");
                            } else {
                                csvStringBuilder.append(",");
                            }
                        }
                        csvStringBuilder.deleteCharAt(csvStringBuilder.length() - 1);
                        csvStringBuilder.append("\n");
                    }
                    writer.write(csvStringBuilder.toString());
                    System.out.println("CSV file created successfully at: " + csvFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Delete files in the folder
                deleteFilesInFolder(folderPath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No zip files found in the specified folder.");
        }
    }
    private static String cellToString(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == CellType.BLANK) {
            return "";
        } else {
            return cell.toString();
        }
    }
    private static Date extractEndDate(File logFile,Sheet sheet,int rowNum){
        try(RandomAccessFile randomAccessFile=new RandomAccessFile(logFile,"r")){
            long length=randomAccessFile.length();
            if(length==0){
                return null;
            }
            long position=length-1;
            randomAccessFile.seek(position);
            while(position > 0 && randomAccessFile.readByte() !='\n'){
                position--;
                randomAccessFile.seek(position);
            }
            byte[] bytes=new byte[(int) (length-position)];
            randomAccessFile.read(bytes);
            String lastLine=new String(bytes).trim();
            try(BufferedReader bufferReader=new BufferedReader(new FileReader(logFile))){
            String last,line;
            if(bufferReader!=null){
                while((line=bufferReader.readLine()) !=null){
                    lastLine=line;
                }
            }
            }
            String keyword="successful";
            int recordCountCell=extractNumberBeforeSuccessful(lastLine,keyword);
 
 
            if(!lastLine.isEmpty()){
                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try{
                    Date endDate=dateFormat.parse(lastLine);
 
 
                    return endDate;
                }catch(ParseException e){
                    System.out.println("Error parsing end date from log file:"+logFile.getName());
                    e.printStackTrace();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
 
    }
    private static int extractNumberBeforeSuccessful(String lastLine, String keyword){
        String regex="(\\d+)\\s*successful";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(lastLine);
        if(matcher.find()){
            String numberStr=matcher.group(1);
            try{
                return Integer.parseInt(numberStr);
 
            }catch(NumberFormatException e){
                System.err.println("Error parsing number:"+numberStr);
                e.printStackTrace();
            }
        }
        return 0;
    }
    private static String mapTargetString(String table) {
        // Map the target string based on the provided conditions
        String targetString = "";
 
        if (table.equals("SFDC_W_TR_REGISTRATION_MAP") ||
                table.equals("SBR_W_REG_LETTER_LOG_REL_SFDC") ||
                table.equals("SFDC_W_SPONSOR_NAMES")) {
            targetString = "Copy_Tables_extract_log_files";
        } else if (table.equals("SFDC_W_CLIENT") ||
                table.equals("SFDC_W_REGISTRATION") ||
                table.equals("SFDC_W_REGISTRATION_MEMBERS") ||
                table.equals("SFDC_W_BENEFICIARY") ||
                table.equals("SFDC_W_CLIENT_DISCLOSURE") ||
                table.equals("SFDC_W_PORTFOLIO_REVIEW")) {
            targetString = "trade_review_extract_log_files";
        } else if (table.equals("SFDC_EBLOTTER")) {
            targetString = "EblotterExtract__log_files";
        } else if (table.equals("SBR_ACCOUNT_HISTORY_SFDC") ||
                table.equals("SBR_REG_MEMBER_HISTORY_SFDC") ||
                table.equals("SBR_REGISTRATION_HISTORY_SFDC") ||
                table.equals("SBR_REG_LETTER_LOG_SFDC") ||
                table.equals("SBR_REG_LETTER_LOG_T2_SFDC")) {
            targetString = "SFDC_History";
        } else if (table.equals("SFDC_CHECKS") || table.equals("SFDC_TRADES")) {
            targetString = "sfdcEBlotter";
        }else if(table.equals("SFDC_USER")){
            targetString="sfdc_emailload_log";
        }else if(table.equals("SFDC_W_FINANCIAL_ACCOUNT") || table.equals("SFDC_W_FINANCIAL_ACCOUNT_TEAM")){
            targetString="FA and FA Team Job";
        }
 
        return targetString;
    }
    private static Map<String, Integer> countOccurrencesInZipFileNames(String folderPath, String targetString) {
        File folder = new File(folderPath);
        File[] zipFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
 
        Map<String, Integer> occurrences = new HashMap<>();
        int count = 0;
 
        if (zipFiles != null) {
            for (File zipFile : zipFiles) {
                String zipFileName = zipFile.getName();
 
                if (zipFileName.contains(targetString)) {
                    count++;
                }
            }
        }
 
        occurrences.put(targetString, count);
        return occurrences;
    }
 
 
    private static int extractRecordCount(String lastLine){
        String[] parts=lastLine.split("\\D+");
        if(parts.length>0){
            return Integer.parseInt(parts[parts.length-1]);
        }else{
            return 0;
        }
    }
    private static void readLogFile(File logFile) {
        // Read and process the log file as needed
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line of the log file
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static Date extractDateFromLogFile(File logFile) {
        // Read and process the first line of the log file to extract date and time
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String firstLine = reader.readLine();
            if (firstLine != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               
                try {
                    return dateFormat.parse(firstLine);
                    //System.out.println(dateFormat.parse(firstLine));
                } catch (ParseException e) {
                    System.out.println("Error parsing date from log file: " + logFile.getName());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Date extractStartDate(File logFile) {
        // Read and process the lines of the log file to extract date and time from the line containing "Decrypting..."
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            boolean decryptingFound=false;
            while ((line = reader.readLine()) != null) {
                if (decryptingFound) {
 
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Adjust the format based on your log
                    try {
                        // Assuming the date and time start at index 13 and end at index 32
                        return dateFormat.parse(line.trim());
                    } catch (ParseException e) {
                        System.out.println("Error parsing start date from log file: " + logFile.getName());
                        e.printStackTrace();
                    }
                }
                if (line.contains("Decrypting...")){
                    decryptingFound=true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }private static String mapWeeklyDaily(String logFileName) {
        // Map "W" to WEEKLY_DAILY if the logfile name contains certain strings, otherwise map as "D"
        if (logFileName.toLowerCase().contains("sfdcfinancialaccountextractprocess") ||
                logFileName.toLowerCase().contains("sfdcfinancialaccountteamextractprocess")) {
            return "W";
        } else {
            return "D";
        }
    }
    private static String mapTableName(String fileName) {
        // Map the table name based on the file name
        if (fileName.toLowerCase().contains("sfdcregistrationmapextractprocess")) {
            return "SFDC_W_TR_REGISTRATION_MAP";
        }else if (fileName.contains("sfdcSbrLetterLogRelExtractProcess")) {
            return "SBR_W_REG_LETTER_LOG_REL_SFDC";
        }else if (fileName.contains("sfdcSponserNamesExtractProcess")) {
            return "SFDC_W_SPONSOR_NAMES";
        }else if (fileName.contains("sfdcClientExtractProcess")) {
            return "SFDC_W_CLIENT";
        }else if (fileName.contains("sfdcRegistrationExtractProcess")) {
            return "SFDC_W_REGISTRATION";
        }else if (fileName.contains("oracleEblotterExtractProcess")) {
            return "SFDC_EBLOTTER";
        }else if (fileName.contains("sfdcEBlottereBlotterExtractProcess")) {
            return "SFDC_EBLOTTER";
        }else if (fileName.contains("sfdcRegMemberExtractProcess")) {
            return "SFDC_W_REGISTRATION_MEMBERS";
        }else if (fileName.contains("sfdcRegBeneficiaryExtractProcess")) {
            return "SFDC_W_BENEFICIARY";
        }else if (fileName.contains("sfdcClientDisclosureExtractProcess")) {
            return "SFDC_W_CLIENT_DISCLOSURE";
        }else if (fileName.contains("sfdcPortfolioReviewExtractProcess")) {
            return "SFDC_W_PORTFOLIO_REVIEW";
        }else if (fileName.contains("SFDCHistoryAccountHistoryExtract")) {
            return "SBR_ACCOUNT_HISTORY_SFDC";
        }else if (fileName.contains("SFDCHistoryRegClientmemberHistoryExtract")) {
            return "SBR_REG_MEMBER_HISTORY_SFDC";
        }else if (fileName.contains("SFDCHistoryRegistrationHistoryExtract")) {
            return "SBR_REGISTRATION_HISTORY_SFDC";
        }else if (fileName.contains("SFDCHistoryRegistrationLogExtract")) {
            return "SBR_REG_LETTER_LOG_SFDC";
        }else if (fileName.contains("SFDCHistoryRegistrationLogtable_T2_Extract")) {
            return "SBR_REG_LETTER_LOG_T2_SFDC";
        }else if (fileName.contains("sfdcEBlotterChecksExtractProcess")) {
            return "SFDC_CHECKS";
        }else if (fileName.contains("sfdcEBlotterTradesExtractProcess")) {
            return "SFDC_TRADES";
        }else if (fileName.toLowerCase().contains("sfdc_emailload_log")) {
            return "SFDC_USER";
        }else if (fileName.contains("sfdcFinancialAccountExtractProcess")) {
            return "SFDC_W_FINANCIAL_ACCOUNT";
        }else if (fileName.contains("sfdcFinancialAccountTeamExtractProcess")) {
            return "SFDC_W_FINANCIAL_ACCOUNT_TEAM";
        }
 
        return "";
    }
 
    private static void addLogDataToExcel(Sheet sheet, int rowNum, Date runDate, String tableName,Date startDate,Date endDate,String weeklyDaily,int recordCount,int runCycleCount) throws ParseException {
        Row row = sheet.createRow(rowNum);
 
        // Add the extracted run date to the RUN_DATE column
        Cell cellRunDate = row.createCell(0);
        if (runDate != null) {
            SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat excelDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            cellRunDate.setCellValue(excelDateFormat.format(logDateFormat.parse(logDateFormat.format(runDate))));
        }
    
        Cell cellTableName = row.createCell(2);
        cellTableName.setCellValue(tableName);
        Cell cellStartDate=row.createCell(5);
        Cell cellWeeklyDaily=row.createCell(3);
        Cell setRecordCount=row.createCell(4);
        Cell setRunCycle=row.createCell(1);
        cellWeeklyDaily.setCellValue(weeklyDaily);
        if (startDate != null) {
            SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat excelDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            cellStartDate.setCellValue(excelDateFormat.format(logDateFormat.parse(logDateFormat.format(startDate))));
        }
        Cell cellEndDate=row.createCell(6);
        if (endDate != null) {
            SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat excelDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            cellEndDate.setCellValue(excelDateFormat.format(logDateFormat.parse(logDateFormat.format(endDate))));
        }
        setRecordCount.setCellValue(recordCount);
        if (setRunCycle != null) {
            setRunCycle.setCellValue(runCycleCount);
        }
    }
    /*private static void deleteFilesWithExtensions(String folderPath, String... extensions) {
        File folder = new File(folderPath);

        
        deleteFilesRecursive(folder, extensions);
    }
*/
    /*private static void deleteFilesRecursive(File folder, String... extensions) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    
                    deleteFilesRecursive(file, extensions);
                } else {
                    
                    for (String extension : extensions) {
                        if (file.getName().toLowerCase().endsWith(extension)) {
                            if (file.delete()) {
                                System.out.println("Deleted file: " + file.getAbsolutePath());
                            } else {
                                System.out.println("Failed to delete file: " + file.getAbsolutePath());
                            }
                            break;  
                        }
                    }
                }
            }
        }
    }*/

    private static void deleteFilesInFolder(String folderPath) throws IOException {
        // Use java.nio.file to delete all files in the folder
        Path folder = Paths.get(folderPath);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder)) {
            for (Path path : directoryStream) {
                Files.delete(path);
            }
            System.out.println("All files in the folder deleted successfully.");
        } catch (IOException e) {
            System.err.println("Error deleting files in the folder: " + e.getMessage());
            throw e;
        }
    }
 
}