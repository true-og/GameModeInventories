package me.eccentric_nz.gamemodeinventories.database;

public class GameModeInventoriesRecordingManager {

    /**
     * If the recorder skips running we need to count because if this happens x times in a row, the recorder will delay
     * itself so we don't kill the server
     */
    public static int failedDbConnectionCount = 0;
}
