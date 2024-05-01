package DrawingObject.drawingPanelElements;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class SavedCanvas implements Serializable {
    private String name;
    private byte[] imageData;
    private boolean isSaving;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SavedCanvas(String name, BufferedImage canvas) {
        this.name = name;
        this.imageData = convertToByteArray(canvas);
        this.isSaving = true;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public boolean isSaving() {
        return isSaving;
    }

    public void setSaving(boolean saving) {
        isSaving = saving;
    }

    private byte[] convertToByteArray(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos); // Write the image to the output stream as PNG
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
