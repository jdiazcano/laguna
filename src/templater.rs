use tera::{Tera, Context};
use walkdir::{WalkDir, DirEntry};
use std::path::{Path};
use std::collections::HashMap;

use crate::filters::{register_all_filters};

pub struct Templater<'a> {
    pub path: &'a Path,
    pub parameters: Vec<(String, String)>,
}

fn dirs_filter(dir: &DirEntry) -> bool {
    dir.path().is_dir()
}

impl Templater<'_> {
    pub fn render(&self) -> Result<HashMap<String, String>, String> {
        let str_path = self.path.to_str().unwrap();
        let glob = format!("{}/**/*", str_path);
        let mut tera = Tera::new(&glob).unwrap();
        register_all_filters(&mut tera);
        let walker = WalkDir::new(self.path).into_iter();

        let mut context = Context::new();
        for parameter in &self.parameters {
            context.insert(&parameter.0, &parameter.1);
        }
        let prefix = &format!("{}/", str_path);
        let mut rendered_files = HashMap::new();
        for file in walker {
            match file {
                Ok(file) => {
                    let template_file = file.path().to_str().unwrap().strip_prefix(prefix);
                    match template_file {
                        Some(str) => {
                            match tera.render(str, &context) {
                                Ok(rendered_text) => {
                                    rendered_files.insert(str.to_string(), rendered_text)
                                },
                                Err(error) => {
                                    eprintln!("{}", error);
                                    return Err(String::from("Error in rendering."))
                                }
                            };
                        },
                        None => {}
                    }
                },
                Err(err) => {
                    eprintln!("{}", err);
                    return Err(String::from("Error opening the file."))
                }
            }
        }

        Ok(rendered_files)
    }
}