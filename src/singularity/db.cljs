(ns ^{:doc "Contains central persistent db atom and helper get/set functions."}
  singularity.db
  (:refer-clojure :exclude [get get-in update-in assoc-in])
  (:require [reagent.core :refer [atom]]
            [reagent.ratom :refer [cursor]]))




; Use schema as validator for state atom. Ensures that an exception
; is thrown if `state` is modified in a way that breaks the schema.
(def state
  "Central application state atom. Automatically validated to match
   the `schema`. Easily get/set data in here using the `singularity.db`
   versions of `get-in`, `update-in`, `assoc-in`, etc."
  (atom {} {:validator (constantly true)}))

(defn me [] (cursor state [:me]))
(defn them [] (cursor state [:them]))
(defn turn [] (cursor state [:turn]))
(defn selected-space [] (cursor state [:selected-space]))
(defn board-width [] (cursor state [:board-width]))
(defn board [] (cursor state [:board]))
(defn square [coords] (cursor (board) [coords]))
(defn piece [coords] (cursor (square coords) [:piece]))

; These functions wrap the `clojure.core` versions, preventing the user
; from having to think about derefs, swap!, etc.
(defn get [key] (clojure.core/get @state key))
(defn get-in [path] (clojure.core/get-in @state path))
(defn update-in [path fn] (swap! state clojure.core/update-in path fn))
(defn assoc-in [path fn] (swap! state clojure.core/assoc-in path fn))

(defn define 
  "Helper function for merging into state. Usage: (define k1 v1, k2 v2, ...)"
  [& args] (swap! state conj (->> args (partition 2) (map vec))))