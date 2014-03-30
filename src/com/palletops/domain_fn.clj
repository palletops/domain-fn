(ns com.palletops.domain-fn
  "Build augmented defn forms"
  (:require
   [clojure.tools.macro :refer [name-with-attributes]]
   [schema.core :as schema]))

(def defn-arglists (vec (:arglists (meta #'defn))))

(def ArityMap {:args [schema/Any]
               (schema/optional-key :conditions)
               {(schema/optional-key :pre) [schema/Any]
                (schema/optional-key :post) [schema/Any]}
               :body [schema/Any]})
(def DefnMap {:name clojure.lang.Symbol
              :arities [ArityMap]
              :meta {schema/Keyword schema/Any}})

(defn arity-map
  [args]
  {:post [(schema/validate ArityMap %)]}
  (let [body (rest args)
        n (count body)
        condition-map (if (and (> (count body) 1)
                               (map? (first body)))
                        (first body))
        body (if condition-map
               (rest body)
               body)]
    (-> {:args (first args)
         :body body}
        (cond->
         condition-map (assoc :conditions condition-map)))))

(defn defn-map
  "Return a map with destructured defn args."
  [n args]
  {:post [(schema/validate DefnMap %)]}
  (let [[n args] (name-with-attributes n args)
        m (meta n)]
    {:name n
     :arities (if (vector? (first args))
                [(arity-map args)]
                (map arity-map args))
     :meta m}))

(defn arity-form [{:keys [args conditions body] :as arity}]
  `(~args ~@(if conditions [conditions]) ~@body))

(defn defn-form
  "Return a defn form for a defn map"
  [{:keys [name arities meta] :as m}]
  {:pre [(schema/validate DefnMap m)]}
  `(defn ~(with-meta name meta)
     ~@(if (= 1 (count arities))
         (arity-form (first arities))
         (map arity-form arities))))

(defn defn-impl
  [mw n args]
  (defn-form
    (reduce
     (fn [m f]
       {:pre [(schema/validate DefnMap m)]
        :post [(schema/validate DefnMap %)]}
       (f m))
     (defn-map n args)
     mw)))

(defmacro def-defn
  "Define a defn form"
  [name middleware]
  `(defmacro ~name
     {:arglists '~defn-arglists}
     [n# & args#]
     (defn-impl ~middleware n# args#)))