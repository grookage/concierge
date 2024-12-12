package com.grookage.concierge.models.config;

public interface ConfigEventVisitor<T> {

    T configCreate();

    T configUpdate();

    T configApprove();

    T configReject();

    T configActivate();

}
