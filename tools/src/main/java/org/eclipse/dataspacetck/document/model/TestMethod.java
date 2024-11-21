package org.eclipse.dataspacetck.document.model;

public record TestMethod(String methodName, String displayName, String number, boolean isMandatory, String diagramCode) {
    public String specUrl(){
        return "https://foo.bar/spec/" + number;
    }
}
