package Client;

import DrawingObject.*;
import DrawingObject.Rectangle;

import java.awt.*;

public class shapeUtility {
    public static void updateShape(DrawingShape shape, int x1, int y1, int x2, int y2) {
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            int centerX = x1 - radius;
            int centerY = y1 - radius;
            circle.update(centerX, centerY, 2 * radius, 2 * radius);
        } else if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            int width = Math.abs(x2 - x1);
            int height = Math.abs(y2 - y1);
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            rectangle.update(minX, minY, width, height);
        } else if (shape instanceof Line) {
            Line line = (Line) shape;
            line.update(x1, y1, x2, y2);
        } else if (shape instanceof Oval) {
            Oval oval = (Oval) shape;
            int ovalWidth = Math.abs(x2 - x1);
            int ovalHeight = Math.abs(y2 - y1);
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            oval.update(minX, minY, ovalWidth, ovalHeight);
        }
    }

    public static DrawingShape createShape(int x1, int y1, int x2, int y2, Color color, String currentTool, float lineWidth) {
        switch (currentTool) {
            case "FreeLine":
                return new FreeLine(x1, y1, x2, y2, color, lineWidth);
            case "Line":
                return new Line(x1, y1, x2, y2, color, lineWidth);
            case "Circle":
                int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                int topLeftX = x1 - radius;
                int topLeftY = y1 - radius;
                return new Circle(topLeftX, topLeftY, 0, 0, color, lineWidth);
            case "Rectangle":
                int width = Math.abs(x2 - x1);
                int height = Math.abs(y2 - y1);
                return new Rectangle(x1, y1, width, height, color, lineWidth);
            case "Oval":
                int ovalWidth = Math.abs(x2 - x1);
                int ovalHeight = Math.abs(y2 - y1);
                return new Oval(x1, y1, ovalWidth, ovalHeight, color, lineWidth);
            default:
                return null;
        }
    }
}
