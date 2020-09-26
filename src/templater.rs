use std::collections::HashMap;
use std::path::Path;
use tera::{Context, Tera};
use walkdir::{DirEntry, WalkDir};

use crate::filters::register_all_filters;
use crate::renderer::{BinaryOutput, Output, TemplateOutput};
use content_inspector::ContentType;
use std::fs::File;
use std::io::Read;

pub struct Templater<'a> {
    pub path: &'a Path,
    pub parameters: Vec<(String, String)>,
}

fn dirs_filter(dir: &DirEntry) -> bool {
    dir.path().is_dir()
}

impl Templater<'_> {
    pub fn render(&self) -> Result<HashMap<String, Box<dyn Output>>, String> {
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
                    let content_type = find_content_type(&file);
                    let template_file = file.path().to_str().unwrap().strip_prefix(prefix);
                    match template_file {
                        Some(str) => {
                            let output: Box<dyn Output> = match content_type {
                                ContentType::BINARY => {
                                    let mut opened_file = File::open(file.path()).unwrap();
                                    let mut bytes: Vec<u8> = Vec::new();
                                    opened_file.read_to_end(&mut bytes);
                                    Box::new(BinaryOutput::new(bytes))
                                }
                                _ => match tera.render(str, &context) {
                                    Ok(rendered_text) => {
                                        Box::new(TemplateOutput::new(rendered_text))
                                    }
                                    Err(error) => {
                                        eprintln!("{}", error);
                                        return Err(String::from("Error in rendering."));
                                    }
                                },
                            };
                            rendered_files.insert(str.to_string(), output);
                        }
                        None => {}
                    }
                }
                Err(err) => {
                    eprintln!("{}", err);
                    return Err(String::from("Error opening the file."));
                }
            }
        }

        Ok(rendered_files)
    }
}

fn find_content_type(file: &DirEntry) -> ContentType {
    let opened_file = File::open(file.path()).unwrap();
    let mut peek_content = String::new();
    opened_file.take(1024).read_to_string(&mut peek_content);

    content_inspector::inspect(peek_content.as_ref())
}
