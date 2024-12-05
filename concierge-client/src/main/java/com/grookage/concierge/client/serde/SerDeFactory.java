package com.grookage.concierge.client.serde;

public interface SerDeFactory {

    SerDe getSerDe(String configName);

}
