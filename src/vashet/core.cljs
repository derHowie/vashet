(ns vashet.core
  (:require
    cljsjs.fela
    cljsjs.fela-dom
    cljsjs.fela-plugin-prefixer
    [clojure.spec.alpha :as s]
    [clojure.string :refer [capitalize split]])
  (:require-macros
    [vashet.core :refer [nilless-keyframe js-result]]))

;; ---------------------- Spec

(def key-or-str (s/or :keyword keyword? :string string?))
(def num-str-or-map (s/or :number number? :string string? :map map?))
(def coll-or-str (s/or :coll coll? :string string?))
(def valid-style-map (s/nilable (s/map-of key-or-str (s/nilable num-str-or-map))))

(s/def ::renderer-config (s/nilable (s/map-of key-or-str coll-or-str :max-count 5)))

(s/def ::render-rule fn?)
(s/def ::rule-result valid-style-map)

(s/def ::render-keyframe fn?)
(s/def ::keyframe-result (s/nilable (s/map-of key-or-str map?)))

(s/def ::font-family string?)
(s/def ::font-files coll?)
(s/def ::font-props map?)

(s/def ::static-styles valid-style-map)
(s/def ::static-selectors (s/or :string string? :selector-vec (s/coll-of key-or-str)))

(s/def ::subscription-callback fn?)

