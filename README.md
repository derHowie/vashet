# vashet

A ClojureScript wrapper for Robin Frischmann's css-in-js library [fela](https://github.com/rofrischmann/fela/).

[![Clojars Project](https://img.shields.io/clojars/v/vashet.svg)](https://clojars.org/vashet)[![Build Status](https://travis-ci.org/derHowie/vashet.svg?branch=master)](https://travis-ci.org/derHowie/vashet)

> “This is the nature of love." Vashet said. "To attempt to describe it will drive a woman mad. This is what keeps poets scribbling endlessly away. If one could pin it to paper all complete, the others would lay down their pens. But it cannot be done.”

## Overview

The primary objective of this project is provide the ability to leverage fela.js from ClojureScript without having to worry about interop and JavaScript object types. Most of the methods are just kebab-case versions of fela's api that accept ClojureScript data types. However fela's 'Renderer' class has been abstracted away and there are some additional utility methods.

## Usage

```clojure
(ns application.core
  (:require [vashet.core :as styles]))

;; create a style renderer
(styles/create-renderer)

;; style rules are just a function of props that return a css map
;; new styles will be generated when props change
(defn rule
  [props]
  {:color (:color props)
   :padding (case (:padding props)
              :large "20px"
              :small "5px"
              0)
   :font-size "16px"
   ":hover" {:color "black"
             :transform "translateY(-" (:shiftY props) ")"}
   "@media (max-width 410px)" {:margin "10px 0"}})

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

This wrapper was made with Reagent in mind, but the class names generated can be passed anywhere and to any object you'd like. Styling as a function of state is fela's goal and with the inclusion of media queries, pseudo classes, child selectors, and keyframes etc. it becomes a viable substitute for css. Vashet attempts to embrace these features and allow for the creation of rich, dynamic, styled components for web applications written in ClojureScript.

## Reagent Example
```clojure
(ns reagent-app.core
  (:require [reagent.core :as r]
            [vashet.core :as styles]))

(defn toggle-button-style
  [props]
  (let [toggled? (:toggled? props)]
    (merge
      {:background-color (:bg-color props)
       :font-size (str (:font-size props) "px")
       :opacity 0.8
       ":hover" {:opacity 1
                 :transform "translateY(-5%)"}}
      (if toggled? {:opacity 1} {}))))

(defn toggle-button-media-query
  [props]
  {"@media (max-width 410px)" {:height "42px" :width (if (:wide? props) "90%" 92px)}
   "@media (max-width 920px)" {:height "64px" :width (if (:wide? props) "85%" 180px)}})

(def toggle-button-rule (styles/combine-rules
                          toggle-button-style
                          toggle-button-media-query))

(defn toggle-button
  [text]
  (let [state (r/atom {:toggled? false})]
    (fn [text]
      [:div
        {:class (styles/render-rule toggle-button-rule (merge {:bg-color "blue" :font-size 12} @state))
         :on-click #(swap! update-in state [:toggled?] not)}
        text])))

(defn application-container
  []
  (r/create-class
    {:component-did-mount (fn []
                            (create-renderer)
                            (init-styles (.getElementById js/document "my-styles-node")))
     :reagent-render (fn []
                       [:div
                         [toggle-button "toggle vashet styles"]])}))

(r/render [application-container] (.getElementById js/document "my-reagent-app"))
```

## API
Below is a brief description of the available methods. For further clarification I recommend visiting fela's [website](http://fela.js.org/). Discounting plugins, all of fela's API is encapsulated here, but expects EDN arguments. The only plugin current available in vashet is [fela-plugin-prefixer](https://github.com/rofrischmann/fela/tree/master/packages/fela-plugin-prefixer) which is activated within vashet's default configuration. I intend to expose more fela plugins in the future.

### (create-renderer [& [config]])
instantiate the fela renderer with an optional config object
#### config
type: `map`

### (init-styles node)
renders all styles into specified node
#### node *required*
type: `DOM-node`

### (render-rule rule props)
invokes the rule function with props, applies the resulting styles to the style node, and returns corresponding class names
#### rule *required*
type: `fn`
#### props
type: `map`

### (render-keyframe keyframe props)
invokes the keyframe function with props, places the keyframe definition the style node, and returns corresponding keyframe name
```clojure
(defn my-keyframe
  [props]
  {:0% {:opacity (:start props)}
   :100% {:opacity (:end props)}})

(println (render-keyframe my-keyframe {:start 0 :end 1})) ;; => "k1"
```
#### keyframe *required*
type: `fn`
#### props
type: `map`

### (render-font family files props)
Adds a font-face to the style node
```clojure
(def files ["./fonts/Lato.ttf" "./fonts/Lato.woff"])

(render-font "Lato" files)
(render-font "Lato-bold" files {:font-weight "bold"})
```
#### family *required*
type: `string`
#### files *required*
type: `collection`
#### props
type: `map`

### (render-static styles selectors)
applies static styles for the provided selectors
```clojure
(def header-global
  []
  {:font-weight "bold"
   :font-family "Open Sans"
   :color "blue"})

(def container-global
  []
  {"@media (max-width 410px)" {:margin "10px 20px"}
   "@media (max-width 660px)" {:margin "10px 30px"}
   "@media (max-width 980px)" {:margin "10px 50px"}})

(render-static header-global [:h1 "h2" :h3])
(render-static container-global [".container" :#special-container])
```
#### styles *required*
type: `map`
#### selectors *required*
type: `collection`

### (combine-rules & rules)
accepts a collection of rule functions and merges their result; functions appearing later in the collection overwrite existing properties
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

(println (render-rule combined-rule {:color "red" :weight "bold" :font "monospace"}))
;; => "a b c d"
```
#### rules *required*
type: `collection`

### (build-animation & {:keys [duration timing-fn delay count direction keyframe props]})
accepts optional css animation key-value pairs, a 'keyframe' key-value pair, and 'props' key-value pair then applies the keyframe to the style node and returns the animation string
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

(println pulse-animation) ;; => "3s ease-in infinite alternate k1"

(def rule
  [props]
  {:background-color "black"
   ":hover" {:background-color "gray"}
   :animation (:animation props)})

(render-rule rule {:animation pulse-animation})
```
#### duration *required*
type: `string`
#### timing-fn
type: `string`
#### delay
type: `string`
#### count
type: `string/number`
#### direction
type: `string`
#### keyframe *required*
type: `fn`
#### props
type: `map`

### (render-styles & {:keys [rule props add-class]})
works like render-rule but accepts key-value pairs and allows for optionally passing in a collection of regular css classes
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
#### rule *required*
type: `fn`
#### props
type: `map`
#### add-class
type: `collection`

### (render-to-string)
returns all styles in a css string
```clojure
(render-rule #(merge {:color nil} %) {:color "yellow"})

(println (render-to-string)) ;; => ".a{color:yellow}"
```

### (subscribe-to-styles cbfn)
listens to style changes and invokes the callback function with an info map when new styles are added
#### cbfn *required*
type: `fn`

### (clear-styles)
removes all styles from the targeted style node

## License

Copyright © 2017 Christopher Howard

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
