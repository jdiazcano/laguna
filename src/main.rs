mod git;
mod ocean;

use clap::{App, Arg, AppSettings};
use crate::ocean::OceanArgs;
use crate::git::Git;

fn main() {
    let template_name = Arg::with_name("template_name")
        .required(true)
        .about("Name of the template.");

    let project_name = Arg::from("-n, --name=[PROJECT_NAME]")
        .about("Project name (and name of the created folder)")
        .required(true);

    let verbose = Arg::from("-v, --verbose")
        .multiple(true)
        .default_value("0")
        .about("Enable debug messages");

    let force_clean = Arg::from("-c, --clean")
        .about("Force clean up of repository.");

    let repository = Arg::from("-r, --repository=[REPOSITORY]")
        .about("Repository (or folder) where templates are located.")
        .default_value("https://github.com/jdiazcano/laguna-templates.git");

    let no_clean = Arg::from("-C, --no-clean")
        .about("Git repository will not be updated or cleaned up.");

    let varargs = Arg::with_name("inputs")
        .last(true)
        .validator(validate_input_args)
        .multiple(true);

    let matches = App::new("laguna")
        .setting(AppSettings::TrailingVarArg)
        .args(&[
            template_name,
            repository,
            project_name,
            verbose,
            no_clean,
            force_clean,
            varargs
        ])
        .get_matches();

    let arguments = OceanArgs::from(matches);
    println!("{:?}", arguments);

    Git::prepare_repo(arguments);
}

fn validate_input_args(val: &str) -> Result<(), String> {
    println!("{}", val);
    if !val.starts_with("--") {
        return Err(String::from(format!("Error with '{}', arguments must start with --", val)))
    }
    return Ok(())
}