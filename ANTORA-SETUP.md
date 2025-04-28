# Antora Documentation Setup for Fabrikt Gradle Plugin

This document provides a summary of the changes made to implement the Antora documentation setup for the Fabrikt Gradle Plugin.

## Overview

The documentation is organized into two main components:
- **User Documentation**: For users of the plugin
- **Developer Documentation**: For developers contributing to the plugin

## Files and Directories Created

### Configuration Files
- `antora-playbook.yml`: The main Antora configuration file that defines the site structure and content sources.

### Documentation Structure
- `documentation/`: The main documentation directory
  - `README.md`: Instructions for building and maintaining the documentation
  - `user/`: User documentation component
    - `antora.yml`: Component descriptor for user documentation
    - `modules/ROOT/nav.adoc`: Navigation file for user documentation
    - `modules/ROOT/pages/`: Directory containing user documentation pages
      - `index.adoc`: Home page for user documentation
      - `getting-started.adoc`: Getting started guide
      - `configuration.adoc`: Configuration options
      - `usage.adoc`: Usage examples
  - `developer/`: Developer documentation component
    - `antora.yml`: Component descriptor for developer documentation
    - `modules/ROOT/nav.adoc`: Navigation file for developer documentation
    - `modules/ROOT/pages/`: Directory containing developer documentation pages
      - `index.adoc`: Home page for developer documentation
      - `architecture.adoc`: Architecture overview
      - `contributing.adoc`: Contributing guidelines
      - `development-setup.adoc`: Development setup instructions
      - `release-process.adoc`: Release process documentation

### Build Integration
- Added a `buildDocs` task to `build.gradle.kts` to automate the documentation build process.

## How to Build the Documentation

From the root of the repository, run:

```bash
./gradlew buildDocs
```

This will:
1. Check if Node.js is installed and available in your PATH
2. Install Antora CLI and Site Generator locally if not already installed
3. Build the documentation site in the `build/site` directory

The task will provide helpful error messages if any prerequisites are missing.

## How to View the Documentation

After building the documentation, open `build/site/index.html` in your web browser.

## Next Steps

1. **Install Node.js and npm**: Required to run Antora
2. **Install Antora CLI and Site Generator**: `npm i -g @antora/cli @antora/site-generator`
3. **Build the Documentation**: `./gradlew buildDocs`
4. **Review the Generated Site**: Open `build/site/index.html` in your browser
5. **Customize the Content**: Update the AsciiDoc files as needed
6. **Consider CI/CD Integration**: Add documentation build to your CI/CD pipeline

## Resources

- [Antora Documentation](https://docs.antora.org/antora/latest/)
- [AsciiDoc Syntax](https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/)
