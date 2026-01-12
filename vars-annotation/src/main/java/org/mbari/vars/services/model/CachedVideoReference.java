package org.mbari.vars.services.model;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-11-12T11:00:00
 */
public class CachedVideoReference {

    private String missionContact;
    private String platformName;
    private UUID videoReferenceUuid;
    private String missionId;
    private UUID uuid;

    public CachedVideoReference(String missionContact, String platformName,
                                UUID videoReferenceUuid, String missionId,
                                UUID uuid) {
        this.missionContact = missionContact;
        this.platformName = platformName;
        this.videoReferenceUuid = videoReferenceUuid;
        this.missionId = missionId;
        this.uuid = uuid;
    }

    public String getMissionContact() {
        return missionContact;
    }

    public String getPlatformName() {
        return platformName;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public String getMissionId() {
        return missionId;
    }


    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedVideoReference that = (CachedVideoReference) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "CachedVideoReference{" +
                "missionContact='" + missionContact + '\'' +
                ", missionId='" + missionId + '\'' +
                '}';
    }
}
