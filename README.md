Stucco
======
The Stucco<sup>[†](#note-name)</sup> library is an effort to foster [rapid application development](https://en.wikipedia.org/wiki/Rapid_application_development) through a collection of accessible,  adaptive [reagent](https://github.com/reagent-project/reagent) user interface components.

The library is currently being used - together with [rescope](https://github.com/kuhumcst/rescope) - to build the [glossematics](https://github.com/kuhumcst/glossematics) archival website as part of the _"Infrastrukturalisme"_ project, a joint effort between the University of Copenhagen and Aarhus University.

> <a name="note-name"><sup>†</sup></a> Stucco is a [construction material](https://en.wikipedia.org/wiki/Stucco) made of reagent components. It is used as a decorative coating for Clojure data and allows end users to participate in sculpturing the user interface.

Overview
--------
<a href="https://youtu.be/ibiK8sgwvqc"><img align="right" width="60%" src="doc/carousel-and-tabs.png"></a>

This is an ongoing experiment in user interface adaptability using just reagent components and a simple underlying data model. There are three separate component types to consider:

* `dk.cst.stucco.surface`: Surface components are comparable to typical reusable components found in libraries such as [re-com](https://github.com/day8/re-com). The primary purpose of a Surface component is to provide a basic level of interactivity expected for simple pieces of data. An example might be the `illustration` component which can be used in place of the HTML `[:img]` element.
* `dk.cst.stucco.plastic`: Plastic components are more complex than Surface components and reflect the mutability of their underlying state. In practice, this mutability is realised as universal drag-and-drop between Plastic components with compatible data. An example might be the `tabs` or `carousel` components which both visualise a collection of key-value pairs according to an index.
* `dk.cst.stucco.foundation`: Foundation components are used to realise a user interface declaratively. Rather than specifying their precise locations, Stucco components are partitioned semantically into Foundation sections. These sections are then positioned on the web page according to an archetypical layout. Like the Plastic components, this layout can also be changed by the end user, but at a higher level of abstraction.

The components help construct a user interface that can adapt to various end user requirements without resulting in configurations that are too strange. When combined, Stucco components also compose into highly accessible web applications. Please see the document detailing the overall [vision](doc/vision.md) for more.

Development prerequisites
-------------------------
The development workflow of the project itself is built around the [Clojure CLI](https://clojure.org/reference/deps_and_cli) for managing dependencies and [shadow-cljs](https://github.com/thheller/shadow-cljs) for compiling ClojureScript code and providing a live-reloading development environment.

In this project, the dependency management feature of shadow-cljs is not used directly. Rather, I leverage the built-in support in shadow-cljs for the Clojure CLI/deps.edn to download dependencies and build a classpath.

I personally use IntelliJ with the Cursive plugin which [integrates quite well with the Clojure CLI](https://cursive-ide.com/userguide/deps.html).

### macOS setup
(assuming [homebrew](https://brew.sh/) has already been installed)


I'm not sure which JDK version you need, but anything 8+ is probably fine! I personally just use the latest from AdoptOpenJDK (currently JDK 13):

```
brew cask install adoptopenjdk
```

The following will get you the Clojure CLI and shadow-cljs, along with NodeJS:

```
brew install clojure/tools/clojure
brew install node
npm install -g shadow-cljs
```

Workflow
--------
Development of the project is done using the live-reloading capabilities of shadow-cljs:

```
shadow-cljs watch app
```

This will start a basic web server at `localhost:7000` serving the `:app` build as specified in the `shadow-cljs.edn` file.

It's possible to execute unit tests while developing by also specifying the `:test` build:

```
shadow-cljs watch app test
```

This will make test output available at `localhost:7100`. It's quite convenient to keep a separate browser tab open just for this. The favicon will be coloured green or red depending on the state of the assertions.

Personally, I use the Clojure CLI integration in Cursive to calculate a classpath and download dependencies. Something like this command is being executed behind the scenes:

```
clj -A:app:test -Spath
```

I have also set up some aliases in my personal [~/.clojure/deps.edn](https://github.com/simongray/dotfiles/blob/master/dot/clojure/deps.edn) file to perform certain common tasks such as listing/updating outdated packages:

```
clj -A:outdated
clj -A:update
```
