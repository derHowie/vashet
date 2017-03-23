(ns vashet.clj.core)

(defmacro js-result
  "Accepts a function that returns a map and returns a
   new function that will return a javascript object"
  [wrapped-fn]
  `(fn [& [props#]]
     (apply ~'js-obj (~'map->name-seq (~wrapped-fn props#)))))

(defmacro js-keyframe
  "Accepts a keyframe function that returns an edn map
   and returns a keyframe function that returns a map
   comprised of values that are javascript objects"
  [wrapped-fn]
  `(fn [& [props#]]
     (let [value->js-obj# #(apply ~'js-obj (~'map->name-seq %))
           mapped-keys#   (map key (~wrapped-fn props#))
           mapped-values# (map value->js-obj# (map val (~wrapped-fn props#)))]
       (apply hash-map (interleave mapped-keys# mapped-values#)))))
