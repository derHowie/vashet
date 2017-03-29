(ns ^{:skip-aot true} browser.core
  (:require
    [reagent.core :as r]
    [vashet.core :as vashet]))

(vashet/create-renderer)

(defn browser-test-rule-one
  [props]
  {:font-size (:font-size props)
   :color     (:color props)})

(defn browser-test-rule-two
  [props]
  {:animation (:animation props)})

(defn browser-test-keyframe
  [props]
  {:0%   {:opacity   (:start props)
          :transform (str "scale(" (:scale-start props) "," (:scale-start props) ")")}
   :100% {:opacity   (:end props)
          :transform (str "scale(" (:scale-end props) "," (:scale-end props) ")")}})

(defn browser-test-cmp
  []
  (let [state (r/atom false)]
    (fn []
      (let [animation (vashet/build-animation
                        :duration  "3s"
                        :delay     "1s"
                        :count     "infinite"
                        :direction "alternate"
                        :keyframe  browser-test-keyframe
                        :props     {:start       0
                                    :scale-start 1
                                    :end         1
                                    :scale-end   0.75})]
        [:p
         {:class (vashet/render-styles
                   :rule      (vashet/combine-rules browser-test-rule-one browser-test-rule-two)
                   :props     {:color     (if @state "blue" "red")
                               :font-size "20px"
                               :animation animation}
                   :add-class ["another-class"])}
         "Vashet Browser Test"]))))

(defn app-container
  []
  (r/create-class
    {:component-did-mount #(vashet/init-styles (.getElementById js/document "styles"))
     :reagent-render      (fn []
                            [:div
                             [browser-test-cmp]])}))

(defn mount-root []
  (r/render [app-container]
            (.getElementById js/document "app")))

(defn ^:export init []
  (mount-root))
