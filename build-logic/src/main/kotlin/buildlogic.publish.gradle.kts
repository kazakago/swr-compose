plugins {
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = "com.kazakago.swr",
        version = "1.0.0",
    )
    pom {
        name.set("swr-compose")
        description.set("\"React SWR\" ported for Jetpack Compose & Compose Multiplatform")
        inceptionYear.set("2022")
        url.set("https://github.com/kazakago/swr-compose")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("kazakago")
                email.set("kazakago@gmail.com")
                url.set("https://github.com/kazakago/")
            }
        }
        scm {
            url.set("https://github.com/kazakago/swr-compose")
            connection.set("scm:git:git://github.com/kazakago/swr-compose.git")
            developerConnection.set("scm:git:ssh://git@github.com/kazakago/swr-compose.git")
        }
    }
}
