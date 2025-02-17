package com.grookage.concierge.models.config;

public interface ConfigEventVisitor<T> {

    T configCreate();

    T configAppend();

    T configUpdate();

    T configApprove();

    T configReject();

    T configActivate();

}
