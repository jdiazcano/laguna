use clap::ArgMatches;

struct Ocean;
impl Ocean {
    fn exec(&self, arguments: &OceanArgs) {}
}

#[derive(Debug)]
pub struct OceanArgs {
    pub template_name: String,
    pub project_name: String,
    pub verbose: u64,
    pub force_clean: bool,
    pub repository: String,
    pub no_clean: bool,
    pub varargs: Vec<(String, String)>,
    pub output_folder: Option<String>,
}

impl From<ArgMatches> for OceanArgs {
    fn from(matches: ArgMatches) -> Self {
        return OceanArgs {
            template_name: matches.value_of("template_name").unwrap().to_string(),
            project_name: matches.value_of("name").unwrap().to_string(),
            verbose: matches.occurrences_of("verbose"),
            force_clean: matches.is_present("clean"),
            repository: matches.value_of("repository").unwrap().to_string(),
            no_clean: matches.is_present("no-clean"),
            output_folder: matches.value_of("output-folder").map(|e| e.to_string()),
            varargs: matches
                .values_of("inputs")
                .unwrap_or_default()
                .map(|value| value.to_string())
                .map(|value| value.split("=").map(|e| e.to_string()).collect::<Vec<_>>())
                .map(|args: Vec<String>| {
                    (
                        args[0].strip_prefix("--").unwrap().to_string(),
                        args[1].to_string(),
                    )
                })
                .collect::<Vec<_>>(),
        };
    }
}
