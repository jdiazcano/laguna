## Laguna
Agnostic and pluggable project generator for all kind of projects with a centralized repository. 100% written in Kotlin
Multiplatform with support for Mac (Linux/JVM incoming)

Features:

1. Variable substitution
1. Centralized repository (updated content!)
1. Custom repositories/folders
1. Soon: Custom commands before/after the creation of the folder

## Usage
```
Usage: laguna [OPTIONS] TEMPLATENAME [TEMPLATEARGUMENTS]...

Options:
  -r, --repository TEXT  Repository (or folder) where templates are located.
  -n, --name TEXT        Project name (and name of the created folder)
  -o, --output TEXT      Folder where the project will be created (Defaults to
                         current folder)
  -v, --verbose
  -C, --no-clean         Git repository will not be updated or cleaned up.
  -c, --clean            Force clean up of repository.
  -h, --help             Show this message and exit

Arguments:
  TEMPLATENAME       Name of the template.
  TEMPLATEARGUMENTS
```

It will create a new folder in the output folder (current folder by default) with the directory tree of the template and
rendered files with variable substitution.

Example: `laguna kotlin-multiplatform --name newlaguna -- variable1=value1 variable2=value2`

## Creating a template

### Create folder structure
Creating a template is easy, the only things you need to do is to create a folder and within that folder you can already
create your templates:

```
/tmp/laguna-templates
├── kotlin-multiplatform
└── test-template
```

In this case, in the folder `laguna-templates` we have two templates, `kotlin-multiplatform` and `test-template`.

### Create files (and other folders)
Now anything inside the template folder (in the example above it would be `kotlin-multiplatform` or `test-template`),
you can create any file/folder that you want. The folder structure will be copied and the (text) files will be rendered
accordingly.

### Template format
There are two places where you can learn about templating:

1. Laguna uses [KorTe](https://github.com/korlibs/korte) as templating engine, its documentation is pretty good!
1. Examples at the official [laguna templates repository](https://github.com/jdiazcano/laguna-templates)

## Build from source
1. Checkout this project
1. Run `./gradlew release` (Needs Java!)

### Building troubleshoot