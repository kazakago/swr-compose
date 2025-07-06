plugins {
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

// Workaround for JavadocJar signing issue: https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(tasks.withType<Sign>())
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar.get())
        groupId = "com.kazakago.swr"
        version = "1.0.0"
        pom {
            name.set("swr-compose")
            description.set("\"React SWR\" ported for Jetpack Compose")
            url.set("https://github.com/kazakago/swr-compose")
            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            scm {
                connection.set("git:git@github.com:kazakago/swr-compose")
                developerConnection.set("git:git@github.com:kazakago/swr-compose")
                url.set("https://github.com/kazakago/swr-compose")
            }
            developers {
                developer {
                    name.set("kazakago")
                    email.set("kazakago@gmail.com")
                    url.set("https://blog.kazakago.com/")
                }
            }
        }
    }
}

signing {
    val keyId = System.getenv("SIGNING_KEY_ID") ?: findProperty("signing.keyId")?.toString()
    val secretKey = System.getenv("SIGNING_SECRET_KEY") ?: findProperty("signing.secretKey")?.toString()
    val password = System.getenv("SIGNING_PASSWORD") ?: findProperty("signing.password")?.toString()
    if (keyId != null && secretKey != null && password != null) {
        useInMemoryPgpKeys(keyId, secretKey, password)
        sign(publishing.publications)
    }
}
