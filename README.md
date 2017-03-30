# vashet

A ClojureScript wrapper for Robin Frischmann's css-in-js library fela.

```clojure
[vashet "0.0.1"]
```

## Overview

The primary object of this project is provide the ability to leverage fela.js from ClojureScript without having to worry about interop and coercing data into JavaScript object types. Most of the methods are just kebab-case versions of fela's api that accept ClojureScript data types, however there are also some extra methods meant to provide some additional functionality.

## Usage

```clojure
(ns application.core
  (:require [vashet.core :as styles]))

;; instantiate a renderer that will apply styles to a DOM node
(styles/create-renderer)

;; style rules are just a function of props that return a styles map
;; new styles will be generated when props change
(defn rule
  [props]
  {:color (:color props)
   :padding (case (:padding props)
              :large "20px"
              :med   "10px"
              :small "5px"
              0)
   :font-size "16px"
   ":hover" {:color "black"
             :transform "translateY(-" (:shiftY props) ")"}
   "@media (min-height 410px)" {:margin "10px 0"}})

;; rendering a rule returns a class name that can be applied to any element
(def class (styles/render-rule
             rule
             {:color "red"
              :padding :medium
              :shiftY "10%"}))

;; it uses atomic css design to reuse styles on declaration base and to keep the markup minimal
(println class) ;; => "a b c d e"  

;; renders all styles into the DOM
(styles/init-styles (.getElementById js/document "style-node"))
```

## API

### (create-renderer [& [config]])
instantiate the fela renderer with an optional config object
#### config
type: `map`

### (init-styles node)
renders all styles into specified node
#### node
*Required*
type: `DOM-node`

### (render-rule rule props)
invokes the rule function with props, applies the resulting styles to the style node, and returns corresponding class names
#### rule
*Required*
type: `fn`
#### props
type: `map`

### (render-keyframe keyframe props)
invokes the keyframe function with props, applies the resulting styles to the style node, and returns corresponding keyframe name
#### keyframe
*Required*
type: `fn`
#### props
type: `map`

```clojure
(defn my-keyframe
  [props]
  {:0% {:opacity (:start props)}
   :100% {:opacity (:end props)}})

(render-keyframe my-keyframe {:start 0 :end 1})
```

### (render-font family files props)
Adds a font-face to the style node
#### family
*Required*
type: `string`
#### files
*Required*
type: `collection`
#### props
type: `map`

```clojure
(def files ["./fonts/Lato.ttf" "./fonts/Lato.woff"])

(render-font "Lato" files)
(render-font "Lato-bold" files {:font-weight "bold"})
```

### (render-static styles selectors)
applies static styles for the provided selectors
#### styles
*Required*
type: `map`
#### selectors
*Required*
type: `collection`

```clojure
(def header-global
  []
  {:font-weight "bold"
   :font-family "Open Sans"
   :color "blue"})

(def container-global
  []
  {"@media (min-height 410px)" {:margin "10px 20px"}
   "@media (min-height 660px)" {:margin "10px 30px"}
   "@media (min-height 980px)" {:margin "10px 50px"}})

(render-static header-global [:h1 :h2 :h3])
(render-static container-global [".container"])
```

### (combine-rules & rules)
accepts a collection of rule functions and merges their result; functions appearing later in the collection overwrite existing properties
#### rules
*Required*
type: `collection`

```clojure
(defn rule-one
  [props]
  {:color (:color props)
   :font-weight (:weight props)
   :font-size "16px"})

(defn rule-two
  [props]
  {:font-family (:font props)
   :font-size "14px"})

(def combined-rule (combine-rules [rule-one rule-two]))

(println (combined-rule {:color "red" :weight "bold" :font "monospace"}))
;; => {:color "red" :font-weight "bold" :font-size "14px" :font-family "monospace"}

(render-rule combined-rule {:color "red" :weight "bold" :font "monospace"})
```

### (build-animation & {:keys [duration timing-fn delay count direction keyframe props]})
accepts optional css animation key-value pairs, a 'keyframe' key-value pair, and 'props' key-value pair then applies the keyframe to the style node and returns the animation string
#### duration
*Required*
type: `string`
#### timing-fn
type: `string`
#### delay
type: `string`
#### count
type: `string/number`
#### direction
type: `string`
#### keyframe
*Required*
type: `fn`
#### props
type: `map`

```clojure
(defn pulse
  [props]
  {:0% {:opacity 1
        :transform "scale(1,1)"}
   :100% {:opacity 0.4
          :transform (str "scale(" (:scaleX props) "," (:scaleY props) ")")}})

(def pulse-animation (build-animation
                       :duration "3s"
                       :timing-fn "ease-in"
                       :count "infinite"
                       :direction "alternate"
                       :keyframe pulse
                       :props {:scaleX 2 :scaleY 2}))

(def rule
  [props]
  {:background-color "black"
   ":hover" {:background-color "gray"}
   :animation (:animation props)})

(render-rule rule {:animation pulse-animation})
```

### (render-styles & {:keys [rule props add-class]})
works like render-rule but accepts key-value pairs and allows for optionally passing in a collection of regular css classes
#### rule
*Required*
type: `fn`
#### props
type: `map`
#### add-class
type: `collection`

```clojure
(defn rule
  [props]
  {:color (:color props)
   :font-family "Gill Sans Light"})

(def hybrid-class-string (render-styles
                           :rule rule
                           :props {:color "green"}
                           :add-class [:regular-class "another-class"]))

(println hybrid-class-string) ;; => "a b regular-class another-class"
```

### (render-to-string)
returns all styles in a css string

```clojure
(render-rule #(merge {:color nil} %) {:color "yellow"})

(println (render-to-string)) ;; => ".a{color:yellow}"
```

### (subscribe-to-styles cbfn)
listens to style changes and invokes the callback function with an info map when new styles are added
#### cbfn
*Required*
type: `fn`

### (clear-styles)
removes all styles from the targeted style node

## License

Copyright © 2017 Christopher Howard

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
