# fuin-fx

Hopefully useful JavaFX components

[![Java Maven Build](https://github.com/fuinorg/fuin-fx/actions/workflows/maven.yml/badge.svg)](https://github.com/fuinorg/fuin-fx/actions/workflows/maven.yml)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.fuin%3Afuin-fx&metric=coverage)](https://sonarcloud.io/dashboard?id=org.fuin%3Afuin-fx)
[![Maven Central](https://img.shields.io/maven-central/v/org.fuin/fuin-fx-navidraw.svg)](https://central.sonatype.com/artifact/org.fuin/fuin-fx-navidraw)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 25](https://img.shields.io/badge/JDK-25-green.svg)](https://openjdk.java.net/projects/jdk/25/)

## Versions

See [CHANGELOG.md](CHANGELOG.md) for the release notes.

## Navigation Drawer

A navigation drawer for JavaFX: a panel at the leading edge holding the application's top-level
destinations, beside the content on a wide window and above it on a narrow one, at full width or as an icon
only rail.

[![The navigation drawer](navidraw/doc/navigation-drawer-preview.png)](navidraw/doc/navigation-drawer.png)

For details see [navidraw](navidraw/).

## Modules

| Module                | Artifact                    | Description                                                                                                                                                                                                                |
|-----------------------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [navidraw](navidraw/) | `org.fuin:fuin-fx-navidraw` | Navigation drawer: a panel with the application's top-level destinations, beside the content or above it, at full width or as an icon rail. Collapsible sections, sub menus, a selection model and a stylesheet of its own |
| jacoco                | -                           | Aggregates the coverage of all modules. Internal, not published                                                                                                                                                            |

Each module documents itself - see [navidraw/README.md](navidraw/README.md) for what the drawer does, how it
is styled and how to run its demo.

## Snapshots

Snapshots are published to the Central snapshot repository:

```xml

<repositories>
    <repository>
        <id>central.sonatype.snapshots</id>
        <name>Central Sonatype Snapshot Repository</name>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

## Building

Requires JDK 25.

```bash
./mvnw -s settings.xml clean verify
```

The tests start the JavaFX toolkit for real but need no display: they run against JavaFX's own headless
Glass, selected with `glass.platform=Headless` in the `navidraw` module's surefire configuration.
