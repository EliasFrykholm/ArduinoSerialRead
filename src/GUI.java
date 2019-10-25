import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.fazecast.jSerialComm.SerialPort;

public class GUI {

    SensorReader sensorReader;
    ExcelWriter writer;
    static int x = 0;

    public GUI() {

        final JFrame frame = new JFrame("JFileChooser Demo");
        final JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setDialogTitle("Select excel file to log data");
        fc.setCurrentDirectory(new File("./"));

        int retVal = fc.showOpenDialog(frame);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File selectedfile = fc.getSelectedFile();
            writer = new ExcelWriter(selectedfile);
        } else {
            writer = new ExcelWriter();
        }

        // create and configure the window
        JFrame window = new JFrame();
        window.setTitle("Sensor Graph GUI");
        window.setSize(600, 400);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a drop-down box and connect button, then place them at the top of the window
        JComboBox<String> portList = new JComboBox<String>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        topPanel.add(portList);
        topPanel.add(connectButton);
        window.add(topPanel, BorderLayout.NORTH);

        JPanel boxPanel = new JPanel();

        // populate the drop-down box
        SerialPort[] portNames = SerialPort.getCommPorts();
        for(int i = 0; i < portNames.length; i++)
            portList.addItem(portNames[i].getSystemPortName());

        // create the line graph
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart("Temp Sensor Readings", "Time (seconds)", "Temp reading", dataset);
        window.add(new CustomChartPanel(chart), BorderLayout.SOUTH);

        // configure the connect button and use another thread to listen for data
        connectButton.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent arg0) {
                if(connectButton.getText().equals("Connect")) {
                    try{
                        sensorReader = new SensorReader(SerialPort.getCommPort(portList.getSelectedItem().toString()));
                        connectButton.setText("Disconnect");
                        portList.setEnabled(false);
                        boxPanel.removeAll();
                        FutureTask<Integer> task = new FutureTask<>(() -> {
                            return sensorReader.read().size();
                        });
                        Thread initThread = new Thread(task);
                        initThread.setDaemon(true);
                        initThread.start();
                        Integer nrOfSensors = task.get(5, TimeUnit.SECONDS);
                        ArrayList<JCheckBox> checkBoxes = new ArrayList<>();
                        for(int i = 0; i<nrOfSensors; i++){
                            checkBoxes.add(new JCheckBox("Sensor nr: " + i));
                            boxPanel.add(checkBoxes.get(i));
                            dataset.addSeries(new XYSeries("Data" + i));
                            if(writer!=null) {
                                writer.addSensor("Sensor nr: " + i, i);
                            }
                        }
                        window.add(boxPanel, BorderLayout.CENTER);

                        window.repaint();

                        Thread thread = new Thread(() -> {
                            while(true){
                                ArrayList<Integer> sensorValues = sensorReader.read();
                                if(sensorValues == null){
                                    break;
                                }
                                for(int i = 0; i< nrOfSensors; i++){
                                    if(checkBoxes.get(i).isSelected()){
                                        dataset.getSeries(i).add(x, sensorValues.get(i));
                                    } else{
                                        dataset.getSeries(i).clear();
                                    }
                                }
                                x++;
                                window.repaint();
                                if(writer!=null) {
                                    writer.addSensorValues(sensorValues);
                                }
                            }});
                        thread.start();

                    } catch (DataFormatException e){
                        portList.setEnabled(true);
                        connectButton.setText("Connect");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        portList.setEnabled(true);
                        connectButton.setText("Connect");
                    }

                    // create a new thread that listens for incoming text and populates the graph

                } else {
                    // disconnect from the serial port
                    sensorReader.disconnect();
                    dataset.removeAllSeries();
                    window.remove(boxPanel);
                    portList.setEnabled(true);
                    connectButton.setText("Connect");
                    x = 0;
                    window.repaint();
                }
            }
        });

        // show the window
        window.setVisible(true);
    }

}