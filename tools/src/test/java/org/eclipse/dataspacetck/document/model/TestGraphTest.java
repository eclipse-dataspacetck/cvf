package org.eclipse.dataspacetck.document.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestGraphTest {

    @Test
    void category_whenNotExists() {
        var g = new TestGraph();
        g.category("test-category");
        assertThat(g.categories()).hasSize(1);
    }

    @Test
    void category_whenExists() {
        var g = new TestGraph();
        g.category("test-category");
        g.category("test-category");
        assertThat(g.categories()).hasSize(1);
    }

    @Test
    void render() {
    }
}