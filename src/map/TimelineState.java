package map;

/**
 * Encapsulates information about the state of the world in a Timeline.
 * @author Peter
 */
public class TimelineState {
    
    private boolean securityAlert;
    private boolean[] mapState;
    
    protected TimelineState() {
        securityAlert = false;
        mapState = new boolean[10];
    }
    
    public boolean isAlertTriggered() {
        return securityAlert;
    }
    public void triggerAlert(boolean t) {
        securityAlert = t;
    }
    
    public void toggleMapState(int index) {
        mapState[index] = !mapState[index];
    }
    public void setMapState(int index, boolean state) {
        mapState[index] = state;
    }
    public boolean getMapState(int index) {
        return mapState[index];
    }
}
