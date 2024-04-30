package DrawingObject;

import java.awt.*;

public class Line implements DrawingShape {
    private int x1, y1, x2, y2;
    private Color color;
    private final float strokeWidth;

    public Line(int x1, int y1, int x2, int y2, Color color, float strokeWidth) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    @Override
    public void execute(Graphics2D g2d) {
        Color currentColor = g2d.getColor();
        Stroke currentStroke = g2d.getStroke();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setColor(currentColor);
        g2d.setStroke(currentStroke);
    }

    public void update(int newX1, int newY1, int newX2, int newY2) {
        this.x1 = newX1;
        this.y1 = newY1;
        this.x2 = newX2;
        this.y2 = newY2;
    }

    public void drawTemporary(Graphics2D g2d) {
        g2d.setXORMode(Color.WHITE); // Set XOR mode with a specific background color
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setPaintMode(); // Reset to normal paint mode after drawing
    }
}
