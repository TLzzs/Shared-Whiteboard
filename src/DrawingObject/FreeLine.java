package DrawingObject;

import java.awt.*;

public class FreeLine implements DrawingShape{
    private final int startX, startY, endX, endY;
    private final Color color;
    private final float strokeWidth;

    public FreeLine(int startX, int startY, int endX, int endY, Color color, float strokeWidth) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    @Override
    public void execute(Graphics2D g) {
        Color currentColor = g.getColor();
        Stroke currentStroke = g.getStroke();
        g.setColor(color);
        g.setStroke(new BasicStroke(strokeWidth));
        g.drawLine(startX, startY, endX, endY);
        g.setColor(currentColor);
        g.setStroke(currentStroke);
    }

    @Override
    public void drawTemporary(Graphics2D g2d) {

    }
}
