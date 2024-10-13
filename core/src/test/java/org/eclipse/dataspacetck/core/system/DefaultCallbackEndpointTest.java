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

package org.eclipse.dataspacetck.core.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultCallbackEndpointTest {

    private DefaultCallbackEndpoint endpoint;

    @Test
    void verifyDirectHandle() {
        endpoint.registerHandler("/foo", mock());
        assertThat(endpoint.handlesPath("/foo/")).isTrue();
        assertThat(endpoint.handlesPath("/foo")).isTrue();
        assertThat(endpoint.handlesPath("/foobar")).isFalse();
    }

    @Test
    void verifyPatternHandle() {
        endpoint.registerHandler("/foo/[^/]+/bar/", mock());
        assertThat(endpoint.handlesPath("/foo/123/bar/")).isTrue();
        assertThat(endpoint.handlesPath("/foo/123/bar")).isTrue();
        assertThat(endpoint.handlesPath("/foo/bar")).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifyDispatch() {
        var mock = mock(Function.class);
        when(mock.apply(any())).thenReturn("return");
        endpoint.registerHandler("/foo/[^/]+/bar/", mock);
        assertThat(endpoint.apply("/foo/123/bar/", new ByteArrayInputStream(new byte[0]))).isEqualTo("return");
    }

    @BeforeEach
    void setUp() {
        endpoint = DefaultCallbackEndpoint.Builder.newInstance().address("http://localhost").build();
    }
}