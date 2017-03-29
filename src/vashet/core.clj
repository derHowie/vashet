(ns vashet.core)

(defmacro js-result
  "Accepts a function that returns a map and returns a
   new function that will return a javascript object
  
   param -- {fn} wrapped-fn: a function that returns an EDN map"
  [wrapped-fn]
  `(fn [& [props#]]
     (apply ~'js-obj (~'map->name-seq (~'rip-nils (~wrapped-fn props#))))))

(defmacro js-keyframe
  "Accepts a keyframe function that returns an EDN map
   and returns a keyframe function that returns a map
   comprised of values that are javascript objects
  
   param -- {fn} wrapped-fn: a function returning an EDN map describing a keyframe"
  [wrapped-fn]
  `(fn [& [props#]]
     (let [value->js-obj# #(apply ~'js-obj (~'map->name-seq %))]
       (~'map-map (~'kf-rip-nils (~wrapped-fn props#)) identity value->js-obj#))))
