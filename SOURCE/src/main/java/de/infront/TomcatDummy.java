package de.infront;

import java.awt.*;
import java.awt.event.*;


/**
 *
 *   ServerProcessCtrl.config soll DAS starten
 *
 */
public class TomcatDummy {

    public static void main(String[] args) {
        Frame frame = new Frame("Simple AWT App"); // Create a new frame with a title
        frame.setSize(300, 200); // Set the size of the frame

        // Create a label and add it to the frame
        Label label = new Label("Tomcat Dummy", Label.CENTER);
        frame.add(label);

        // Handle window closing event
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setSize(300, 50);
        frame.setLocation(10, 10);

        frame.setVisible(true); // Make the frame visible
    }
}
