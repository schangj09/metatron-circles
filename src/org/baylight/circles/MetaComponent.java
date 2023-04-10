package org.baylight.circles;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;

import static java.lang.Math.*;


class MetaComponent extends JPanel implements ItemListener, ActionListener, Printable {
  int sizex = 1000, sizey = 1000;
  JPanel controlPanel;
  JCheckBox colorCheckbox;
  JCheckBox circlesCheckbox;
  JButton printButton;
  char colorSelection = 'B';
  boolean showCircles = Boolean.TRUE;
  int imageCount = 1;

  public MetaComponent() {
    super(new FlowLayout());

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
    JComboBox<String> countList = new JComboBox<String>(new String[] {"1", "2", "4"});
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
    setControlPanelColors();

    add(controlPanel);
    //add(pictureLabel, BorderLayout.CENTER);
    setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
  }

  public Dimension getPreferredSize() {
      return new Dimension(sizex,sizey);
  }

  /** Listens to the check boxes. */
  public void itemStateChanged(ItemEvent e) {
    int index = 0;
    char c = '-';
    Object source = e.getItemSelectable();

    if (source == colorCheckbox) {
      colorSelection = (e.getStateChange() == ItemEvent.DESELECTED)
        ? 'W' : 'B';
      setControlPanelColors();
    } else if (source == circlesCheckbox) {
      showCircles = (e.getStateChange() == ItemEvent.SELECTED);
    }
    repaint();
  }

