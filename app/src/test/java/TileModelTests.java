import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import se2.carcassonne.model.Tile;

public class TileModelTests {
    @Test
    public void testTileConstructor() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        assertEquals(0L, tile.getId());
        assertEquals("castle_wall_road", tile.getImageName());
        assertEquals(0, tile.getRotation());
        assertArrayEquals(new int[]{3, 2, 1, 2}, tile.getEdges());
        assertArrayEquals(new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1}, tile.getFeatures());
        assertNull(tile.getCoordinates());
    }

    @Test
    public void testRotateEdgesDegree0() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedEdges = tile.rotatedEdges(0);
        assertArrayEquals(new int[]{3, 2, 1, 2}, rotatedEdges);
    }

    @Test
    public void testRotateEdgesDegree90() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedEdges = tile.rotatedEdges(1);
        assertArrayEquals(new int[]{2, 3, 2, 1}, rotatedEdges);
    }

    @Test
    public void testRotateEdgesDegree180() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedEdges = tile.rotatedEdges(2);
        assertArrayEquals(new int[]{1, 2, 3, 2}, rotatedEdges);
    }

    @Test
    public void testRotateEdgesDegree270() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedEdges = tile.rotatedEdges(3);
        assertArrayEquals(new int[]{2, 1, 2, 3}, rotatedEdges);
    }

    @Test
    public void testRotateEdgesInvalidRotation() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        assertThrows(RuntimeException.class, () -> tile.rotatedEdges(4));
    }

    @Test
    public void testRotateFeaturesDegree0() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedFeatures = tile.rotatedFeatures(0);
        assertArrayEquals(new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1}, rotatedFeatures);
    }

    @Test
    public void testRotateFeaturesDegree90() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedFeatures = tile.rotatedFeatures(1);
        assertArrayEquals(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}, rotatedFeatures);
    }

    @Test
    public void testRotateFeaturesDegree180() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedFeatures = tile.rotatedFeatures(2);
        assertArrayEquals(new int[]{1, 1, 1, 2, 2, 2, 3, 3, 3}, rotatedFeatures);
    }

    @Test
    public void testRotateFeaturesDegree270() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        int[] rotatedFeatures = tile.rotatedFeatures(3);
        assertArrayEquals(new int[]{3, 2, 1, 3, 2, 1, 3, 2, 1}, rotatedFeatures);
    }

    @Test
    public void testRotateFeaturesInvalidRotation() {
        Tile tile = new Tile(0L, "castle_wall_road", new int[]{3, 2, 1, 2}, new int[]{3, 3, 3, 2, 2, 2, 1, 1, 1});
        assertThrows(RuntimeException.class, () -> tile.rotatedFeatures(4));
    }
}
