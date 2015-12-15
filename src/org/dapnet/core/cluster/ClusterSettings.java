/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.cluster;

import java.io.Serializable;

public class ClusterSettings implements Serializable {
    private int responseTimeout = 10000;
    private String clusterConfigurationFile = "ClusterConfig.xml";

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public String getClusterConfigurationFile() {
        return clusterConfigurationFile;
    }
}
