[![Tests](https://github.com/poseidon-fisheries/poseidon-gui/actions/workflows/gradle.yml/badge.svg)](https://github.com/poseidon-fisheries/poseidon-gui/actions/workflows/gradle.yml)

# poseidon-gui

*POSEIDON's classic Java Swing GUI, now in it's very own repository*

We are in the process of making POSEIDON more modular so we can distribute only the bits relevant to a particular project.

This bit is the GUI, which allows you to run pre-defined POSEIDON scenarios and visually interact with one simulation as it is happening.

To build it from a bash terminal, it should be sufficient to:

```
git clone git@github.com:poseidon-fisheries/poseidon-gui.git
cd poseidon-gui
git submodule update --init
./gradlew build
```

You should then be able to run it with:

```
./gradlew run
```

Note that this repository depends on the core https://github.com/poseidon-fisheries/POSEIDON repository.

## About POSEIDON

A good run-down of what it is and does is available here:
http://carrknight.github.io/poseidon/appendix.html

A full fledged tutorial for this project is available here:
http://carrknight.github.io/poseidon/tutorial/index.html
