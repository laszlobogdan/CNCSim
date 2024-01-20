package View;
import Business_Logic.CNC;

import java.awt.*;
import javax.swing.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class View extends JFrame {
    Pattern patternMove = Pattern.compile("(move) X(-?\\d+\\.?\\d*) Y(-?\\d+\\.?\\d*)?( ?)");
    Pattern patternLinear = Pattern.compile("(liniar) X(-?\\d+\\.?\\d*) Y(-?\\d+\\.?\\d*)?( ?)");
    Pattern patternCircular = Pattern.compile("(circular) X(-?\\d+\\.?\\d*) Y(-?\\d+\\.?\\d*) I(-?\\d+\\.?\\d*) J(-?\\d+\\.?\\d*) (clockwise|counterclockwise)?( ?)");

    public View() throws IOException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLayout(new BorderLayout(10, 10));

        JPanel panel1 = new JPanel();
        CNC cnc = new CNC();

        cnc.setBackground(Color.WHITE);

        JButton reset = new JButton("Reset");
        JButton send = new JButton("Send");
        JButton pos=new JButton("Get Position");
        JTextField jTextField = new JTextField(25);
        JLabel currentPos=new JLabel("Current Position: X"+cnc.getCurrentX()+" Y"+cnc.getCurrentY());
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("comm.txt", true));
        send.addActionListener(e -> {
            String input = jTextField.getText();
            Matcher matMove = patternMove.matcher(input);
            Matcher matLinear = patternLinear.matcher(input);
            Matcher matCircular = patternCircular.matcher(input);
            if (matMove.matches()) {
                try {
                    bufferedWriter.write("G00 X"+matMove.group(2)+" Y"+matMove.group(3));
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                cnc.setXGoal(Float.parseFloat(matMove.group(2)));
                cnc.setYGoal(Float.parseFloat(matMove.group(3)));
                cnc.setType(0); // Move command
            } else if (matLinear.matches()) {
                try {
                    bufferedWriter.write("G01 X"+matLinear.group(2)+" Y"+matLinear.group(3));

                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                cnc.setXGoal(Float.parseFloat(matLinear.group(2)));
                cnc.setYGoal(Float.parseFloat(matLinear.group(3)));

                cnc.setType(1); // Linear cut
            } else if (matCircular.matches()) {
                try {
                    if (matCircular.group(6).equals("clockwise")) {
                        bufferedWriter.write("G02 X"+matCircular.group(2)+" Y"+matCircular.group(3)+" I"+matCircular.group(4)+" J"+matCircular.group(5));
                    }else{
                        bufferedWriter.write("G03 X"+matCircular.group(2)+" Y"+matCircular.group(3)+" I"+matCircular.group(4)+" J"+matCircular.group(5));
                    }

                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                cnc.setXGoal(Float.parseFloat(matCircular.group(2)));
                cnc.setYGoal(Float.parseFloat(matCircular.group(3)));
                if (matCircular.group(6).equals("clockwise")) {
                    cnc.setType(2); // clockwise Circ cut
                } else {
                    cnc.setType(3); // counterClockwise Circ cut
                }
                cnc.setXCenter(Float.parseFloat(matCircular.group(4)) + cnc.getCurrentX());
                cnc.setYCenter(Float.parseFloat(matCircular.group(5)) + cnc.getCurrentY());
            } else {
                JOptionPane.showMessageDialog(cnc, "Invalid command format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new Thread(cnc::startCuttingAnimation).start();
            });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reset.addActionListener(e -> cnc.reset());
        pos.addActionListener(e -> currentPos.setText("Current Position: X"+cnc.getCurrentX()+" Y"+cnc.getCurrentY()));
        panel1.add(jTextField);
        panel1.add(reset);
        panel1.add(send);
        panel1.add(pos);
        panel1.add(currentPos);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, cnc);
        splitPane.setDividerLocation(getWidth() / (7 / 2));

        add(splitPane);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}


