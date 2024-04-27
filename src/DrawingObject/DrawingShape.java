package DrawingObject;

import java.awt.*;
import java.io.Serializable;

public interface DrawingShape extends Serializable {
    void execute(Graphics2D g);

    void drawTemporary(Graphics2D g2d);
}
