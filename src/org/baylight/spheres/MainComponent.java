package org.baylight.spheres;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

class MainComponent extends JPanel implements PrintHandler {

  private final ControlPanelComponent controlPanel;
  private final Draw3dComponent drawPanel;

  public MainComponent() {
    super(new BorderLayout());

    controlPanel = new ControlPanelComponent(this);
    controlPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    drawPanel = new Draw3dComponent(controlPanel);

    add(controlPanel, BorderLayout.PAGE_START);
    add(drawPanel, BorderLayout.CENTER);
  }

  @Override
  public void onPrint() {
    drawPanel.onPrint();
  }
}
