package sir_draco.spinwheel.furnaces;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

public class CustomFurnace {

    private final Location location;
    private final int speed;
    private final FurnaceInventory inventory;

    private int ticksPassed;
    private int fuel;
    private boolean canBreak = false;

    public CustomFurnace(Location location, int speed) {
        this.location = location;
        this.speed = speed;
        String name;
        switch (speed) {
            case 1 -> name = "2x Speed Furnace";
            case 2 -> name = "3x Speed Furnace";
            case 3 -> name = "4x Speed Furnace";
            case 4 -> name = "2 Tick Furnace";
            default -> name = "Furnace";
        }
        inventory = (FurnaceInventory) Bukkit.createInventory(null, InventoryType.FURNACE, name);
    }

    public void tickFurnace() {
        if (getSmelting() == null) {
            updateFurnaceVisualState(false);
            return;
        }
        if (fuel <= 0 && getFuel() == null) {
            updateFurnaceVisualState(false);
            return;
        }

        removeFuel();

        // Make sure the furnace can smelt the item
        if (!CustomFurnaceChecker.getFurnaceRecipes().containsKey(getSmelting().getType())) {
            updateFurnaceVisualState(false);
            return;
        }
        // Check if the furnace is already full
        if (getResult() != null && getResult().getAmount() == 64) {
            updateFurnaceVisualState(false);
            return;
        }
        // Make sure the fuel is valid
        if (getFuel() != null && !CustomFurnaceChecker.getBurnTimeList().containsKey(getFuel().getType())) {
            updateFurnaceVisualState(false);
            return;
        }

        // Fuel
        handleBurningFuel();
        if (fuel == 0) {
            updateFurnaceVisualState(false);
            return; // Out of fuel
        }

        // Furnace is actively smelting
        updateFurnaceVisualState(true);

        // Cook item
        cookItem();
    }

    private void updateFurnaceVisualState(boolean shouldBeLit) {
        if (!shouldBeLit) return;
        Block block = location.getBlock();
        if (block.getType() == Material.FURNACE && block.getState() instanceof Furnace furnaceState) {
            // Set the furnace to appear lit
            block.setType(Material.FURNACE);
            furnaceState.setBurnTime((short) 2);
            furnaceState.setCookTime((short) 0);
            furnaceState.update();
        }
    }

    private void handleBurningFuel() {
        if (fuel <= 0 && getFuel() != null) {
            fuel = CustomFurnaceChecker.getBurnTimeList().get(getFuel().getType());
            if (getFuel().getType().equals(Material.LAVA_BUCKET)) {
                inventory.setFuel(new ItemStack(Material.BUCKET));
            } else {
                int fuelLeft = getFuel().getAmount() - 1;
                if (fuelLeft == 0) inventory.setFuel(null);
                else inventory.setFuel(new ItemStack(getFuel().getType(), fuelLeft));
            }
        }
    }

    private void removeFuel() {
        if (fuel != 0) {
            switch (speed) {
                case 1 -> fuel -= 1;
                case 2 -> fuel -= 2;
                case 3 -> fuel -= 4;
                default -> fuel -= 50;
            }
        }
    }

    private void cookItem() {
        switch (speed) {
            case 1 -> ticksPassed += 2;
            case 2 -> ticksPassed += 3;
            case 3 -> ticksPassed += 4;
            case 4 -> ticksPassed += 50;
            default -> ticksPassed += 1;
        }

        if (ticksPassed < 200) return;
        Material mat = CustomFurnaceChecker.getFurnaceRecipes().get(getSmelting().getType());
        if (getResult() != null) {
            int amount = getResult().getAmount() + 1;
            inventory.setResult(new ItemStack(mat, amount));
        }
        else inventory.setResult(new ItemStack(mat, 1));
        int amount = getSmelting().getAmount() - 1;
        if (amount == 0) inventory.setSmelting(null);
        else inventory.setSmelting(new ItemStack(getSmelting().getType(), amount));
        ticksPassed = 0;
    }

    public void openFurnace(Player p) {
        p.openInventory(inventory);
    }

    public ItemStack getFuel() {
        return inventory.getFuel();
    }

    public ItemStack getSmelting() {
        return inventory.getSmelting();
    }

    public ItemStack getResult() {
        return inventory.getResult();
    }

    public int getSpeed() {
        return speed;
    }

    public Location getLocation() {
        return location;
    }

    public FurnaceInventory getInventory() {
        return inventory;
    }

    public void setCanBreak(boolean canBreak) {
        this.canBreak = canBreak;
    }

    public boolean canBreak() {
        return canBreak;
    }
}
