recap
=====
... or more correctly: **ReCAP**, as it is an abbreviation of _**Re**usable / reactive / reagent **C**omponents for **A**cademic **P**rojects_.

The `recap` project is an ongoing effort to build a collection of lightly [skeuomorphic](https://en.wikipedia.org/wiki/Skeuomorph), reusable [reagent](https://github.com/reagent-project/reagent) components for building a UI of discrete parts loosely connected by shared state.

It is currently being used - together with [rescope](https://github.com/kuhumcst/rescope) - to build the [tei-facsimile](https://github.com/kuhumcst/tei-facsimile) viewer.

Shared state without callbacks
-----------------------------
The state containers of reagent are all used _as-is_ in `recap`, which means ratoms, cursors, reactions (with `on-set` defined), and wrappers are all valid state containers for recap components.

However, `recap` somewhat differentiates itself from other reagent UI component libraries (e.g. [recom](https://github.com/day8/re-com)) by not using callback functions. Instead, `recap` components are generally called using a **reference** to a piece of state rather than the **dereferenced** value of that same piece of state.

In practice, this means that the components can be considered a variation of [Form-2 components](https://github.com/reagent-project/reagent/blob/master/doc/CreatingReagentComponents.md#form-2--a-function-returning-a-function) where the inner state atom is actually an external piece of state.

### Motivation and trade-offs
This is the motivation behind the choice:

1. The **primary** motivation  was to set up a design restraint that would make the state data itself simpler and more re-usable across components.
2. A **secondary** objective was to make it simple to extend reusable components by connecting them with other components by giving read/write access to the same state. This is not always possible using components based on internal state atoms.
3. **Thirdly**, when using a single state atom model to represent all state - as is common in the ClojureScript world - it makes for simpler debugging if the internal state of components is _fully_ exposed rather than hidden away.

This is of course a trade-off:

* The user is now no longer required to write call back functions, but code that relies on side-effects of callback functions is now more complicated to write (e.g. maybe you need a wrapper or a watch function).
* The internal state of the reusable components is no longer encapsulated inside a function closure, so the user should take care not to mess with the state by accident.

**TL;DR**: _component state should be simple, components should connect through state_, and ["we are all consenting adults here"](https://mail.python.org/pipermail/tutor/2003-October/025932.html).

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
shadow-cljs watch dev
```

This will start a basic web server at `localhost:7000` serving the `:dev` build as specified in the `shadow-cljs.edn` file.

It's possible to execute unit tests while developing by also specifying the `:test` build:

```
shadow-cljs watch dev test
```

This will make test output available at `localhost:7100`. It's quite convenient to keep a separate browser tab open just for this. The favicon will be coloured green or red depending on the state of the assertions.

Personally, I use the Clojure CLI integration in Cursive to calculate a classpath and download dependencies. Something like this command is being executed behind the scenes:

```
clj -A:dev:test -Spath
```

I have also set up some aliases in my personal [~/.clojure/deps.edn](https://github.com/simongray/dotfiles/blob/master/dot/clojure/deps.edn) file to perform certain common tasks such as listing/updating outdated packages:

```
clj -A:outdated
clj -A:update
```
