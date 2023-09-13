package org.baylight.circles;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;

import static java.lang.Math.*;

/**
 * A JPanel component that contains a control panel with several UI controls and
 * additional space on the screen to draw the Metatron Cubes.
 *
 * @author Jeffrey Schang
 */
class MetaComponent extends JPanel implements ItemListener, ActionListener, Printable {

  private static final Dimension preferredSize = new Dimension(1000, 1000);
  private final JPanel controlPanel;
  private final JCheckBox colorCheckbox;
  private final JCheckBox circlesCheckbox;
  private final JButton printButton;
  private char colorSelection = 'B';
  private boolean showCircles = Boolean.TRUE;
  private int imageCount = 1;

  /**
   * Default constructor.
   */
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
    setControlPanelColors();

    add(controlPanel);
  }

  /**
   * Override for the initial preferred size of the screen component.
   *
   * @return the preferred size
   */
  @Override
  public Dimension getPreferredSize() {
    return preferredSize;
  }

  /**
   * Listens to the check boxes.
   */
  @Override
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

  /**
   * Listens to the drop down combo or print button
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == printButton) {
      onPrint();
    } else {
      JComboBox cb = (JComboBox) source;
      String newSelection = (String) cb.getSelectedItem();
      try {
        imageCount = Integer.parseInt(newSelection);
      } catch (NumberFormatException exc) {
        System.out.println("Error parsing selection" + exc);
      }
      repaint();
    }
  }

  /**
   * Helper method to set the colors of the control panel and its components.
   */
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

  /**
   * Helper method to invoke the print dialog. When the user presses Print in
   * the dialog then the framework will call the print method.
   */
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

  /**
   * Override the default print method to implement printing functionality. We
   * print only one page with the diagram occupying as much of the page as
   * possible.
   *
   * @param g the printer graphics context
   * @param pf the page format details
   * @param page the index of the page to print
   * @return a code from {@link Printable} indicating whether there more pages
   * to print
   * @throws {@link PrinterException}
   */
  @Override
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
    Graphics2D g2d = (Graphics2D) g;
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
    double diameter = min(pageWidth, pageHeight) / 5;
    DrawingContext drawing = new DrawingContext(g2d, diameter, showCircles);
    Point2D centerPt = new Point2D.Double(pageWidth / 2, pageHeight / 2);
    double rotation = PI / 6.0;
    double angleIncrement = toRadians(15);
    int indexIncrement = getIndexIncrement();
    for (int i = 0; i < 4; i += indexIncrement) {
      drawing.drawMain(centerPt, rotation);
      rotation += angleIncrement * indexIncrement;
    }

    // tell the caller that this page is part
    // of the printed document
    return PAGE_EXISTS;
  }

  /**
   * Override the default paintComponent for drawing in the screen Graphics
   * context.
   *
   * @param g the screen graphics context
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;
    setBackground(colorSelection == 'B' ? Color.BLACK : Color.WHITE);

    // offset y for control panel and adjust height of drawing area
    Dimension size = getSize();
    double yOffset = controlPanel.getHeight();
    double width = size.getWidth();
    double height = size.getHeight() - yOffset;
    Point2D centerPt = new Point2D.Double(width / 2, yOffset + height / 2);

    Color[] blackColors = new Color[]{
      Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN
    };
    Color[] whiteColors = new Color[]{
      Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK
    };
    Color[] colors = colorSelection == 'B' ? blackColors : whiteColors;

    // Image is appoximately 5 times the diameter, so choose
    // diameter to be min(pageWidth, pageHight)/5.2
    double diameter = min(width, height) / 5.2;
    DrawingContext drawing = new DrawingContext(g2d, diameter, showCircles);
    double rotation = PI / 6.0;
    double angleIncrement = toRadians(15);
    int indexIncrement = getIndexIncrement();
    for (int i = 0; i < 4; i += indexIncrement) {
      drawing.setColor(colors[i]);
      drawing.drawMain(centerPt, rotation + i * angleIncrement);
    }
  }

  /**
   * Helper method to calculate the value to increment the index based on the
   * desired number of instances of the metatron cube.
   *
   * @return the increment value
   */
  protected int getIndexIncrement() {
    return switch (imageCount) {
      case 1 ->
        4;
      case 2 ->
        2;
      default ->
        1;
    };
  }

  /**
   * Inner class for drawing a metatron cube into a given {@link Graphics2D}.
   * The diameter is fixed but the rotation is specified for each call.
   */
  protected class DrawingContext {

    private static final double deg60 = PI / 3.0;
    private final Graphics2D g2d;
    private final double diameter;
    private final boolean showCircles;

    /**
     * Default constructor.
     *
     * @param g2d the graphics context to draw into
     * @param diameter the diameter of the circles in the metatron cube
     * @param showCircles whether to show the circles in addition to lines
     */
    DrawingContext(Graphics2D g2d, double diameter, boolean showCircles) {
      this.g2d = g2d;
      this.diameter = diameter;
      this.showCircles = showCircles;
    }

    public void setColor(Color color) {
      g2d.setColor(color);
    }

    /**
     * Draw all circles and connecting lines of the metatron with an initial
     * rotation offset.
     *
     * Note: if showCircles is false, then only the connecting lines are drawn.
     *
     * @param centerPt the center of the metatron cube
     * @param rotation the rotation offset to use
     */
    public void drawMain(Point2D centerPt, double rotation) {
      drawCircle(centerPt);
      double dist = diameter;
      Point2D[] innerPts = draw6(centerPt, dist, rotation);
      Point2D[] outerPts = draw6(centerPt, dist * 2, rotation);

      connectInnerOuter(0, innerPts, outerPts);
      connectInnerOuter(1, innerPts, outerPts);
      connectInnerOuter(2, innerPts, outerPts);
      connectInnerOuter(3, innerPts, outerPts);
      connectInnerOuter(4, innerPts, outerPts);
      connectInnerOuter(5, innerPts, outerPts);
    }

    /**
     * Draw 2 lines connecting the given outer point to the 2 inner points
     * across from it that are not already connected by other outer point
     * connecting lines.
     *
     * @param index index of the outer point we are connecting
     * @param innerPts array of the center points of the inner circles
     * @param outerPts array of the center points of the outer circles
     */
    protected void connectInnerOuter(int index, Point2D[] innerPts, Point2D[] outerPts) {
      drawLine(outerPts[index], innerPts[(index + 2) % 6]);
      drawLine(outerPts[index], innerPts[(index + 4) % 6]);
    }

    /**
     * Draw 6 circles around the center point with the given distance and an
     * initial rotation offset. Also connect all of the circle center points by
     * lines.
     *
     * Note: if showCircles is false, then only the connecting lines are drawn.
     *
     * @param center center of the collection of circles
     * @param dist distance between the circles center points
     * @param rotation rotation offset in radians
     * @return the array of center points of the six circles
     */
    protected Point2D[] draw6(Point2D center, double dist, double rotation) {

      Point2D[] circles = new Point2D[6];
      for (int i = 0; i < 6; i++) {
        circles[i] = vectorPoint(center, dist, rotation + i * deg60);
        drawCircle(circles[i]);
      }
      Point2D pt1 = circles[0];
      Point2D pt2 = circles[1];
      Point2D pt3 = circles[2];
      Point2D pt4 = circles[3];
      Point2D pt5 = circles[4];
      Point2D pt6 = circles[5];

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

      return circles;
    }

    /**
     * Draw a single circle at the given point.
     *
     * @param center the center point
     */
    protected void drawCircle(Point2D center) {
      if (showCircles) {
        Ellipse2D centerCircle = new Ellipse2D.Double();
        centerCircle.setFrameFromCenter(center, corner(center));
        g2d.draw(centerCircle);
      }
    }

    /**
     * Calculate the upper left corner of the rectangle that encloses a circle.
     *
     * @param center the center of the circle
     * @return the upper left corner of the circle
     */
    protected Point2D corner(Point2D center) {
      return new Point2D.Double(center.getX() - diameter / 2, center.getY() - diameter / 2);
    }

    /**
     * Draw a line between two points.
     *
     * @param pt1 the start point
     * @param pt2 the end point
     */
    protected void drawLine(Point2D pt1, Point2D pt2) {
      g2d.draw(new Line2D.Double(pt1, pt2));
    }

    /**
     * Calculate a point with the given vector (distance and angle) from the
     * center point.
     *
     * @param center the center point
     * @param dist distance of the vector
     * @param angle angle of the vector in radians
     * @return the point at the end of the vector
     */
    protected Point2D vectorPoint(Point2D center, double dist, double angle) {
      double vx = dist * cos(angle);
      double vy = dist * sin(angle);
      return new Point2D.Double(center.getX() + vx, center.getY() + vy);
    }
  }

}
