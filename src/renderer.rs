use std::fs;
use std::path::PathBuf;

pub trait Output {
    fn render(&self, file: PathBuf);
}

pub struct BinaryOutput {
    content: Vec<u8>,
}

impl BinaryOutput {
    pub fn new(content: Vec<u8>) -> Self {
        BinaryOutput { content }
    }
}

impl Output for BinaryOutput {
    fn render(&self, file: PathBuf) {
        fs::write(&file, &self.content);
    }
}

pub struct TemplateOutput {
    content: String,
}

impl TemplateOutput {
    pub fn new(content: String) -> Self {
        TemplateOutput { content }
    }
}

impl Output for TemplateOutput {
    fn render(&self, file: PathBuf) {
        fs::write(&file, &self.content);
    }
}
