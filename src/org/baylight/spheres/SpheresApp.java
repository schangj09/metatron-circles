package org.baylight.spheres;

import java.awt.*;
import javax.swing.*;

class SpheresApp {

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame = new JFrame("SpheresApp");
    frame.setSize(250, 250);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Container pane = frame.getContentPane();

    //Add the label.
    JLabel label = new JLabel("Start drawing");
    pane.add(label);

    MainComponent mainComponent = new MainComponent();
    pane.add(mainComponent, BorderLayout.CENTER);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
}
