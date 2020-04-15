recap
=====
The `recap`<sup>†</sup> project is an ongoing effort to create a collection of lightly [skeuomorphic](https://en.wikipedia.org/wiki/Skeuomorph), reusable [reagent](https://github.com/reagent-project/reagent) components for building a UI of discrete parts loosely connected by shared state. It is currently being used - together with [rescope](https://github.com/kuhumcst/rescope) - to build the [tei-facsimile](https://github.com/kuhumcst/tei-facsimile) viewer.

_<sup>†</sup> Or more correctly, **ReCAP**, as it is an abbreviation of "**Re**usable / reactive / reagent **C**omponents for **A**cademic **P**rojects"._

Malleable user interfaces
-------------------------
Academic software is often developed for a specialised purpose and in a relatively short amount of time. End users of academic software typically have their own particular workflows, so it makes sense that the UI for such software can accommodate a wide range of needs. Furthermore, academic software is in a unique disposition:

* It must be maintained for much longer than most other production software.
* There is often little incentive/budget to enhance or extend the software after the point of delivery.

Anticipating both the current and future needs of end users, UIs built with `recap` get a high degree of customisability _"for free"_. Most of the components can be reordered or otherwise customised by the end user to fit their individual workflow. Knowing that fashion is fleeting, the designs of the components do not necessarily follow the trend of the day, but instead lean towards a more utilitarian, evergreen style.

Discrete components with shared state
-------------------------------------
The existing state containers of reagent are used directly in `recap`, which means ratoms, cursors, reactions (with `on-set` defined), and wrappers can all be used with `recap` components. You don't need any special functions or macros.

However, `recap` somewhat differentiates itself from other reagent UI component libraries (e.g. [recom](https://github.com/day8/re-com)) by not using callback functions. Instead, you generally call these components with a **reference** to a piece of state rather than the **dereferenced** value of that same piece of state. From the developer's perspective, the components may be considered a variation of [Form-2 components](https://github.com/reagent-project/reagent/blob/master/doc/CreatingReagentComponents.md#form-2--a-function-returning-a-function) where the inner state atom is injected by the developer as a function argument.

### Motivation
This UI library is my take on how to build an ecosystem of components that are simpler to reason about, less mechanical to connect, and more declarative overall.

I was motivated by the following considerations:

1. **Components should connect through state:** Reusable components<sup>†</sup> are simpler to extend if they have read-write access to the same state. The practice of hiding component state inside a function closure is detrimental to the composability of reusable components.
2. **Component state should be simple:** Both the component APIs and the underlying data will benefit from a design constraint that seeks to reuse state across different component types. The APIs benefit from less ceremony overall due to the de-emphasis on callback functions. The data benefits from an enforced generalisation.
3. **All state should be exposed:** Having access to most state through a single deref - as is common in the ClojureScript world - makes for simple debugging. If it's beneficial to expose the majority of the state why not expose state hidden away behind function closures too?

The Python principle of ["we are all consenting adults here"](https://mail.python.org/pipermail/tutor/2003-October/025932.html) is relevant here. This idea is also widely practiced in the Clojure/ClojureScript world, just not when it comes to stateful reagent components.

_<sup>†</sup> Of course, many "dumb" components are actually completely stateless. These are not the main concern of `recap`._

### Trade-offs
* The user is no longer required to write call back functions, but code that relies on side-effects of callback functions is now more complicated to write (e.g. maybe you need a wrapper or a watch function).
* The internal state of the reusable components is no longer encapsulated inside a function closure. The user should take care not to mess with the state by accident, for example by having components making incompatible changes to the same state.
* Function parameters cannot be destructured inline which obfuscates the component APIs somewhat. This is one key advantage of a more traditional approach.

### What about Re-frame?
[Re-frame](https://github.com/day8/re-frame) is a great idea that solves many hurdles by totally separating business logic from DOM mutation. Unfortunately, it doesn't have a good story when it comes to creating reusable components. In fact, the architecture that Re-frame imposes on the developer basically disincentivises components with any kind of internal component state. Re-frame strongly prefers to have all data transformations occur in `subscriptions` or as part of the state machine represented by the graph of Re-frame `events`. Components in Re-frame should be as dumb as possible and hook directly into the business logic by emitting events and dereferencing subscriptions.

Developers that want to use stateful components from a library such as `recap` can still do so in Re-frame (since it's just a thin layer on top of reagent), but doing so is slightly antithetical to Re-frame's overall design. On the other hand, Re-frame's concept of putting _all_ application state in a single place has similar benefits to exposing _individual_ component state, albeit in a more coupled and centralised way.

Accessibility included
----------------------
All the components in this library aspire to comply with the [EU Web Accessibility Directive](https://en.wikipedia.org/wiki/Web_Accessibility_Directive) and the relevant [Danish law](https://www.retsinformation.dk/Forms/r0710.aspx?id=201794). In practice, this means compliance with [WCAG 2.1](https://www.w3.org/TR/WCAG21/) which is a guideline from the World Wide Web Consortium.

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
