#!/usr/bin/env bash

function check_dir () {
    if [ ! -d "$target_dir" ]; then
        echo "Invalid target directory"
        echo "Usage: ./graphviz-util <target_dir>"
        exit 1
    fi
    return
}

function check_dependency () {
    # Check for Linux dependencies.
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        if [ ! "$(command -v gxl2dot)" ]; then
            install_graphviz
        fi
    # Check for macOS dependencies.
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if [ ! "$(command -v gxl2dot)" ]; then
            if [ ! "$(command -v brew)" ]; then
                echo "Homebrew not installed. Please install from https://brew.sh/"
                exit 1
            else
                install_graphviz
            fi
        fi
    fi
    return
}

function install_graphviz () {
    read -p "Required dependency graphviz. Would you like to install it? (y/n): " yn
    case $yn in
        [Yy])
            if [[ "$OSTYPE" == "linux-gnu" ]]; then
                apt-get install graphviz
                return
            elif [[ "$OSTYPE" == "darwin"* ]]; then
                brew install graphviz
                return
            fi
            ;;
        [Nn]) 
            echo "Installation cancelled"
            exit 1
            ;;
        *) 
            install_graphviz
            ;;
    esac
    return
}

function select_mode() {
    echo "Mode 1 = gxl2dot, Mode 2 = dot2jpg, quit = exit program"
    read -p "Select mode: " mode
    if [ "$mode" == "quit" ]; then
        exit 0
    fi
    case $mode in
        [1])
            rm log1.txt
            if [ ! -d "./output_mode1" ]; then
                mkdir output_mode1
            else
                rm -r output_mode1
                mkdir output_mode1
            fi
            for filename in $target_dir/*.gxl; do
                gxl2dot "$filename" > "./output_mode1/"$(basename $filename .gxl)".dot" 2>> log1.txt
            done
            return
            ;;
        [2])
            rm log2.txt
            if [ ! -d "./output_mode2" ]; then
                mkdir output_mode2
            else
                rm -r output_mode2
                mkdir output_mode2
            fi
            for filename in $target_dir/*.dot; do
                dot -Tjpg "$filename" -o "./output_mode2/"$(basename $filename .dot)".jpg" 2>> log2.txt
            done
            return
            ;;
        *)
            select_mode
            ;;
    esac
}

echo "This is a tool used to convert a folder of graphs from .gxl format to .dot format or to create visualisation of graphs"
echo "If Linux and installation required please run this script with sudo"

# prevent errors from popping
touch log1.txt
touch log2.txt

target_dir=$1
check_dir
check_dependency
if [ ! $? ]; then
    echo "Installation of graphviz failed"
else
    select_mode
fi

