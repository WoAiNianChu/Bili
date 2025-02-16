name: Build 小条 EXE

on:
  push:
  # 手动触发
  workflow_dispatch:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  build:
    runs-on: windows-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up Python 3.8
      uses: actions/setup-python@v4
      with:
        python-version: 3.8

    - name: Install dependencies
      run: |
        python -m pip install --upgrade pip
        pip install pyinstaller openpyxl  # 确保 openpyxl 被安装

    - name: Create EXE with PyInstaller
      run: |
        pyinstaller --onefile --hidden-import=openpyxl 小条.py  # 确保 openpyxl 被打包

    - name: Upload EXE as artifact
      uses: actions/upload-artifact@v4
      with:
        name: excel-processor-exe
        path: ./dist/小条.exe
        retention-days: 7