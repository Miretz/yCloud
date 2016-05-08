ycloud
==============

File storage application written in Kotlin which uses Vaadin Framework, Google Guice and MongoDB

Before run modify the config.properties file.

By default a fake in memory MongoDB (https://github.com/fakemongo/fongo) is used.

This is a complete Intellij Idea project.
I'm using the community edition.

Workflow
========
Prerequisites
- Apache Maven - to compile and run
- Intellij Idea Community Edition

Modify config.properties
- Set username and password
- Set upload directory (absolute path)

Compile using:
```
mvn clean install
```

Run embedded jetty server:
```
mvn jetty:run
```

Open browser and navigate to:
```
http://localhost:8080/
```

