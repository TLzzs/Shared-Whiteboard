package DrawingObject;

import java.awt.*;

public class Eraser implements DrawingShape {
    private int x1, y1, x2, y2;
    private Color color;
    private int thickness;

    public Eraser(int x1, int y1, int x2, int y2, Color color, int thickness) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public void execute(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(color);
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawTemporary(Graphics2D g2d) {

    }

    // Getters and setters if needed
}