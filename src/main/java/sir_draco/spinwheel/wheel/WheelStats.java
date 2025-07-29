package sir_draco.spinwheel.wheel;

public class WheelStats {

    private int spins;
    private int time;
    private int rare;
    private int epic;
    private int legendary;
    private static final int MAX_SPINS = 500; // Maximum number of spins a player can hold

    public WheelStats(int spins, int time, int rare, int epic, int legendary) {
        this.spins = spins;
        this.time = time;
        this.rare = rare;
        this.epic = epic;
        this.legendary = legendary;
    }

    public int getSpins() {
        return spins;
    }

    public void setSpins(int spins) {
        this.spins = Math.min(spins, MAX_SPINS);
    }

    public void changeSpins(int amount) {
        spins = Math.min(spins + amount, MAX_SPINS);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void increaseTime() {
        time++;
    }

    public int getRare() {
        return rare;
    }

    public void setRare(int rare) {
        this.rare = rare;
    }

    public void changeRare(int amount) {
        rare += amount;
    }

    public int getEpic() {
        return epic;
    }

    public void setEpic(int epic) {
        this.epic = epic;
    }

    public void changeEpic(int amount) {
        epic += amount;
    }

    public int getLegendary() {
        return legendary;
    }

    public void setLegendary(int legendary) {
        this.legendary = legendary;
    }

    public void changeLegendary(int amount) {
        legendary += amount;
    }

    /**
     * Attempts to add spins to the player, returns true if successful (under the cap),
     * false if the player is already at the maximum spin limit
     */
    public boolean tryAddSpins(int amount) {
        if (spins >= MAX_SPINS) {
            return false; // Already at max, cannot add more
        }
        spins = Math.min(spins + amount, MAX_SPINS);
        return true;
    }

    /**
     * Gets the maximum number of spins a player can hold
     */
    public static int getMaxSpins() {
        return MAX_SPINS;
    }

    /**
     * Checks if the player is at the maximum spin limit
     */
    public boolean isAtMaxSpins() {
        return spins >= MAX_SPINS;
    }
}
