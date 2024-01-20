import View.View;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new View();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
/*
cerc: circular X10 Y9 I5 J10 counterclockwise

 */