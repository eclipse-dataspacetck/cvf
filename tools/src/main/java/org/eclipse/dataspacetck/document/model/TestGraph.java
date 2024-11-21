package org.eclipse.dataspacetck.document.model;

import org.eclipse.dataspacetck.rendering.spi.TestPlanRenderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestGraph {
    private final Map<String, Category> categories = new HashMap<>();

    public Category category(String category) {
        return categories.computeIfAbsent(category, s -> new Category(category));
    }

    public Collection<Category> categories() {
        return categories.values();
    }

    public void render(TestPlanRenderer renderer) {
        categories()
                .forEach(cat -> {
                    renderer.category(cat);
                    cat.suites().forEach(suite -> {
                        renderer.testSuite(suite);
                        suite.testMethods().forEach(renderer::testCase);
                    });
                });
    }
}
