package com.grookage.concierge.client.serde;

public interface SerDeFactory {

    <T> SerDe<T> getSerDe(String configName);

}
