use std::collections::HashMap;
use std::path::Path;
use tera::{Context, Tera};
use walkdir::{DirEntry, WalkDir};

use crate::filters::register_all_filters;
use crate::renderer::{BinaryOutput, Output, TemplateOutput};
use content_inspector::ContentType;
use std::fs;
use std::fs::File;
use std::io::Read;

pub struct Templater<'a> {
    pub path: &'a Path,
    pub parameters: Vec<(String, String)>,
}

fn exclude_directories(dir: &DirEntry) -> bool {
    !dir.path().is_dir()
}

impl Templater<'_> {
    pub fn render(&self) -> Result<HashMap<String, Box<dyn Output>>, String> {
        let str_path = self.path.to_str().unwrap();
        let _glob = format!("{}/**/*", str_path);
        let mut tera = Tera::default();
        register_all_filters(&mut tera);
        let mut context = Context::new();
        let walker = WalkDir::new(self.path).into_iter();

        for parameter in &self.parameters {
            context.insert(&parameter.0, &parameter.1);
        }
        let prefix = &format!("{}/", str_path);
        let mut rendered_files = HashMap::new();
        for file in walker {
            match file {
                Ok(file) => {
                    if file.path().is_dir() {
                        continue;
                    }

                    let content_type = find_content_type(&file.path());
                    println!("Type of {:?} is {:?}", &file.path(), content_type);
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
                                _ => {
                                    let file_contents = fs::read_to_string(file.path())
                                        .expect(format!("Could not read file").as_str());
                                    tera.render_str(&file_contents, &context);
                                    tera.add_template_file(file.path(), Some("a"));
                                    match tera.render("a", &context) {
                                        Ok(rendered_text) => {
                                            Box::new(TemplateOutput::new(rendered_text))
                                        }
                                        Err(error) => {
                                            eprintln!("{}", error);
                                            return Err(String::from("Error in rendering."));
                                        }
                                    }
                                }
                            };
                            let rendered_file_name = tera.render_str(str, &context);
                            rendered_files.insert(rendered_file_name.unwrap(), output);
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

pub fn find_content_type(file: &Path) -> ContentType {
    let content = fs::read(file);

    content_inspector::inspect(content.unwrap().as_ref())
}
