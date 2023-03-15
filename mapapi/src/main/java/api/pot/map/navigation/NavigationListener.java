package api.pot.map.navigation;

public interface NavigationListener {
    void onSpeedLimitChange(double speed);
    void onEnabledChange(boolean enabled);
    void movingOnWay(boolean rightDir);
    void movingOnRadius(boolean inside);
    void onMoving(float mySense, float rightSense);
    void onParamsChange(long durationS, double distanceM, double speedMps);
}
