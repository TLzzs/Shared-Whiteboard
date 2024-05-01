package DrawingObject.drawingPanelElements;

import java.io.Serializable;
import java.util.List;

public class ExistingCanvas implements Serializable {
    private List<SavedCanvas> savedCanvasList;

    public void setSavedCanvasList(List<SavedCanvas> savedCanvasList) {
        this.savedCanvasList = savedCanvasList;
    }

    public List<SavedCanvas> getSavedCanvasList() {
        return savedCanvasList;
    }
}
