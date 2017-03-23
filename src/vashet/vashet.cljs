(ns vashet.core
  (:require        js.fela js.fela-dom js.fela-prefixer
                   [reagent.core :as r])
  (:require-macros [vashet.clj.core :refer [js-keyframe js-result]]))

(enable-console-print!)
;; ---------------------- Helpers

(defn- map->name-seq
  [m]
  (interleave (map name (map key m)) (map val m)))

;; ---------------------- Renderer API

(def Renderer (atom nil))

(defn create-renderer
  "instantiate the fela renderer with an optional config object
   
   @param {map} config: a map of configuration options; includes plugins, key-frame-prefixes, enhancers, media-query-order, selector-prefix"
  [& [config]]
  (if-let [c config]
  (reset! Renderer (.createRenderer js/Fela (apply js-obj (map->name-seq c))))
  (reset! Renderer (.create))))

(defn render-rule
  "accepts a function, applies the resulting styles to the style node,
   and returns the class name

   @param {fn}  rule:  a function returning a map of styles REQUIRED
   @param {map} props: a map of styles passed to the rule function"
  [rule props]
  (.renderRule @Renderer (js-result rule) props))

(defn render-keyframe
  "accepts a keyframe function, applies the keyframe to the style node, and
   returns the name of the keyframe

   @param {fn}  keyframe: a function returning the keyframe map REQUIRED
   @param {map} props:    a map of values passed to the keyframe function"
  [keyframe props]
  (.renderKeyframe @Renderer (js-result (js-keyframe keyframe)) props))

(defn render-font
  "Adds a font-face to the style node
  
   @param {string} family: font-family name REQUIRED
   @param {vector} files:  collection of font source file paths relative to index.html REQUIRED
   @param {map}    props:  hash-map containing optional font properties; includes font-variant, font-stretch, font-weight, font-style, unicode-range"
  [family files props]
  (.renderFont @Renderer (apply js-obj (map->name-seq props))))

(defn render-static
  "applies static styles to the provided selectors

   @param {map}    styles:    styles object REQUIRED
   @param {vector} selectors: vector containing keys describing desired selectors REQUIRED"
  [styles selectors]
  (.renderStatic
    @Renderer
    (apply js-obj (map->name-seq styles))
    (apply str (interpose "," (map name selectors)))))

(defn render-to-string
  "returns all styles in a css string"
  []
  (.renderToString @Renderer))

(defn subscribe-to-styles
  "hands a callback function to a listener that fires every time a new
   style is rendered; returns info obj

   @param {fn} cbfn: callback function REQUIRED"
  [cbfn]
  (.subscribe @Renderer cbfn))

(defn clear-styles
  []
  "clears all styles from fela-dom.render()'s targeted node and prevents
   more styles from rendering"
  (.clear @Renderer))

;; ---------------------- Util Methods

(defn init-styles
  "calls fela-dom.render() on the provided node

   @param {dom-element} node: a 'style' or 'div' element to append styles to REQUIRED"
  [node]
  (.render js/FelaDOM @Renderer node))

(defn combine-rules
  "accepts a collection of rule functions and merges their result; functions
  appearing later in the collection overwrite existing properties

   @param {coll} rules: a collection of rule functions to be combined REQUIRED"
  [& rules]
  (fn [props]
    (reduce merge (map #(% props) rules))))

(defn build-animation
  "accepts optional css-animation key-value pairs, a 'keyframe' key-value pair,
   a 'props' key-value pair then applies the keyframe to the style node and
   returns the animation string
  
   @param {key+string}     duration:  the animation's duration REQUIRED
   @param {key+string}     timing-fn: a timing function (i.e. 'ease-in')
   @param {key+string}     delay:     the animation's delay
   @param {key+int/string} count:     the number of iterations
   @param {key+string}     direction: the animation's direction
   @param {key+fn}         keyframe:  a function returning the keyframe map REQUIRED
   @param {key+map}        props:     a map of values passed to the keyframe function"
  [& {:keys [duration timing-fn delay count direction keyframe props] :as args}]
  (str (apply str (interpose " " (filter #(or (string? %) (number? %)) (map val args))))
       " "
       (render-keyframe keyframe props)))

(defn render-styles
  "works like 'render-rule' but accepts key-value pairs and allows for optionally
   passing in a collection of regular css classes to included
  
   @param {key+fn}   rule:      a function returning a map of styles REQUIRED
   @param {key+map}  props:     a map of styles passed to the rule function
   @param {key+coll} add-class: a collection of strings or keys corresponding to regular css classes"
  [& {:keys [rule props add-class]}]
  (let [static-string (apply str (interpose " " add-class))]
    (str (render-rule rule props) " " static-string)))

(defn test-keyframe
  [props]
  {:0%   {:font-size "10px"}
   :100% {:font-size (:final-size props)}})

(defn test-rule
  [props]
  {:color     (:color props)
   :font-size (str (* (:font-size props) 10) "px")
   :animation (build-animation
                :duration  "1s"
                :timing-fn "ease-in"
                :count     "infinite"
                :direction "alternate"
                :keyframe  test-keyframe
                :props     {:final-size "40px"})})

(defn test-rule-two
  [props]
  {:font-weight "bold"})

(def combined (combine-rules test-rule-two test-rule))

(defn test-cmp
  []
  (let [state (r/atom false)]
    (r/create-class
      {:component-did-mount (fn []
                              (init-styles (.getElementById js/document "vashet"))
                              (render-static {:background-color "#999"} [:body :p]))
       :reagent-render      (fn []
                              [:div 
                               [:p
                                {:class    (render-styles
                                             :rule      combined
                                             :props     {:color     (if @state "red" "blue")
                                                         :font-size 5}
                                             :add-class ["style-a" "style-b"]) 
                                 :on-click #(swap! state not)}
                                "Test Component"]])})))

(create-renderer)
(r/render [test-cmp] (.getElementById js/document "app"))
