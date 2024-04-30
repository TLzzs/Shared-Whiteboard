package DrawingObject.Shape;

import DrawingObject.Shape.DrawingShape;

import java.awt.*;

public class Circle implements DrawingShape {
    private int x, y, width, height;
    private Color color;

    private final float strokeWidth;
    public Circle(int x, int y, int width, int height, Color color,  float strokeWidth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }
    @Override
    public void execute(Graphics2D g2d) {
        Color currentColor = g2d.getColor();
        Stroke currentStroke = g2d.getStroke();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.drawOval(x, y, width, height);
        g2d.setColor(currentColor);
        g2d.setStroke(currentStroke);
    }

    public void update(int newX, int newY, int newWidth, int newHeight) {
        this.x = newX;
        this.y = newY;
        this.width = newWidth;
        this.height = newHeight;
    }

    public void drawTemporary(Graphics2D g2d) {
        g2d.setXORMode(Color.WHITE); // Set XOR mode with a specific background color
        g2d.drawOval(x, y, width, height);
        g2d.setPaintMode(); // Reset to normal paint mode after drawing
    }
}