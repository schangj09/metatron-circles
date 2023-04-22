package org.baylight.spheres;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ControlPanelComponent extends JPanel implements ItemListener, ActionListener {

  private final JPanel controlPanel;
  private final JCheckBox colorCheckbox;
  private final JCheckBox circlesCheckbox;
  private final JButton printButton;
  private final PrintHandler printHandler;
  char colorSelection = 'B';
  boolean showCircles = Boolean.TRUE;
  int imageCount = 1;

  public ControlPanelComponent(final PrintHandler printHandler) {
    super(new FlowLayout());

    this.printHandler = printHandler;

    //Create the check boxes.
    colorCheckbox = new JCheckBox("Color");
    colorCheckbox.setMnemonic(KeyEvent.VK_C);
    colorCheckbox.setSelected(colorSelection == 'B');
    colorCheckbox.addItemListener(this);
    circlesCheckbox = new JCheckBox("Circles");
    circlesCheckbox.setMnemonic(KeyEvent.VK_K);
    circlesCheckbox.setSelected(showCircles);
    circlesCheckbox.addItemListener(this);

    JLabel countLabel = new JLabel("Number of images", JLabel.LEADING);
    JComboBox<String> countList = new JComboBox<String>(new String[]{"1", "2", "4"});
    countList.addActionListener(this);

    printButton = new JButton("Print");
    printButton.setMnemonic(KeyEvent.VK_P);
    printButton.addActionListener(this);

    controlPanel = new JPanel(new GridLayout(1, 0));
    controlPanel.add(countLabel);
    controlPanel.add(countList);
    controlPanel.add(colorCheckbox);
    controlPanel.add(circlesCheckbox);
    controlPanel.add(printButton);
    //setControlPanelColors();

    add(controlPanel);
  }

  /**
   * Listens to the check boxes.
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    Object source = e.getItemSelectable();

    if (source == colorCheckbox) {
      colorSelection = (e.getStateChange() == ItemEvent.DESELECTED)
          ? 'W' : 'B';
      //setControlPanelColors();
    } else if (source == circlesCheckbox) {
      showCircles = (e.getStateChange() == ItemEvent.SELECTED);
    }
    getParent().repaint();
  }

  /**
   * Listens to the drop down combo or print button
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == printButton) {
      printHandler.onPrint();
    } else {
      JComboBox cb = (JComboBox) source;
      String newSelection = (String) cb.getSelectedItem();
      try {
        imageCount = Integer.parseInt(newSelection);
      } catch (NumberFormatException exc) {
        System.out.println("Error parsing selection" + exc);
      }
      getParent().repaint();
    }
  }

  protected void setControlPanelColors() {
    Color backgroundColor = (colorSelection == 'B') ? Color.BLACK : Color.WHITE;
    Color foregroundColor = (colorSelection == 'B') ? Color.WHITE : Color.BLACK;
    controlPanel.setBackground(backgroundColor);
    controlPanel.setForeground(foregroundColor);
    for (int i = 0; i < controlPanel.getComponentCount(); i++) {
      controlPanel.getComponent(i).setBackground(backgroundColor);
      controlPanel.getComponent(i).setForeground(foregroundColor);
    }
  }

}
