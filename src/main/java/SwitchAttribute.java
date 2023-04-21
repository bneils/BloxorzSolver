import java.util.ArrayList;
import java.util.Optional;

public class SwitchAttribute {
    private final ActivationType activationType;
    private final ArrayList<BridgeAction> bridgeActions;
    private char[] teleportLocations;

    public SwitchAttribute(ActivationType activationType, ArrayList<BridgeAction> bridgeActions,
                           char[] teleportLocations) {
        this.activationType = activationType;
        this.bridgeActions = bridgeActions;
        this.teleportLocations = teleportLocations;
    }

    public void setTeleportLocations(char[] bridgeIds) {
        if (bridgeIds == null || bridgeIds.length == 2)
            teleportLocations = bridgeIds;
    }

    public char[] getTeleportLocations() { return teleportLocations; }

    public ActivationType getActivationType() { return activationType; }
    public ArrayList<BridgeAction> getBridgeActions() { return bridgeActions; }
}