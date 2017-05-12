package org.mbari.m3.vars.annotation.model;

import com.google.common.base.Preconditions;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:12:00
 */
public class PreferenceNode {

    private final String nodeName;
    private final String prefKey;
    private String prefValue;

    public PreferenceNode(String nodeName, String prefKey, String prefValue) {
        Preconditions.checkNotNull(nodeName);
        Preconditions.checkNotNull(prefKey);
        Preconditions.checkNotNull(prefKey);
        this.nodeName = nodeName;
        this.prefKey = prefKey;
        this.prefValue = prefValue;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public String getPrefValue() {
        return prefValue;
    }

    public void setPrefValue(String prefValue) {
        Preconditions.checkNotNull(prefValue);
        this.prefValue = prefValue;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 61 * hash + ((this.nodeName != null) ? this.nodeName.hashCode() : 0);
        hash = 61 * hash + ((this.prefKey != null) ? this.prefKey.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final PreferenceNode other = (PreferenceNode) obj;

        if ((this.nodeName == null) ? (other.nodeName != null) : !this.nodeName.equals(other.nodeName)) {
            return false;
        }

        if ((this.prefKey == null) ? (other.prefKey != null) : !this.prefKey.equals(other.prefKey)) {
            return false;
        }

        return true;
    }
}
