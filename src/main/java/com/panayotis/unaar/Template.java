package com.panayotis.unaar;

class Template {
    static final String POM_GROUPID = "__POM_GROUPID__";
    static final String POM_ARTIFACTID = "__POM_ARTIFACTID__";
    static final String POM_VERSION = "__POM_VERSION__";
    static final String POM_PACKAGING = "__POM_PACKAGING__";
    static final String POM_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
            + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>" + POM_GROUPID + "</groupId>\n"
            + "    <artifactId>" + POM_ARTIFACTID + "</artifactId>\n"
            + "    <version>" + POM_VERSION + "</version>\n"
            + "    <packaging>" + POM_PACKAGING + "</packaging>\n"
            + "</project>\n";

}
