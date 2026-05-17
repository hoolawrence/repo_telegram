package org.telegram.messenger;

public class TarkNetworkModule {

    public static final String VPN_ENGINE_CANDIDATE = "WireGuard Android";
    public static final String PROXY_ENGINE_CANDIDATE = "Outline Client / Shadowsocks";
    public static final String ANDROID_VPN_SERVICE = "Android VpnService";
    public static final String DEFAULT_NODE = "none";

    public enum ConnectionState {
        DISABLED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    private static final TarkNetworkModule instance = new TarkNetworkModule();

    private final NetworkStatus status = new NetworkStatus();

    private TarkNetworkModule() {
    }

    public static TarkNetworkModule getInstance() {
        return instance;
    }

    public NetworkStatus getStatus() {
        return status;
    }

    public static class NetworkStatus {
        public ConnectionState state = ConnectionState.DISABLED;
        public String activeProfileName = DEFAULT_NODE;
        public boolean vpnServiceIntegrated = false;
        public boolean hasDefaultNode = false;
        public boolean trafficRoutingEnabled = false;
    }
}
