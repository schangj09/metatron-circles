package org.baylight.spheres;

import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import static java.lang.Math.*;
import javax.swing.*;

class Draw3dComponent extends JPanel implements Printable, PrintHandler {

  private static final Dimension preferredSize = new Dimension(1000, 1000);
  private final ControlPanelComponent controlPanel;

  public Draw3dComponent(final ControlPanelComponent controlPanel) {
    this.controlPanel = controlPanel;
  }

  @Override
  public Dimension getPreferredSize() {
    return preferredSize;
  }

  @Override
  public void onPrint() {
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
    DrawingContext drawing = new DrawingContext(g2d, diameter, controlPanel.showCircles);
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

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;
    setBackground(controlPanel.colorSelection == 'B' ? Color.BLACK : Color.WHITE);

    // offset y for control panel and adjust height of drawing area
    Dimension size = getSize();
    double width = size.getWidth();
    double height = size.getHeight();
    Point2D centerPt = new Point2D.Double(width / 2, height / 2);

    Color[] blackColors = new Color[]{
      Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN
    };
    Color[] whiteColors = new Color[]{
      Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK
    };
    Color[] colors = controlPanel.colorSelection == 'B' ? blackColors : whiteColors;

    // Image is appoximately 5 times the diameter, so choose
    // diameter to be min(pageWidth, pageHight)/5.2
    double diameter = min(width, height) / 5.2;
    DrawingContext drawing = new DrawingContext(g2d, diameter, controlPanel.showCircles);
    double rotation = PI / 6.0;
    double angleIncrement = toRadians(15);
    int indexIncrement = getIndexIncrement();
    for (int i = 0; i < 4; i += indexIncrement) {
      drawing.setColor(colors[i]);
      drawing.drawMain(centerPt, rotation + i * angleIncrement);
    }
  }

  protected int getIndexIncrement() {
    return switch (controlPanel.imageCount) {
      case 1 ->
        4;
      case 2 ->
        2;
      default ->
        1;
    };
  }

  /**
   * Inner class for drawing the circles into a given graphics context: Graphics
   * context. Diameter of the circles. Whether to show the circles or just the
   * connecting lines.
   *
   */
  protected class DrawingContext {

    private static final double DEG_60 = PI / 3.0;
    private final Graphics2D g2d;
    private final double diameter;
    private final boolean showCircles;

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
     */
    protected Point2D[] draw6(Point2D center, double dist, double rotation) {

      Point2D[] circles = new Point2D[6];
      for (int i = 0; i < 6; i++) {
        circles[i] = arcPoint(center, dist, rotation + i * DEG_60);
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

    protected void drawCircle(Point2D center) {
      if (showCircles) {
        Ellipse2D centerCircle = new Ellipse2D.Double();
        centerCircle.setFrameFromCenter(center, corner(center));
        g2d.draw(centerCircle);
      }
    }

    protected Point2D corner(Point2D center) {
      return new Point2D.Double(center.getX() - diameter / 2, center.getY() - diameter / 2);
    }

    protected void drawLine(Point2D pt1, Point2D pt2) {
      g2d.draw(new Line2D.Double(pt1, pt2));
    }

    protected Point2D arcPoint(Point2D center, double dist, double angle) {
      double vx = dist * cos(angle);
      double vy = dist * sin(angle);
      return new Point2D.Double(center.getX() + vx, center.getY() + vy);
    }
  }

}
