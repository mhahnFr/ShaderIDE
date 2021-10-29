# Welcome to the Shader IDE!
This repository contains a project I made when experimenting with OpenGL ES on
Android. It is designed to generate a file in my own format containing some
useful informations read from the source code as well as the source code
itself.

## Idea
The initial idea behind this project was to generate precompiled GLSL shaders.
As that is something impossible, I tried to create an IDE, which should be able
to compile shaders and check for syntax errors. Informations read from the
source code should be stored in a special file format, so that shaders could be
integrated more dynamically in the application.

## GUI design
The design is kept quite simple: there is a window showing the source code of a
shader. All possible actions are accessible via the menubar.

## Approach

© 2017 [mhahnFr](https://www.github.com/mhahnFr)
