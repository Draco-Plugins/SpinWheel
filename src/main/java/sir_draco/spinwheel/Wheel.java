package sir_draco.spinwheel;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.HashMap;

public class Wheel {

    private boolean spinning;
    private final Location c1;
    private final Location c2;
    private final Location c3;
    private final Location c4;
    private final HashMap<Integer, Material> colorLocation = new HashMap<>();
    private final ArrayList<ArrayList<Location>> locationList = new ArrayList<>();
    private final ArrayList<Location> locations = new ArrayList<>();

    public Wheel(Location loc) {
        // Create Corners
        this.c1 = loc;
        c2 = c1.clone().add(0, 0, 1);
        c3 = c1.clone().add(-1, 0, 1);
        c4 = c1.clone().add(-1, 0, 0);
        // Create sections with certain colors attached
        createColorLocations();
        // Create white center
        setCenter(Material.WHITE_CONCRETE);
        // Create the wheel spoke locations
        generateLocations();
        // Make the wheel
        setWheel(true);
    }

    public void createColorLocations() {
        colorLocation.put(0, Material.RED_CONCRETE);
        colorLocation.put(1, Material.ORANGE_CONCRETE);
        colorLocation.put(2, Material.YELLOW_CONCRETE);
        colorLocation.put(3, Material.LIME_CONCRETE);
        colorLocation.put(4, Material.GREEN_CONCRETE);
        colorLocation.put(5, Material.CYAN_CONCRETE);
        colorLocation.put(6, Material.LIGHT_BLUE_CONCRETE);
        colorLocation.put(7, Material.BLUE_CONCRETE);
        colorLocation.put(8, Material.PURPLE_CONCRETE);
        colorLocation.put(9, Material.MAGENTA_CONCRETE);
        colorLocation.put(10, Material.PINK_CONCRETE);
        colorLocation.put(11, Material.LIGHT_GRAY_CONCRETE);
    }

    public void setCenter(Material mat) {
        Block cor1 = c1.getBlock();
        cor1.setType(mat);
        BlockState state = cor1.getState();
        state.setType(mat);
        state.update();

        Block cor2 = c2.getBlock();
        cor2.setType(mat);
        state = cor2.getState();
        state.setType(mat);
        state.update();

        Block cor3 = c3.getBlock();
        cor3.setType(mat);
        state = cor3.getState();
        state.setType(mat);
        state.update();

        Block cor4 = c4.getBlock();
        cor4.setType(mat);
        state = cor4.getState();
        state.setType(mat);
        state.update();
    }

    public void generateLocations() {
        genLoc(c1, true, false, false);
        genLoc(c2, true, true, true);
        genLoc(c3, false, true, false);
        genLoc(c4, false, false, true);

        for (ArrayList<Location> locs : locationList) {
            for (Location loc : locs) locations.add(loc.getBlock().getLocation());
        }
    }

