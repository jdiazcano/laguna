## Laguna
Agnostic and pluggable project generator for all kind of projects with a centralized repository. 100% written in Kotlin
Multiplatform with support for Mac (Linux/JVM incoming)

Features:

1. Variable substitution
1. Centralized repository (updated content!)
1. Custom repositories/folders

## Usage
`laguna "template-name" --name projectname [--output /tmp][ -- variable1=value1 variable2=value2]`

```
Usage: laguna [OPTIONS] TEMPLATENAME [TEMPLATEARGUMENTS]...

Options:
  -n, --name TEXT
  -o, --output TEXT
  -h, --help         Show this message and exit
```

It will create a new folder in the output folder (current folder by default) with the directory tree of the template and
rendered files with variable substitution.

## Build from source
1. Checkout this project
1. Run `./gradlew releaseCurrent` (Needs Java!)

### Building troubleshoot