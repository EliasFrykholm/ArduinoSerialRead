import com.fazecast.jSerialComm.SerialPort;
import org.jfree.data.xy.XYSeriesCollection;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.DataFormatException;

/**
 * Created by Frykiz on 2019-10-24.
 */
public class SensorReader {

    SerialPort port;
    ArrayList<Integer> latestReading;

    public SensorReader(SerialPort port) throws DataFormatException{
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        if(!port.openPort()){
            port.closePort();
            throw new DataFormatException("Port Closed");
        }
        this.port = port;
    }

    public ArrayList<Integer> read(){
        Scanner scanner = new Scanner(port.getInputStream());
        latestReading = new ArrayList<>();
        if(scanner.hasNextLine()) {
            try {
                String line = scanner.nextLine();
                String[] split = line.split(",");
                for (String s : split){
                    latestReading.add(Integer.parseInt(s));
                }
            } catch(Exception e) {}
        } else {
            scanner.close();
            return null;
        }
        return latestReading;
    }

    public void disconnect(){
        port.closePort();
    }

    public synchronized ArrayList<Integer> getLatestReading() {
        return latestReading;
    }
}
