package com.superhelix;

import java.util.List;

public record SwitchAttribute(ActivationType activationType, List<BridgeAction> bridgeActions,
                              char[] teleportLocations) {
}