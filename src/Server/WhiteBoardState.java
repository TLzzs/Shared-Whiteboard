package Server;

import DrawingObject.DrawingShape;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WhiteBoardState {
    private final List<Object> drawingOperations = new CopyOnWriteArrayList<>();
    private final List<RequestHandler> requestHandlers = new CopyOnWriteArrayList<>();
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
        return requestHandlers.size() == 0;
    }
}
