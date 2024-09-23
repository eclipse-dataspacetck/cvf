/*
 *  Copyright (c) 2024 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */
package org.eclipse.dataspacetck.core.spi.boot;

/**
 * Sends formatted messages to the system output.
 */
public interface Monitor {

    Monitor enableSuccess();

    Monitor enableError();

    Monitor enableBold();

    Monitor resetMode();

    Monitor newLine();

    Monitor message(String message);

    Monitor debug(String message);
}
