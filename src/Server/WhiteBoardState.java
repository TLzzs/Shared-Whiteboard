package Server;

import DrawingObject.Shape.DrawingShape;
import DrawingObject.drawingPanelElements.SavedCanvas;
import DrawingObject.drawingPanelElements.TextOnBoard;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WhiteBoardState {
    private List<Object> drawingOperations = new CopyOnWriteArrayList<>();
    private final List<RequestHandler> requestHandlers = new CopyOnWriteArrayList<>();
    private List<TextOnBoard> textOnBoardList = new CopyOnWriteArrayList<>();
    private SavedCanvas savedCanvas;

    public SavedCanvas getSavedCanvas() {
        return savedCanvas;
    }

    public void setSavedCanvas(SavedCanvas savedCanvas) {
        this.savedCanvas = savedCanvas;
    }

    public synchronized void updateState(DrawingShape operation) {
        drawingOperations.add(operation);
    }
    public List<Object> getCurrentState() {
        return drawingOperations;
    }
    public void addClientHandler(RequestHandler requestHandler) {
        requestHandlers.add(requestHandler);
    }
    public List<RequestHandler> getRequestHandler() {
        return requestHandlers;
    }
    public boolean isFirstClient() {
        return requestHandlers.size() == 1;
    }
    public void deleteAllOperations (){
        drawingOperations = new CopyOnWriteArrayList<>();
    }
    public void addTextOnBoard(TextOnBoard textOnBoard) {
        textOnBoardList.add(textOnBoard);
    }
    public void deleteAllTextOnBoard () {
        textOnBoardList = new CopyOnWriteArrayList<>();
    }

    public List<TextOnBoard> getTextOnBoardList() {
        return textOnBoardList;
    }

    public boolean checkUserNameDuplicate(String username) {
        for (Iterator<RequestHandler> iterator = requestHandlers.iterator(); ((Iterator<?>) iterator).hasNext();) {
            RequestHandler client = iterator.next();
            if (client.getUserName() != null && client.getUserName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void deleteRequestHandler(RequestHandler requestHandler) {
        requestHandlers.remove(requestHandler);
    }

//    public void deleteRequestHandler(String userName) {
////        requestHandlers.
//
//    }
}
