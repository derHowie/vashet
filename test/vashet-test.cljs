(ns vashet.vashet-test
  (:require
    [cljs.test :refer-macros [deftest testing is async]]
    [vashet.core
     :refer [auto-prefixer
             create-renderer
             render-rule
             render-to-string
             render-keyframe
             render-font
             render-static
             subscribe-to-styles
             clear-styles
             combine-rules
             build-animation
             render-styles]]))

(deftest render-rule-test
  (testing "render-rule"
    (let [test-rule #(merge {:color nil} %)]
      ;; instantiate Renderer
      (create-renderer)

      ;; apply styles with render-rule
      (render-rule test-rule {:color "black"})

      (is (= (render-to-string)
             ".a{color:black}")
          "corresponding class is applied to Renderer")

      ;; remove current styles from Renderer
      (clear-styles))))

(deftest render-keyframe-test
  (testing "render-keyframe"
    (let [test-keyframe #(into {} {:0% {:opacity (:start %)} :100% {:opacity (:end %)}})]

      ;; apply keyframe with render-keyframe
      (render-keyframe test-keyframe {:start 0 :end 1})

      (is (= (render-to-string)
             "@-webkit-keyframes k1{0%{opacity:0}100%{opacity:1}}@-moz-keyframes k1{0%{opacity:0}100%{opacity:1}}@keyframes k1{0%{opacity:0}100%{opacity:1}}")
          "keyframes with vendor prefixes applied to Renderer")

      ;; remove current styles from Renderer
      (clear-styles))))

(deftest render-static-test
  (testing "render-static"
    (let [static-styles {:font-family "monospace"}]
      
      ;; apply static styles with render-static
      (render-static static-styles [:h3 :p])
      
      (is (= (render-to-string)
             "h3,p{font-family:monospace}")
          "static fonts applied to selectors")

      ;; apply additional static styles with render-static
      (render-static {:color "red"} [:span])

      (is (= (render-to-string)
             "h3,p{font-family:monospace}span{color:red}")
          "multiple calls to render-static apply all styles")
      
      ;; remove current styles from Renderer
      (clear-styles))))

(deftest subscribe-to-styles-test
  (testing "subscribe-to-styles"
    (let [state       (atom false)
          callback-fn #(reset! state (aget % "declaration"))
          test-rule   #(merge {:font-weight nil} %)]
      (async done

             ;; hand callback-fn to the style listener
             (subscribe-to-styles callback-fn)

             ;; apply new styles to the Renderer with render-rule
             (render-rule test-rule {:font-weight "bold"})
             
             (is (= @state
                    "font-weight:bold")
                 "callback-fn fires when a style is added to Renderer and receives an object describing the most recent change")
             
             ;; remove current styles from Renderer
             (clear-styles)
             
             (done)))))

(deftest combine-rules-test
  (testing "combine-rules"
    (let [rule-one #(merge {:color "black" :font-size nil} %)
          rule-two #(into {} {:color (:color %)})
          combined-rule (combine-rules rule-one rule-two)]
      
      ;; apply styles to Renderer with render-rule
      (render-rule combined-rule {:font-size "12px" :color "red"})
      
      (is (= (render-to-string)
             ".a{color:red}.b{font-size:12px}")
          "combined rule applies the appropriate styles to Renderer")
      
      ;; remove current styles from Renderer
      (clear-styles))))

(deftest build-animation-test
  (testing "build-animation"
    (let [anim-test-keyframe #(into {} {:0% {:opacity (:start %)} :100% {:opacity (:end %)}})
          animation-string   (build-animation
                               :duration  "3s"
                               :timing-fn "ease-in"
                               :delay     "1s"
                               :count     "infinite"
                               :direction "alternate"
                               :keyframe  anim-test-keyframe
                               :props     {:start 0
                                           :end   1}) 
          test-rule          #(merge {:animation nil} %)]

      ;; apply styles, including animation, to Renderer with render-rule
      (render-rule test-rule {:animation animation-string
                              :font-size "12px"})

      (is (= (render-to-string)
             "@-webkit-keyframes k1{0%{opacity:0}100%{opacity:1}}@-moz-keyframes k1{0%{opacity:0}100%{opacity:1}}@keyframes k1{0%{opacity:0}100%{opacity:1}}.a{animation:3s infinite ease-in 1s alternate k1;-webkit-animation:3s infinite ease-in 1s alternate k1}.b{font-size:12px}"))
      
      ;; remove current styles from Renderer
      (clear-styles))))

(deftest render-styles-test
  (testing "render-styles"
    (let [test-rule #(merge {:color nil} %)]
      
      ;; add dynamic styles to Renderer and also append static css class names with render-styles
      (is (= (render-styles
               :rule  test-rule
               :props {:color "blue"}
               :add-class [:class-a :class-b "class-c"])
             "a class-a class-b class-c")))))

;; TODO: 
;; init-styles-test
;; render-font-test
;; auto-prefixer-test
