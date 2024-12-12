package com.grookage.concierge.models.config;


public enum ConfigEvent {

    CREATE_CONFIG {
        @Override
        public <T> T accept(ConfigEventVisitor<T> eventVisitor) {
            return eventVisitor.configCreate();
        }
    },

    UPDATE_CONFIG {
        @Override
        public <T> T accept(ConfigEventVisitor<T> eventVisitor) {
            return eventVisitor.configUpdate();
        }
    },

    APPROVE_CONFIG {
        @Override
        public <T> T accept(ConfigEventVisitor<T> eventVisitor) {
            return eventVisitor.configApprove();
        }
    },

    ACTIVATE_CONFIG {
        @Override
        public <T> T accept(ConfigEventVisitor<T> eventVisitor) {
            return eventVisitor.configActivate();
        }
    },

    REJECT_CONFIG {
        @Override
        public <T> T accept(ConfigEventVisitor<T> eventVisitor) {
            return eventVisitor.configReject();
        }
    };

    public abstract <T> T accept(ConfigEventVisitor<T> eventVisitor);
}
