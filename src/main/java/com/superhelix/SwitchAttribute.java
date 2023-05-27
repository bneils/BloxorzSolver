package com.superhelix;

import java.util.List;

public record SwitchAttribute(ActivationType activationType, List<TileAction> bridgeActions,
                              Position[] teleportLocations) {
}