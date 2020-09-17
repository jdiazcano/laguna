use crate::errorcode::ErrorCode;
use tera::{Tera, Context};
use walkdir::{WalkDir, DirEntry};
use std::fs::{create_dir, create_dir_all};
use std::path::{Path, Display};

pub struct Templater<'a> {
    pub(crate) path: &'a Path
}

fn dirs_filter(dir: &DirEntry) -> bool {
    dir.path().is_dir()
}

impl Templater<'_> {
    pub fn render(&self) -> Result<(), ErrorCode> {
        let str_path = self.path.to_str().unwrap();
        let glob = format!("{}/**/*", str_path);
        let mut tera = Tera::new(&glob).unwrap();
        let walker = WalkDir::new(self.path).into_iter();

        let context = Context::new();
        let prefix = &format!("{}/", str_path);
        for file in walker {

            match file {
                Ok(file) => {
                    let template_file = file.path().to_str().unwrap().strip_prefix(prefix);
                    match template_file {
                        Some(str) => {
                            println!("Template file: {}", str);
                            let str = tera.render(str, &context);
                            println!("{}", str.unwrap());
                        },
                        None => {}
                    }
                },
                Err(err) => return Err(ErrorCode::ErrorRendering)
            }
        }

        Ok(())
    }
}