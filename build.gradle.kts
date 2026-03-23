plugins {
	id("java")
	id("org.jetbrains.intellij.platform") version "2.0.1"
}


java {
	toolchain {
		targetCompatibility = JavaVersion.VERSION_21
		sourceCompatibility = JavaVersion.VERSION_21
	}
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
	mavenCentral()
	intellijPlatform {
		defaultRepositories()
	}
}

dependencies {
	intellijPlatform {
		webstorm("2024.2")
		instrumentationTools()
		bundledPlugin("com.intellij.tailwindcss")
		testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
	}
	testImplementation("junit:junit:4.13.2")
	testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")
}

intellijPlatform {
	pluginConfiguration {
		id.set("com.ericfortis.tailwindeye")
		name.set("Tailwind Eye")
		vendor {
			name.set("ericfortis")
		}
		changeNotes.set("Initial version of Tailwind Eye.")

		ideaVersion {
			sinceBuild.set("242")
			untilBuild.set("253.*")
		}
	}

	pluginVerification {
		ides {
			recommended()
		}
	}

	signing {
		certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
		privateKey.set(System.getenv("PRIVATE_KEY"))
		password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
	}

	publishing {
		token.set(System.getenv("PUBLISH_TOKEN"))
	}
}
