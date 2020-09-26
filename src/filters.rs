use tera::Tera;

pub fn register_all_filters(tera: &mut Tera) {
    tera.register_filter("mixed_case", filters::mixed_case);
    tera.register_filter("camel_case", filters::camel_case);
    tera.register_filter("snake_case", filters::snake_case);
}

mod filters {
    use heck::{CamelCase, MixedCase, SnakeCase};
    use std::collections::HashMap;
    use tera::{to_value, try_get_value, Error, Value};

    pub fn camel_case(value: &Value, _: &HashMap<String, Value>) -> Result<Value, Error> {
        let s = try_get_value!("camel_case", "value", String, value);

        Ok(to_value(&s.to_camel_case()).unwrap())
    }

    pub fn snake_case(value: &Value, _: &HashMap<String, Value>) -> Result<Value, Error> {
        let s = try_get_value!("camel_case", "value", String, value);

        Ok(to_value(&s.to_snake_case()).unwrap())
    }

    pub fn mixed_case(value: &Value, _: &HashMap<String, Value>) -> Result<Value, Error> {
        let s = try_get_value!("camel_case", "value", String, value);

        Ok(to_value(&s.to_mixed_case()).unwrap())
    }
}