  /** Listens to the drop down combo or print button */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == printButton) {
      onPrint();
    } else {
      JComboBox cb = (JComboBox)source;
      String newSelection = (String)cb.getSelectedItem();
      try {
        imageCount = Integer.parseInt(newSelection);
      } catch (NumberFormatException exc) {
        System.out.println("Error parsing selection" + exc);
      }
      repaint();
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

  protected void onPrint() {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(this);
    boolean doPrint = job.printDialog();
    if (doPrint) {
      try {
        job.print();
      } catch (PrinterException e) {
        System.err.println("The print job did not successfully complete.");
        e.printStackTrace();
      }
    }
  }

  public int print(Graphics g, PageFormat pf, int page)
        throws PrinterException {

    // We have only one page, and 'page'
    // is zero-based
    if (page > 0) {
         return NO_SUCH_PAGE;
    }

    // User (0,0) is typically outside the
    // imageable area, so we must translate
    // by the X and Y values in the PageFormat
    // to avoid clipping.
    Graphics2D g2d = (Graphics2D)g;
    g2d.translate(pf.getImageableX(), pf.getImageableY());

    // Stroke dashedStroke = new BasicStroke(
    //   0.1f,
    //   BasicStroke.CAP_ROUND,
    //   BasicStroke.JOIN_MITER,
    //   1.0f,
    //   new float[] {2.0f, 4.0f},
    //   0.0f
    // );
    Stroke thinStroke = new BasicStroke(
      0.4f
    );
    g2d.setStroke(thinStroke);

    // Image is appoximately 5 times the diameter, so choose
    // diameter to be min(pageWidth, pageHight)/5
    double pageWidth = pf.getImageableWidth();
    double pageHeight = pf.getImageableHeight();
    double diameter = min(pageWidth, pageHeight)/5;
    DrawingContext drawing = new DrawingContext(g2d, diameter, showCircles);
    Point2D centerPt = new Point2D.Double(pageWidth/2, pageHeight/2);
    double rotation = PI / 6.0;
    double angleIncrement = toRadians(15);
    int indexIncrement = getIndexIncrement();
    for (int i = 0; i < 4; i += indexIncrement) {
      drawing.drawMain(centerPt, rotation);
      rotation += angleIncrement*indexIncrement;
    }

    // tell the caller that this page is part
    // of the printed document
    return PAGE_EXISTS;
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);       

    Graphics2D g2 = (Graphics2D) g;
    setBackground(colorSelection == 'B' ? Color.BLACK : Color.WHITE);

    // draw Ellipse2D.Double
    double diam = 160;
    Dimension size = getSize();
    Point2D centerPt = new Point2D.Double(size.getWidth()/2, size.getHeight()/2);

    Color[] blackColors = new Color[] {
      Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN
    };
    Color[] whiteColors = new Color[] {
      Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK
    };
    Color[] colors = colorSelection == 'B' ? blackColors : whiteColors;

    DrawingContext drawing = new DrawingContext(g2, diam, showCircles);
    double rotation = PI / 6.0;
    double angleIncrement = toRadians(15);
    int indexIncrement = getIndexIncrement();
    for (int i = 0; i < 4; i += indexIncrement) {
      drawing.setColor(colors[i]);
      drawing.drawMain(centerPt, rotation);
      rotation += angleIncrement*indexIncrement;
    }
  }

  protected int getIndexIncrement() {
    if (imageCount == 1) {
      return 4;
    } else if (imageCount == 2) {
      return 2;
    } else {
      return 1;
    }
  }

  /**
   * Inner class to hold the basic information for drawing the circles.
   * Graphics context.
   * Diameter of the circles.
   * Angle between each circle (30 degrees).
  **/
  protected class DrawingContext {
    final Graphics2D g2;
    final double diam;
    final boolean showCircles;
    final double deg30 = PI / 3.0;
    DrawingContext(Graphics2D g, double diameter, boolean showCircles) {
      g2 = g;
      diam = diameter;
      this.showCircles = showCircles;
    }

    public void setColor(Color color) {
      g2.setColor(color);
    }

    public void drawMain(Point2D centerPt, double rotation) {
      drawCircle(centerPt);
      double dist = diam;
      Point2D[] innerPts = draw6(centerPt, dist, rotation);
      Point2D[] outerPts = draw6(centerPt, dist*2, rotation);

      connectInnerOuter(0, innerPts, outerPts);
      connectInnerOuter(1, innerPts, outerPts);
      connectInnerOuter(2, innerPts, outerPts);
      connectInnerOuter(3, innerPts, outerPts);
      connectInnerOuter(4, innerPts, outerPts);
      connectInnerOuter(5, innerPts, outerPts);
    }

    protected void connectInnerOuter(int index, Point2D[] innerPts, Point2D[] outerPts) {
      drawLine(outerPts[index], innerPts[(index + 2)%6]);
      drawLine(outerPts[index], innerPts[(index + 4)%6]);
    }

    protected Point2D[] draw6(Point2D center, double dist, double rotation) {
      double angle = rotation;
      Point2D pt1 = arcPoint(center, dist, angle);
      drawCircle(pt1);
      angle += deg30;
      Point2D pt2 = arcPoint(center, dist, angle);
      drawCircle(pt2);
      angle += deg30;
      Point2D pt3 = arcPoint(center, dist, angle);
      drawCircle(pt3);
      angle += deg30;
      Point2D pt4 = arcPoint(center, dist, rotation + PI);
      drawCircle(pt4);
      angle += deg30;
      Point2D pt5 = arcPoint(center, dist, angle);
      drawCircle(pt5);
      angle += deg30;
      Point2D pt6 = arcPoint(center, dist, angle);
      drawCircle(pt6);

      drawLine(pt1, pt2);
      drawLine(pt1, pt3);
      drawLine(pt1, pt4);
      drawLine(pt1, pt5);
      drawLine(pt1, pt6);

      drawLine(pt2, pt3);
      drawLine(pt2, pt4);
      drawLine(pt2, pt5);
      drawLine(pt2, pt6);

      drawLine(pt3, pt4);
      drawLine(pt3, pt5);
      drawLine(pt3, pt6);

      drawLine(pt4, pt5);
      drawLine(pt4, pt6);

      drawLine(pt5, pt6);

      return new Point2D[] {
        pt1, pt2, pt3, pt4, pt5, pt6
      };
    }

    protected void drawCircle(Point2D center) {
      if (showCircles) {
        Ellipse2D centerCircle = new Ellipse2D.Double();
        centerCircle.setFrameFromCenter(center, corner(center));
        g2.draw(centerCircle);
      }
    }

    protected Point2D corner(Point2D center) {
      return new Point2D.Double(center.getX() - diam/2, center.getY() - diam/2);
    }

    protected void drawLine(Point2D pt1, Point2D pt2) {
      g2.draw(new Line2D.Double(pt1, pt2));
    }

    protected Point2D arcPoint(Point2D center, double dist, double angle) {
      double vx = dist*cos(angle);
      double vy = dist*sin(angle);
      return new Point2D.Double(center.getX()+vx, center.getY()+vy);
    }
  }

}