    public void genLoc(Location corner, boolean xSign, boolean zSign, boolean flipOrder) {
        int x = 1;
        if (!xSign) {
            x = -1;
        }
        int z = 1;
        if (!zSign) {
            z = -1;
        }

        ArrayList<Location> spoke1 = new ArrayList<>();
        spoke1.add(corner.clone().add(0, 0, z));
        spoke1.add(corner.clone().add(0, 0, 2 * z));
        spoke1.add(corner.clone().add(0, 0, 3 * z));
        spoke1.add(corner.clone().add(0, 0, 4 * z));
        spoke1.add(corner.clone().add(0, 0, 5 * z));
        spoke1.add(corner.clone().add(0, 0, 6 * z));
        spoke1.add(corner.clone().add(0, 0, 7 * z));
        spoke1.add(corner.clone().add(x, 0, 3 * z));
        spoke1.add(corner.clone().add(x, 0, 4 * z));
        spoke1.add(corner.clone().add(x, 0, 5 * z));
        spoke1.add(corner.clone().add(x, 0, 6 * z));
        spoke1.add(corner.clone().add(x, 0, 7 * z));
        spoke1.add(corner.clone().add(2 * x, 0, 5 * z));
        spoke1.add(corner.clone().add(2 * x, 0, 6 * z));
        spoke1.add(corner.clone().add(2 * x, 0, 7 * z));

        ArrayList<Location> spoke2 = new ArrayList<>();
        spoke2.add(corner.clone().add(x, 0, z));
        spoke2.add(corner.clone().add(x, 0, 2 * z));
        spoke2.add(corner.clone().add(2 * x, 0, 2 * z));
        spoke2.add(corner.clone().add(2 * x, 0, 3 * z));
        spoke2.add(corner.clone().add(2 * x, 0, 4 * z));
        spoke2.add(corner.clone().add(3 * x, 0, 4 * z));
        spoke2.add(corner.clone().add(3 * x, 0, 5 * z));
        spoke2.add(corner.clone().add(3 * x, 0, 6 * z));
        spoke2.add(corner.clone().add(2 * x, 0, z));
        spoke2.add(corner.clone().add(3 * x, 0, 2 * z));
        spoke2.add(corner.clone().add(3 * x, 0, 3 * z));
        spoke2.add(corner.clone().add(4 * x, 0, 2 * z));
        spoke2.add(corner.clone().add(4 * x, 0, 3 * z));
        spoke2.add(corner.clone().add(4 * x, 0, 4 * z));
        spoke2.add(corner.clone().add(4 * x, 0, 5 * z));
        spoke2.add(corner.clone().add(4 * x, 0, 6 * z));
        spoke2.add(corner.clone().add(5 * x, 0, 3 * z));
        spoke2.add(corner.clone().add(5 * x, 0, 4 * z));
        spoke2.add(corner.clone().add(5 * x, 0, 5 * z));
        spoke2.add(corner.clone().add(6 * x, 0, 3 * z));
        spoke2.add(corner.clone().add(6 * x, 0, 4 * z));

        ArrayList<Location> spoke3 = new ArrayList<>();
        spoke3.add(corner.clone().add(x, 0, 0));
        spoke3.add(corner.clone().add(2 * x, 0, 0));
        spoke3.add(corner.clone().add(3 * x, 0, 0));
        spoke3.add(corner.clone().add(4 * x, 0, 0));
        spoke3.add(corner.clone().add(5 * x, 0, 0));
        spoke3.add(corner.clone().add(6 * x, 0, 0));
        spoke3.add(corner.clone().add(7 * x, 0, 0));
        spoke3.add(corner.clone().add(3 * x, 0, z));
        spoke3.add(corner.clone().add(4 * x, 0, z));
        spoke3.add(corner.clone().add(5 * x, 0, z));
        spoke3.add(corner.clone().add(6 * x, 0, z));
        spoke3.add(corner.clone().add(7 * x, 0, z));
        spoke3.add(corner.clone().add(5 * x, 0, 2 * z));
        spoke3.add(corner.clone().add(6 * x, 0, 2 * z));
        spoke3.add(corner.clone().add(7 * x, 0, 2 * z));

        if (flipOrder) {
            locationList.add(spoke3);
            locationList.add(spoke2);
            locationList.add(spoke1);
        }
        else {
            locationList.add(spoke1);
            locationList.add(spoke2);
            locationList.add(spoke3);
        }
    }

    public void setWheel(boolean normal) {
        if (!normal) {
            for (int i = 0; i < 12; i++) {
                for (Location loc : locationList.get(i)) {
                    Block block = loc.getBlock();
                    block.setType(Material.AIR);
                    BlockState state = block.getState();
                    state.setType(Material.AIR);
                    state.update();
                }
            }
            return;
        }
        for (int i = 0; i < 12; i++) {
            for (Location loc : locationList.get(i)) {
                Block block = loc.getBlock();
                block.setType(colorLocation.get(i));
                BlockState state = block.getState();
                state.setType(colorLocation.get(i));
                state.update();
            }
        }
    }

    public void rotateColors() {
        Material first = colorLocation.get(0);
        for (int i = 11; i >= 0; i--) {
            if (i == 0) {
                colorLocation.put(i + 1, first);
                break;
            }
            if (i + 1 == 12) {
                colorLocation.put(0, colorLocation.get(i));
                continue;
            }
            colorLocation.put(i + 1, colorLocation.get(i));
        }
    }

    public void spinWheel() {
        rotateColors();
        setWheel(true);
    }

    public void remove() {
        setCenter(Material.AIR);
        setWheel(false);
    }

    public ArrayList<Location> getCorners() {
        ArrayList<Location> corners = new ArrayList<>();
        corners.add(c1.clone().add(6, 0, 7));
        corners.add(c1.clone().add(-6, 0, -5));
        corners.add(c1.clone().add(6, 0, -5));
        corners.add(c1.clone().add(-6, 0, 7));
        return corners;
    }

    public ArrayList<Location> getCardinalDirections() {
        ArrayList<Location> cardinal = new ArrayList<>();
        cardinal.add(c1.clone().add(8, 0, 1));
        cardinal.add(c1.clone().add(-8, 0, 1));
        cardinal.add(c1.clone().add(0, 0, 9));
        cardinal.add(c1.clone().add(0, 0, -7));
        return cardinal;
    }

    public boolean isSpinning() {
        return spinning;
    }

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
    }

    public Location getCenter() {
        return c1.clone().add(0.5, 0, 0.5);
    }

    public Location getC1() {
        return c1;
    }

    public World getWorld() {
        return c1.getWorld();
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }
}
