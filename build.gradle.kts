plugins {
    id("java")
    id("jacoco")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "br.com.yuri.alpha7"
version = "1.0-SNAPSHOT"

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("javax.persistence:javax.persistence-api:2.2")
    implementation("org.hibernate:hibernate-core:5.6.15.Final")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.flywaydb:flyway-core:9.22.3")
    implementation("org.hibernate:hibernate-jcache:5.6.15.Final")
    implementation("org.ehcache:ehcache:3.10.8")
    implementation("javax.cache:cache-api:1.1.1")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.3.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.apache.commons:commons-csv:1.11.0")
    implementation("com.formdev:flatlaf:3.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    runtimeOnly("org.postgresql:postgresql:42.7.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("io.zonky.test:embedded-postgres:1.3.1")
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.shadowJar {
    archiveBaseName.set("alpha7-library-manager")
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "br.com.yuri.alpha7.Main"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "**/config/**",
                "**/Main.class",
                "**/presentation/MainWindow.class",
                "**/presentation/livro/view/LivroFormDialog*.class",
                "**/presentation/livro/view/LivroListPanel*.class",
                "**/presentation/livro/view/ImportPreviewDialog*.class",
                "**/presentation/livro/view/ImportProgressDialog*.class",
                "**/presentation/livro/view/ProgressDialog*.class",
                "**/presentation/livro/presenter/LivroTableModel.class",
                "**/presentation/livro/presenter/LivroListPresenter.class",
                "**/presentation/livro/presenter/LivroListPresenter\$*.class",
                "**/presentation/stats/view/AcervoStatsPanel*.class",
                "**/presentation/stats/presenter/AcervoStatsPresenter.class",
                "**/presentation/stats/presenter/AcervoStatsPresenter\$*.class"
            )
        }
    }))
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "**/config/**",
                "**/Main.class",
                "**/presentation/MainWindow.class",
                "**/presentation/livro/view/LivroFormDialog*.class",
                "**/presentation/livro/view/LivroListPanel*.class",
                "**/presentation/livro/view/ImportPreviewDialog*.class",
                "**/presentation/livro/view/ImportProgressDialog*.class",
                "**/presentation/livro/view/ProgressDialog*.class",
                "**/presentation/livro/presenter/LivroTableModel.class",
                "**/presentation/livro/presenter/LivroListPresenter.class",
                "**/presentation/livro/presenter/LivroListPresenter\$*.class",
                "**/presentation/stats/view/AcervoStatsPanel*.class",
                "**/presentation/stats/presenter/AcervoStatsPresenter.class",
                "**/presentation/stats/presenter/AcervoStatsPresenter\$*.class"
            )
        }
    }))
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
