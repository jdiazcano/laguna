## Laguna
Agnostic and pluggable project generator for all kind of projects with a centralized repository. 100% written in Rust

Features:

1. Variable substitution
1. A centralized repository (updated content!)
1. Custom repositories/folders (also private)

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

## Installing
### Docker
1. Install docker
1. Create an alias for an easy usage: `alias laguna="docker run terkitos/laguna "$@""`

### From source
1. Clone the project
1. cd into the project
1. `cargo install --path .`

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

An example of a template structure would be:

```
random-project
├── build.gradle.kts
├── settings.gradle.kts
├── src
│  └── com
|     └── example
│        └── {{name|camel_case}}.kt
└── tst
```

### Template format
From `Tera`'s readme: _Tera is a template engine inspired by Jinja2 and the Django template language._

It has variables, functions and control flow statements. A hello world example:

```kotlin
@file:JvmName("{{name|camel_case}}")
package com.example

fun main(args: Array<String>) {
    println("Hello world from {{name}}")
}
```

#### Custom constructs in Laguna
1. `snake_case`: Transforms a string to snake_case. For example: `my-project` will be translated to `my_project`
1. `camel_case`: Transforms a string to CamelCase. For example: `my-project` will be translated to `MyProject`
1. `mixed_case`: Transforms a string to mixedCase. For example: `my-project` will be translated to `myProject`
1. `kebab_case`: Transforms a string to mixedCase. For example: `My project` will be translated to `my-project`

#### Further documentation
There are two places where you can learn more about templating:

1. Laguna uses [Tera](https://github.com/Keats/tera) as templating engine, its documentation is pretty good!
1. Examples at the official [laguna templates repository](https://github.com/jdiazcano/laguna-templates)

## Build from source
1. Checkout this project
1. Run `cargo build`