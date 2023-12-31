package org.baylight.circles;

import java.awt.*;
import javax.swing.*;

/**
 * The main App class for running the swing program. It invokes the Swing event
 * dispatcher thread with the Frame and MetaComponent.
 *
 * @author Jeffrey Schang
 */
class CirclesApp {

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame = new JFrame("CirclesApp");
    frame.setSize(250, 250);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Container pane = frame.getContentPane();

    //Add the label.
    //JLabel label = new JLabel("Start drawing");
    //pane.add(label);
    MetaComponent component = new MetaComponent();
    pane.add(component, BorderLayout.CENTER);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Main method to start up the swing UI.
   *
   * @param args arguments from the command line
   */
  public static void main(String[] args) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(() -> {
      createAndShowGUI();
    });
  }
}
