.PHONY: help build test run clean

# Default target
help:
	@echo "Available commands:"
	@echo "  make build  - Build the plugin (creates a ZIP archive for distribution)"
	@echo "  make test   - Run all tests"
	@echo "  make run    - Run the plugin in a sandbox IDE instance"
	@echo "  make clean  - Clean the build directory"

build:
	./gradlew buildPlugin

test:
	./gradlew test

run:
	./gradlew runIde

clean:
	./gradlew clean
