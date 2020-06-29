## Laguna
Agnostic and pluggable project generator for all kind of projects with a centralized repository. 100% written in Kotlin
Multiplatform with support for Mac and Linux (JVM is planned)

Features:

1. Variable substitution
1. A centralized repository (updated content!)
1. Custom repositories/folders
1. Custom commands before/after the creation of the project

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

An example of a template structure would be:

```
random-project
├── build.gradle.kts
├── settings.gradle.kts
├── src
│   └── com
│       └── example
│           └── {{name|classname}}.kt
└── tst
```

### Laguna file format
This is the file containing some metadata related to the template, for example it can contain the commands to execute
before or after the creation of the template (ie: running `gradle build` right after the creation to check that 
everything was good or creating some folders)

The file can be completely omitted and it will just do nothing

Schema (Only fields annotated with `*` are required):
```
{
    "commands": {
        "before": [],
        "after": []
    }
}
``` 

### Template format
From `KorTe`'s readme: _It is a non-strict super set of twig / django / atpl.js template engines and can support liquid templating engine as well with frontmatter._

It has variables, functions and control flow statements. A hello world example:

```kotlin
@file:JvmName("{{name|classname}}")
package com.example

fun main(args: Array<String>) {
    println("Hello world from {{name}}")
}
```

#### Custom constructs in Laguna
1. `classname`: It will translate a string to a JVM class name. For example: `my-project` will be translated to `MyProject`
1. `functionname`: Will remove non desired characters like dashes from a function name.

#### Further documentation
There are two places where you can learn more about templating:

1. Laguna uses [KorTe](https://github.com/korlibs/korte) as templating engine, its documentation is pretty good!
1. Examples at the official [laguna templates repository](https://github.com/jdiazcano/laguna-templates)

## Build from source
1. Checkout this project
1. Run `gradle release`