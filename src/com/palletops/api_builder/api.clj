(ns com.palletops.api-builder.api
  "An API defn form that uses all stages"
  (:require
   [com.palletops.api-builder :refer [def-defn def-def def-defmulti]]
   [com.palletops.api-builder.stage :refer :all]))

;;; # API defn
(def-defn defn-api
  [(validate-errors (constantly true))
   (validate-sig)
   (add-sig-doc)
   (add-meta {:api true})])

(def-defmulti defmulti-api
  [(validate-errors (constantly true))
   (validate-sig)
   (add-sig-doc)
   (add-meta {:api true})])

(def-def def-api
  [(add-meta {:api true})])
