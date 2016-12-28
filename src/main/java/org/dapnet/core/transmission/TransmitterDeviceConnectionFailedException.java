/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.transmission;

public class TransmitterDeviceConnectionFailedException extends TransmitterDeviceException {
	private static final long serialVersionUID = 3658343579078637409L;

	public TransmitterDeviceConnectionFailedException(String message) {
		super(message);
	}
}
