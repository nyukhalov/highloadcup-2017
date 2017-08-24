package com.github.nyukhalov.highloadcup;

import org.rapidoid.net.Server;
import org.rapidoid.net.ServerBuilder;

public class CustomServerBuilder extends ServerBuilder {

    private boolean blockingAccept = false;

    public CustomServerBuilder blockingAccept(boolean value) {
        this.blockingAccept = value;
        return this;
    }

    @Override
    public synchronized Server build() {
        return new CustomRapidoidServerLoop(protocol(), exchangeClass(), helperClass(), address(), port(),
                workers(), bufSizeKB(), noDelay(), syncBufs(), blockingAccept, null);
    }
}
