package com.github.nyukhalov.highloadcup;

import org.rapidoid.net.Protocol;
import org.rapidoid.net.impl.DefaultExchange;
import org.rapidoid.net.impl.RapidoidHelper;
import org.rapidoid.net.impl.RapidoidServerLoop;

import javax.net.ssl.SSLContext;
import java.lang.reflect.Field;

public class CustomRapidoidServerLoop extends RapidoidServerLoop {
    public CustomRapidoidServerLoop(
            Protocol protocol, Class<? extends DefaultExchange<?>> exchangeClass, Class<? extends RapidoidHelper> helperClass,
            String address, int port, int workers, int bufSizeKB, boolean noDelay, boolean syncBufs, boolean blockingAccept, SSLContext sslContext) {
        super(protocol, exchangeClass, helperClass, address, port, workers, bufSizeKB, noDelay, syncBufs, sslContext);

        try {
            Field blockingAcceptField = RapidoidServerLoop.class.getDeclaredField("blockingAccept");
            blockingAcceptField.setAccessible(true);
            blockingAcceptField.setBoolean(this, blockingAccept);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
