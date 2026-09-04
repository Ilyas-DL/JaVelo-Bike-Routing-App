package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * An empty record that represents a waypoint at a given position.
 * @author Ilyas Hawazine (326815).
 */
public record Waypoint(PointCh pointCh, int closestNodeId) {
}
