#![cfg_attr(
    all(not(debug_assertions), target_os = "windows"),
    windows_subsystem = "windows"
)]

use regex::Regex;

#[tauri::command]
fn greet(enterValue: &str) -> String {
    let matches = Regex::new("^(\\-)?\\d+(\\.\\d+)$").unwrap();
    if matches.is_match(&enterValue) {
        let mut result = String::new();
        let mut split: Vec<String> = enterValue.split(".").map(str::to_string).collect();
        if enterValue.starts_with("-") {
            result.push('-');
            split[0].remove(0);
        }
        let mut flag = false; //0为整数部分，1为小数部分
        for ele in &split {
            let a: String = ele.chars().rev().collect();
            result.push_str(&a);
            if !flag {
                result.push('.');
                flag = true;
            }
        }
        return format!("整数小数翻转之后的是：{}", result);
    } else {
        return format!("这不是一个合法的十进制小数！");
    }
}

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![greet])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
