---
layout: docs
title: "Downloading"
category: doc
date: 2018-11-19 16:55:00
order: 2
---

# Choosing the distribution
By default everyone should try using CLI first. Gradle plugin is only applicable to Android project. However there are some caveats to using it, e.g. incompatible gradle versions between the plugin and your project, incompatible libraries which in the end require you to change gradle version such as Kotlin DSL plugin or Coroutines.

CLI provides other benefits such as ease of having multiple configurations which you will have to implement through your gradle code otherwise. If you upgrade your build setup you're gonna need to check if gradle plugin still works, but you don't depend on your build system using CLI.

In the end CLI is a much more stable and concise choice.

## CLI
Grab the latest release from [GitHub Releases][1] page. Extract the archive into your apps folder and add the binary to your path using local terminal session or using your profile file (.bashrc or equivalent), e.g.

```bash
unzip -d $DESTINATION marathon-X.X.X.zip
export PATH=$PATH:$DESTINATION/marathon-X.X.X/bin
```

## MacOS
Grab the latest release with [homebrew][5]:

```bash
brew tap malinskiy/tap
brew install malinskiy/tap/marathon
```

## Gradle
Marathon gradle plugin is published to [MavenCentral][2] so make sure you have this registry either by declaring it directly or using an aggregating registry such as [jCenter][3]. Then apply the plugin


{% tabs gradle %}
{% tab gradle .gradle %}
```kotlin
plugins {
    id 'marathon' version 'X.X.X'
}
```
{% endtab %}
{% tab gradle .kts %}
```kotlin
plugins {
    id("marathon") version "X.X.X"
}
```
{% endtab %}
{% endtabs %}

You also need to add the following to your settings.gradle(.kts):

```kotlin
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "marathon") {
                useModule("com.malinskiy.marathon:marathon-gradle-plugin:${requested.version}")
            }
        }
    }
}
```

All the relevant test tasks should start with **marathon** prefix such as *marathonDebugAndroidTest*.

[1]: https://github.com/MarathonLabs/marathon/releases

[2]: https://search.maven.org/

[3]: https://bintray.com/bintray/jcenter

[4]: https://github.com/MarathonLabs/marathon/releases/latest

[5]: https://brew.sh/
