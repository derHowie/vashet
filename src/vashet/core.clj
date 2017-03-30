(ns vashet.core)

(defmacro js-result
  "Accepts a function that returns a map and returns a
   new function that will return a javascript object
  
   param -- {fn} wrapped-fn: a function that returns an EDN map"
  [wrapped-fn]
  `(fn [& [props#]]
     (~'clj->js (~'rip-nils (~wrapped-fn props#)))))

(defmacro nilless-keyframe
  "Accepts a function that returns a keyframe map and
   returns a function that returns a keyframe map without
   nil values in each step-map
  
   param -- {fn} wrapped-fn: a function returning an EDN map describing a keyframe"
  [wrapped-fn]
  `(fn [& [props#]]
     (~'kf-rip-nils (~wrapped-fn props#))))
