(ns singularity.db
  (:refer-clojure :exclude [get get-in update-in assoc-in])
  (:require [reagent.core :refer [atom]]
            [schema.core :as s :include-macros true]))


(def schema 
  "Defines the expected shape (i.e. data model) for `state`."
  (let [piece-colors (s/enum :white :black)
        piece-types (s/enum :rook :knight :bishop :pawn :queen :king)]
    {:board-width s/Num
     :selected-space (s/maybe [s/Num])
     :me piece-colors
     :them (s/enum :ai :player :me)
     :turn piece-colors
     :board (s/both 
       (s/pred #(= 64 (count %)))
       {[s/Num] {:piece (s/maybe {
        :color piece-colors
        :type piece-types})}})}))


; Use schema as validator for state atom. Ensures that an exception
; is thrown if `state` is modified in a way that breaks the schema.
(def state
  "Central application state atom. Automatically validated to match
   the `schema`. Easily get/set data in here using the `singularity.db`
   versions of `get-in`, `update-in`, and `assoc-in`."
  (atom {} {:validator #(s/validate schema %)}))

; These functions wrap the `clojure.core` versions, preventing the user
; from having to think about derefs, swap!, etc.
(defn get [key] (clojure.core/get @state key))
(defn get-in [path] (clojure.core/get-in @state path))
(defn update-in [path fn] (swap! state clojure.core/update-in path fn))
(defn assoc-in [path fn] (swap! state clojure.core/assoc-in path fn))
(defn define [& args] (swap! state conj (->> args (partition 2) (map vec))))