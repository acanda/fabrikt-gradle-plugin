# Fabrikt Gradle Plugin Documentation

This directory contains the documentation for the Fabrikt Gradle Plugin, built with [Antora](https://antora.org/).

## Documentation Structure

The documentation is organized into two main components:

- **User Documentation**: Documentation for users of the plugin, located in `documentation/user/`.
- **Developer Documentation**: Documentation for developers contributing to the plugin, located in `documentation/developer/`.

## Building the Documentation

To build the documentation, you need to have Node.js and npm installed.

### Build the Documentation

From the root of the repository, you can build the documentation in two ways:

#### Using Gradle (Recommended)

```bash
./gradlew buildDocs
```

This method:
1. Checks if Node.js is installed and available in your PATH
2. Installs Antora CLI and Site Generator locally if not already installed
3. Builds the documentation site in the `build/site` directory

The task will provide helpful error messages if any prerequisites are missing.

#### Using Antora Directly

If you prefer to use Antora directly, you need to:

1. Install Node.js and npm first
2. Install Antora CLI and Site Generator:

```bash
npm i -g @antora/cli @antora/site-generator
```

3. Run Antora:

```bash
antora antora-playbook.yml
```

Both methods will generate the documentation site in the `build/site` directory.

## Viewing the Documentation

After building the documentation, you can view it by opening `build/site/index.html` in your web browser.

For local development, you can use a simple HTTP server:

```bash
# If you have Python installed
python -m http.server -d build/site

# If you have Node.js installed
npx serve build/site
```

Then open your browser and navigate to `http://localhost:8000` (or the port indicated by the command).

## Contributing to the Documentation

### User Documentation

The user documentation is located in `documentation/user/`. It includes:

- Getting started guide
- Configuration options
- Usage examples

### Developer Documentation

The developer documentation is located in `documentation/developer/`. It includes:

- Architecture overview
- Development setup
- Contributing guidelines
- Release process

### Adding New Pages

1. Create a new AsciiDoc file in the appropriate directory (`documentation/user/modules/ROOT/pages/` or `documentation/developer/modules/ROOT/pages/`).
2. Add a reference to the new page in the navigation file (`documentation/user/modules/ROOT/nav.adoc` or `documentation/developer/modules/ROOT/nav.adoc`).

### Updating Existing Pages

Simply edit the AsciiDoc files in the appropriate directory.

## Documentation Guidelines

- Use AsciiDoc syntax for all documentation files.
- Include a title and description at the top of each file.
- Use proper headings and sections to organize content.
- Include examples where appropriate.
- Link to related pages using xref.
- Use code blocks for code examples.
