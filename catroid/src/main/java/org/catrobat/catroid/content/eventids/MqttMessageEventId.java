package org.catrobat.catroid.content.eventids;

import java.util.UUID;

public class MqttMessageEventId extends EventId {
    private static final long serialVersionUID = 1L;

    private final String uniqueId = UUID.randomUUID().toString();

    public MqttMessageEventId() {
        super(EventId.USER_CONCAT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MqttMessageEventId)) return false;
        return uniqueId.equals(((MqttMessageEventId) o).uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
