package com.panayotis.unaar;

class Dependency {
    final String groupId;
    final String artifactId;
    final String version;
    final String packaging;

    Dependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, "jar", version);
    }

    Dependency(String groupId, String artifactId, String packaging, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    Dependency getFlattened(String shadowGroup) {
        if (shadowGroup == null)
            shadowGroup = "";
        shadowGroup = shadowGroup.trim();
        if (!shadowGroup.isEmpty() && !shadowGroup.endsWith("."))
            shadowGroup += ".";

        return "aar".equals(packaging)
                ? new Dependency(shadowGroup + groupId, artifactId, "jar", version)
                : this;
    }

    String getFilename() {
        return artifactId + "-" + version + "." + packaging;
    }

    String getPom() {
        return artifactId + "-" + version + ".pom";
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + (packaging == null || packaging.equals("jar") ? "" : ":" + packaging) + ":" + version;
    }
}

