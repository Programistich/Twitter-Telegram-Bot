import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.0"
	kotlin("plugin.spring") version "1.6.0"
	kotlin("plugin.jpa") version "1.6.0"
}

group = "com.programistich"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	maven { setUrl("https://jitpack.io") }
	maven {
		setUrl("https://takke.github.io/maven")
		content {
			includeGroup("jp.takke.twitter4j-v2")
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("jp.takke.twitter4j-v2:twitter4j-v2-support:1.0.3")
	implementation("org.twitter4j:twitter4j-core:4.0.7")
	implementation("org.twitter4j:twitter4j-media-support:4.0.6")

	implementation("org.telegram:telegrambots:5.5.0")
	implementation("org.telegram:telegrambots-meta:5.5.0")

	implementation("com.squareup.okhttp3:okhttp:4.9.3")

	implementation("com.flickr4java:flickr4java:3.0.6")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
