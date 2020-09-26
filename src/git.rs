use crate::ocean::OceanArgs;
use dialoguer::{Input, Password};
use git2::build::{CheckoutBuilder, RepoBuilder};
use git2::{Cred, ErrorCode, FetchOptions, Reference, RemoteCallbacks, Repository};
use std::path::{Path, PathBuf};
use url::Url;

pub struct Git;

impl Git {
    pub fn prepare_repo<'a>(arguments: &OceanArgs) -> Result<PathBuf, ErrorCode> {
        let repository = match Url::parse(&arguments.repository) {
            Ok(_url) => prepare_url(&arguments.repository),
            Err(_e) => prepare_path(&arguments.repository),
        };
        let reference = match repository.head() {
            Ok(reference) => reference,
            Err(error) => return Err(error.code()),
        };
        Git::clean_and_update_repository(&repository, reference);
        let x = repository.path().to_owned();
        Ok(x)
    }

    fn clean_and_update_repository(repository: &Repository, reference: Reference) {
        let mut remote = repository.find_remote("origin").unwrap();
        let mut opts = Git::fetch_opts();
        remote.fetch(&["master"], Some(&mut opts), None);

        let treeish = repository.revparse_single("origin/master").unwrap();
        let mut checkout_builder = CheckoutBuilder::new();
        let checkout_builder = checkout_builder.force();
        repository.checkout_tree(&treeish, Some(checkout_builder));
        repository
            .reference(
                reference.name().unwrap(),
                reference.target().unwrap(),
                true,
                "log",
            )
            .unwrap()
            .set_target(treeish.id(), "");
    }

    fn callback<'a>() -> RemoteCallbacks<'a> {
        let mut callbacks = RemoteCallbacks::default();
        callbacks.credentials(|_url, username_from_url, allowed_types| {
            let username: String = match username_from_url {
                Some(user) => user.to_string(),
                None => Input::new().with_prompt("Username").interact().unwrap(),
            };

            if allowed_types.is_user_pass_plaintext() {
                let password = Password::new().with_prompt("Password").interact().unwrap();
                Cred::userpass_plaintext(&username, &password)
            } else if allowed_types.is_ssh_key() {
                Cred::ssh_key_from_agent(&username)
            } else if allowed_types.is_username() {
                Cred::username(&username)
            } else {
                panic!("Unknown type of credentials.")
            }
        });

        callbacks
    }

    fn fetch_opts<'a>() -> FetchOptions<'a> {
        let mut opts = FetchOptions::new();
        let callback = Git::callback();
        opts.remote_callbacks(callback);
        opts
    }

    fn init(path: String) -> Repository {
        return match Repository::open(path) {
            Ok(repo) => repo,
            Err(e) => panic!("Failed to init repository: {}", e),
        };
    }
}

fn prepare_url(url: &String) -> Repository {
    let repository_name: String = match url.rfind('/') {
        Some(position) => url
            .chars()
            .skip(position + 1)
            .take(url.len() - position - 1 - 4)
            .collect(),
        None => panic!("Repository name not found in '{}'", url),
    };

    let path = Path::new("/tmp/").join(repository_name);
    let path_string = String::from(path.to_str().unwrap());

    return if path.exists() {
        prepare_path(&path_string)
    } else {
        let options = Git::fetch_opts();
        match RepoBuilder::new()
            .fetch_options(options)
            .clone(url, path.as_ref())
        {
            Ok(repo) => repo,
            Err(_e) => panic!(
                "Could not clone repository '{}' into '{}'",
                url, &path_string
            ),
        }
    };
}

fn prepare_path(path: &String) -> Repository {
    return match Repository::open(path) {
        Ok(repo) => repo,
        Err(e) => {
            eprintln!("{}", e);
            panic!("Could not open repository: {}", path)
        }
    };
}
