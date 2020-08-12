recap
=====
The `recap`<sup>[†](#note-name)</sup> library is an effort to foster [rapid application development](https://en.wikipedia.org/wiki/Rapid_application_development) through a collection of accessible, adaptive [reagent](https://github.com/reagent-project/reagent) components that communicate dynamically using state itself as a protocol. This should yield a UI that is simple to reason about, less mechanical to connect, and more declarative overall.

The library is currently being used - together with [rescope](https://github.com/kuhumcst/rescope) - to build the [tei-facsimile](https://github.com/kuhumcst/tei-facsimile) viewer as part of the _"Infrastrukturalisme"_ project, a joint effort between the University of Copenhagen and Aarhus University.

> _<a name="note-name"><sup>†</sup></a> Or more correctly, **ReCAP**, as it is an abbreviation of "**Re**agent **C**omponents for **A**cademic **P**rojects"._

Adaptive user interfaces
------------------------
End users of academic software typically have very specialised workflows, so it makes sense that the UI for such software can accommodate a wide range of needs. Furthermore, academic software is in a unique disposition:

* It is usually developed in relatively few man-hours.
* It must be maintained for much longer than most other software.
* There is often little incentive/budget to further develop the software past the point of delivery.

Anticipating the varied needs of both current and future end users, UIs built with `recap` aim for a high degree of adaptability:

1. **Components should be tangible:** Most of the components can be reordered, resized, or otherwise customised by the end user to fit their individual workflow.
2. **Components should be contextual:** Many components will visually connect with nearby elements by morphing their styling accordingly.
3. **Devices are different:** The components are responsive and will adapt to different screen sizes. Alternative input methods like keyboard and touch gestures are also supported.

The library focuses _specifically_ on frontend development for data-centric systems. While it is highly modular, **no special effort** has been made to support a custom branded experience. Knowing that fashion is fleeting, the default designs of the components do not necessarily follow all the trends of the day, but rather lean towards a more utilitarian, evergreen style. Whenever a choice had to be made, form always yields to function.

Accessibility semantics
-----------------------
Full accessibility is _really_ hard to get right in web applications. The aim of this library is to have any UI built with it be broadly accessible. To this effect, the components mostly follow the [WAI-ARIA Authoring Practices](https://www.w3.org/TR/wai-aria-practices-1.1/):

1. **Markup should be semantic:** Semantic HTML is used throughout (when possible) and appropriate ARIA attributes are set whenever they're required.
2. **Keyboard input must be equal** Every component can be operated in a predictable way when using a keyboard instead of a mouse (also to the benefit of keyboard power users).
3. **Layout should be declarative:** ARIA [Landmark Regions](https://www.w3.org/TR/wai-aria-practices-1.1/#aria_landmark) are used as the basis of layouts in `recap`, making the process entirely declarative whilst coercing an accessible HTML structure.
    - This allows for precise control of the tab order by ensuring that the DOM order corresponds to the visual layout presented to the end user.
    - Focusing purely on semantics also allows the developer (or the end user) to hot swap the basic layout for a different one.
4. **Terminology should be clear:** The component names try to match the official W3C terminology as much as possible, e.g. the `tabs` component consists of a `tab-list` and the currently selected `tab-panel`. The deliberate use of well-established names aids developers using this library.

For these reasons, accessibility should not be seen as an obstacle to overcome, but rather as a set of semantic constraints that work in synergy with the goal of creating an adaptive UI.

All the components in the library should - in principle - comply with the [EU Web Accessibility Directive](https://en.wikipedia.org/wiki/Web_Accessibility_Directive) and the relevant [Danish law](https://www.retsinformation.dk/Forms/r0710.aspx?id=201794). In practice, this means compliance with [WCAG 2.1](https://www.w3.org/TR/WCAG21/) which is the current guideline from the World Wide Web Consortium. Only the contextual parts that _cannot_ be automatically deduced are made the responsibility of the developer. Runtime assertions serve as a helpful enforcement mechanism during development.

Shared component state
----------------------
In more typical UI libraries (e.g. [recom](https://github.com/day8/re-com)), you construct stateful components from one or more values - usually from **dereferenced** state - along with associated callback functions for handling mutation. Components then create a closure around their mutable inner state and (sometimes) exchange data with other components using callbacks functions, though only to the extent that the developer has explicitly defined. 

In `recap`, you generally construct stateful components with a **reference** to a piece of state and _no_ callbacks. From the developer's perspective, the components may be considered a variation of [Form-2 components](https://github.com/reagent-project/reagent/blob/master/doc/CreatingReagentComponents.md#form-2--a-function-returning-a-function) where the developer injects the inner state as a function argument. Reagent's RAtoms, RCursors, Reactions (with `on-set` defined), and Wrappers can all be used directly with the UI components of this library. You don't need any special functions or macros. 

To facilitate component integration, the shape of the injected state is very generic, enabling many UI components to accept the same state. Most callback functions are unnecessary when components have this kind of direct access to shared state. And as with the accessibility enforcement mentioned above, runtime assertions continuously validate component state during development.

### Mutable interfaces: an emergent advantage
Recap features extensive drag-and-drop of interface elements. One example of this might be dragging a tab somewhere else, e.g. to another tabbed interface on the same page. When injecting mutable state - such as RAtoms - into a Recap component, the RAtoms can in fact survive a component being removed from the page and re-rendered somewhere else on the same page. The approach to state facilitates the 
adaptive interface.

In this way, complex interface elements composed of stateful reagent components are able to _preserve_ their state while being moved around on the page. The only additional code needed is wrapping the initial state with `(r/atom ...)`. You do not even need to keep a reference to the state. The implication here is that you can still inline the input args if you don't care in particular about inspecting the state.

It's possible to initialise Recap components using plain data too. The components behave like typical, stateful reagent components, creating a temporary (fully internal) RAtom to contain the component state. However, using the components in this way does not allow for the above-mentioned advantage.

### Motivation
I was motivated by the following considerations:

1. **Components should connect through state:** Stateful components<sup>[†](#note-state)</sup> are simpler to integrate if they are allowed read-write access to the same state. The component APIs benefit from reduced ceremony due to a de-emphasis on callback functions.
2. **Component state should be generic:** There are many benefits to having similarly shaped state in use across different component types. It imposes simplicity and a separation of concerns, while facilitating adhoc communication between discrete UI components by using the data itself as a protocol.
3. **All state should be exposed:** Having access to most state through a single deref - as is common in the ClojureScript world - makes for simple debugging. If it's beneficial to expose the majority of the state why not expose state hidden away behind function closures too?

The Python principle of ["we are all consenting adults here"](https://mail.python.org/pipermail/tutor/2003-October/025932.html) is relevant here. This idea is also widely practiced in the Clojure/ClojureScript world, just not when it comes to stateful reagent components.

> _<a name="note-state"><sup>†</sup></a> Of course, many "dumb" components are actually completely stateless. These are not the main concern of `recap`._

### Trade-offs
* Using the library in an opinionated frontend architecture like Re-frame<sup>[†](#note-re-frame)</sup> is slightly less ergonomic.
* The user is no longer required to write call back functions, but code that relies on side-effects of callback functions is now more complicated to write (e.g. maybe you need a wrapper or a watch function).
* The internal state of the stateful components is no longer encapsulated inside a function closure. The user should take care not to mess with the state by accident, for example by having components making incompatible changes to the same state.
* Function parameters cannot be destructured inline which obfuscates the component APIs somewhat. This is one key advantage of a more traditional approach.

> _<a name="note-re-frame"><sup>†</sup></a> [Re-frame](https://github.com/day8/re-frame) is a great idea that solves many hurdles by attempting to totally separate business logic from DOM mutation. Unfortunately, it doesn't have a good story when it comes to creating stateful components using its core abstractions._
>
> _In fact, the architecture that Re-frame imposes on the developer basically disincentivises making components with internal state. Re-frame strongly prefers to have all data transformations occur in `subscriptions` or as part of the state machine represented by the graph of Re-frame `events`. Components in Re-frame should be as dumb as possible and hook directly into the business logic by emitting events and dereferencing subscriptions._
>
> _Developers that want to use stateful components from a library such as `recap` can still do so in Re-frame (since it's just a layer on top of reagent), but doing so is slightly antithetical to Re-frame's overall design._

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
