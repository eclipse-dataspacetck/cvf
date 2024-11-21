package org.eclipse.dataspacetck.document.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void testSuite_whenNotExists() {
        var cat = new Category("test-cat");
        cat.testSuite("test-suite").insert(new TestCase("m", "d","n",true, "diag"));

        assertThat(cat.suites()).hasSize(1);
    }

    @Test
    void testSuite_whenExists_shouldAppend() {
        var cat = new Category("test-cat");
        cat.testSuite("test-suite").insert(new TestCase("m", "d","n",true, "diag"));
        cat.testSuite("test-suite").insert(new TestCase("m2", "d2","n2",true, "diag"));

        assertThat(cat.suites()).hasSize(1);
    }
}
