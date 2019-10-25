import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Frykiz on 2019-10-25.
 */
public class ExcelWriter {

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    FileInputStream fis;
    FileOutputStream fos;
    File file;
    private Workbook wb;
    Sheet sh;

    public ExcelWriter(File file){
        try {
            this.file = file;
            fis = new FileInputStream(file);
            wb = WorkbookFactory.create(fis);
            fis.close();
            sh = wb.getSheetAt(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ExcelWriter() {
            file = new File("tempLog.xlsx");
            wb = new XSSFWorkbook();
            sh = wb.createSheet("SensorData");


    }

    public synchronized void addSensor(String name, int pos){
        if(sh!= null) {
            if(sh.getRow(0) == null){
                sh.createRow(0);
            }
            Row row = sh.getRow(0);
            if(row.getCell(pos+1) == null) {
                Cell cell = row.createCell(pos + 1);
                cell.setCellValue(name);
            }
        }
    }

    public synchronized void addSensorValues(ArrayList<Integer> values){
        if(sh!=null) {
            Row row = sh.createRow(sh.getLastRowNum() + 1);
            Date date = new Date();
            row.createCell(0).setCellValue(formatter.format(date));
            int i = 1;
            for (Integer val : values) {
                row.createCell(i).setCellValue(val);
                i++;
            }
            write();
        }
    }

    private synchronized void write(){
        try {
            fos = new FileOutputStream(file);
            wb.write(fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