(s/def ::DOM-node #(= (.-nodeType %) 1))

(s/def ::rule-collection (s/coll-of fn?))

(s/def ::duration string?)
(s/def ::timing-fn (s/nilable string?))
(s/def ::delay (s/nilable string?))
(s/def ::count (s/nilable (s/or :string string? :number number?)))
(s/def ::direction (s/nilable string?))
(s/def ::keyframe fn?)
(s/def ::animation
  (s/keys :req-un [::duration ::keyframe]
          :opt-un [::timing-fn ::delay ::count ::direction ::keyframe]))


(defn- args-valid?
  [spec args fn-name]
  (let [valid? (s/valid? spec args)]
    (when-not valid?
      (throw
        (js/Error.
          (str "METHOD: " fn-name ": \n"
               (s/explain-str spec args)))))
    valid?))

;; ---------------------- Helpers

(defn- kebab->camel
  [v]
  (reduce (fn [a b] (if a (str a (capitalize b)) b)) nil (split (name v) #"\-")))

(defn- map-keys->camel
  [m]
  (zipmap (map kebab->camel (map key m)) (map val m)))

(defn- rip-nils
  [m]
  (into {} (filter #(not= (val %) nil) m)))

(defn- kf-rip-nils
  [m]
  (into {} (filter #(seq (val %)) (into {} (map #(into [] [(key %) (rip-nils (val %))]) m)))))

;; ---------------------- Plugins / Enhancers

(defn auto-prefixer
  "applies auto vendor prefixing to styles when this method invocation is included in the 'plugins' vector of the 'create-renderer' config"
  []
  (js/FelaPluginPrefixer))

;; ---------------------- Renderer API

(def Renderer (atom "not a renderer"))

(defn create-renderer
  "instantiate the fela renderer with an optional config object
   
   param -- {map} config: a map of configuration options; includes plugins, key-frame-prefixes, enhancers, media-query-order, selector-prefix"
  [& [config]]
  {:pre [(args-valid? ::renderer-config config "create-renderer")]}
  (let [default-config {:plugins [(auto-prefixer)]}
        jsify-config   #(clj->js (map-keys->camel %))
        js-config      (jsify-config (merge default-config config))]
    (reset! Renderer (.createRenderer js/Fela js-config))))

(defn render-rule
  "accepts a function, applies the resulting styles to the style node,
   and returns the class name

   param -- {fn}  rule:  a function returning a map of styles REQUIRED
   param -- {map} props: a map of styles passed to the rule function"
  [rule props]
  {:pre [(args-valid? ::render-rule rule "render-rule")]}
  {:post [(args-valid? ::rule-result (rule nil) "render-rule")]}
  (.renderRule @Renderer (js-result rule) props))

(defn render-keyframe
  "accepts a keyframe function, applies the keyframe to the style node, and
   returns the name of the keyframe

   param -- {fn}  keyframe: a function returning the keyframe map REQUIRED
   param -- {map} props:    a map of values passed to the keyframe function"
  [keyframe props]
  {:pre [(args-valid? ::render-keyframe keyframe "render-keyframe")]}
  {:post [(args-valid? ::keyframe-result (keyframe nil) "render-keyframe")]}
  (.renderKeyframe @Renderer (js-result (nilless-keyframe keyframe)) props))

(defn render-font
  "Adds a font-face to the style node
  
   param -- {string} family: font-family name REQUIRED
   param -- {coll}   files:  collection of font source file paths relative to index.html REQUIRED
   param -- {map}    props:  hash-map containing optional font properties; includes font-variant, font-stretch, font-weight, font-style, unicode-range"
  [family files props]
  {:pre [(args-valid? ::font-family family "render-font")
         (args-valid? ::font-files files "render-font")
         (args-valid? ::font-props props "render-font")]}
  (.renderFont @Renderer family (apply array files) (clj->js (map-keys->camel props))))

(defn render-static
  "applies static styles to the provided selectors

   param -- {map}    styles:    styles object REQUIRED
   param -- {vector} selectors: vector containing keys describing desired selectors REQUIRED"
  [styles selectors]
  {:pre [(args-valid? ::static-styles styles "render-static")
         (args-valid? ::static-selectors selectors "render-static")]}
  (.renderStatic
    @Renderer
    (clj->js styles)
    (apply str (interpose "," (map name selectors)))))

(defn render-to-string
  "returns all styles in a css string"
  []
  (.renderToString @Renderer))

(defn subscribe-to-styles
  "hands a callback function to a listener that fires every time a new
   style is rendered; returns info obj

   param -- {fn} cbfn: callback function REQUIRED"
  [cbfn]
  {:pre [(args-valid? ::subscription-callback cbfn "subscribe-to-styles")]}
  (.subscribe @Renderer #(cbfn (js->clj % :keywordize-keys true))))

(defn clear-styles
  []
  "clears all styles from fela-dom.render()'s targeted node"
  (.clear @Renderer))

;; ---------------------- Util Methods

(defn init-styles
  "calls fela-dom.render() on the provided node

   param -- {dom-element} node: a 'style' or 'div' element to append styles to REQUIRED"
  [node]
  {:pre [(args-valid? ::DOM-node node "init-styles")]}
  (.render js/FelaDOM @Renderer node))

(defn combine-rules
  "accepts a collection of rule functions and merges their result; functions
  appearing later in the collection overwrite existing properties

   param -- {coll} rules: a collection of rule functions to be combined REQUIRED"
  [& rules]
  {:pre [(args-valid? ::rule-collection rules "combine-rules")]}
  (fn [props]
    (reduce merge (map #(% props) rules))))

(defn build-animation
  "accepts optional css-animation key-value pairs, a 'keyframe' key-value pair,
   a 'props' key-value pair then applies the keyframe to the style node and
   returns the animation string
  
   param -- {kv(string)}     duration:  the animation's duration REQUIRED
   param -- {kv(string)}     timing-fn: a timing function (i.e. 'ease-in')
   param -- {kv(string)}     delay:     the animation's delay
   param -- {kv(string/int)} count:     the number of iterations
   param -- {kv(string)}     direction: the animation's direction
   param -- {kv(fn)}         keyframe:  a function returning the keyframe map REQUIRED
   param -- {kv(map)}        props:     a map of values passed to the keyframe function"
  [& {:keys [duration timing-fn delay count direction keyframe props] :as args}]
  {:pre [(args-valid? ::animation args "build-animation")]}
  (str (apply str (interpose " " (filter #(or (string? %) (number? %)) (map val args))))
       " "
       (render-keyframe keyframe props)))

(defn render-styles
  "works like 'render-rule' but accepts key-value pairs and allows for optionally
   passing in a collection of regular css classes to include
  
   param -- {key+fn}   rule:      a function returning a map of styles REQUIRED
   param -- {key+map}  props:     a map of styles passed to the rule function
   param -- {key+coll} add-class: a collection of strings or keys corresponding to regular css classes"
  [& {:keys [rule props add-class]}]
  (let [static-string (apply str (interpose " " (map name (filter #(not (nil? %)) add-class))))]
    (str (render-rule rule props) " " static-string)))
