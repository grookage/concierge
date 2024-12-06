package com.grookage.concierge.aerospike.exception;

import com.grookage.concierge.models.exception.ConciergeErrorCode;
import lombok.Getter;

@Getter
public enum ConciergeAeroErrorCode implements ConciergeErrorCode {

    INDEX_CREATION_FAILED(412),

    WRITE_FAILED(500);

    final int status;

    ConciergeAeroErrorCode(int status) {
        this.status = status;
    }
}
