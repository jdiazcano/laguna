mod filters;
mod git;
mod ocean;
mod templater;

use crate::git::Git;
use crate::ocean::OceanArgs;
use crate::templater::Templater;
use clap::{App, AppSettings, Arg};
use std::fs;
use std::path::Path;

fn main() {
    let cli_arguments = create_arguments();

    let matches = App::new("laguna")
        .setting(AppSettings::TrailingVarArg)
        .args(&cli_arguments)
        .get_matches();

    let arguments = OceanArgs::from(matches);
    println!("{:?}", arguments);

    let repository_path = match Git::prepare_repo(&arguments) {
        Ok(path) => path,
        Err(error) => panic!(error),
    };

    let template_path = repository_path
        .as_path()
        .parent()
        .unwrap()
        .join(&arguments.template_name);

    let mut parameters = arguments.varargs.clone();
    parameters.insert(0, ("name".to_string(), arguments.project_name.clone()));
    let templater = Templater {
        path: template_path.as_path(),
        parameters,
    };
    let rendered_files = match templater.render() {
        Ok(files) => files,
        Err(error) => panic!(error),
    };

    let output_folder = match &arguments.output_folder {
        Some(folder) => Path::new(folder).join(&arguments.project_name),
        None => Path::new(&arguments.project_name).to_path_buf(),
    };

    if output_folder.exists() {
        println!(
            "Output folder {} already exists.",
            &output_folder.to_str().unwrap()
        );
    } else {
        println!("Creating directory and writing all the files.");
        fs::create_dir_all(output_folder.as_path());
        for (key, value) in &rendered_files {
            let output_file = output_folder.join(key);
            print!("Writing file: {}", &output_file.to_str().unwrap());
            fs::write(&output_file, value);
        }
    }
}

fn create_arguments<'a>() -> [Arg<'a>; 8] {
    let template_name = Arg::new("template_name")
        .required(true)
        .about("Name of the template.");

    let project_name = Arg::from("-n, --name=[PROJECT_NAME]")
        .about("Project name (and name of the created folder)")
        .required(true);

    let output_folder = Arg::from("-o, --output-folder=[OUTPUT_FOLDER]")
        .about("Output folder where the new project will be");

    let verbose = Arg::from("-v, --verbose")
        .multiple(true)
        .default_value("0")
        .about("Enable debug messages");

    let force_clean = Arg::from("-c, --clean").about("Force clean up of repository.");

    let repository = Arg::from("-r, --repository=[REPOSITORY]")
        .about("Repository (or folder) where templates are located.")
        .default_value("https://github.com/jdiazcano/laguna-templates.git");

    let no_clean =
        Arg::from("-C, --no-clean").about("Git repository will not be updated or cleaned up.");

    let varargs = Arg::new("inputs")
        .last(true)
        .validator(validate_input_args)
        .multiple(true);

    [
        template_name,
        repository,
        project_name,
        verbose,
        no_clean,
        force_clean,
        output_folder,
        varargs,
    ]
}

fn validate_input_args(val: &str) -> Result<(), String> {
    println!("{}", val);
    if !val.starts_with("--") {
        return Err(String::from(format!(
            "Error with '{}', arguments must start with --",
            val
        )));
    }
    return Ok(());
}
