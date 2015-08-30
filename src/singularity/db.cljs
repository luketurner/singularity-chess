(ns singularity.db
  (:refer-clojure :exclude [get get-in update-in assoc-in])
  (:require [reagent.core :refer [atom]]
            [singularity.tools :as tools]
            [schema.core :as s :include-macros true]))


(def schema 
  "Defines the expected shape (i.e. data model) for `state`."
  {:board-width s/Num
   :selected-space (s/maybe [s/Num])
   :board (s/both 
     (s/pred #(= 64 (count %)))
     {[s/Num] {:piece (s/maybe s/Str)}})})


; Use schema as validator for state atom. Ensures that an exception
; is thrown if `state` is modified in a way that breaks the schema.
(def state
  "Central application state atom. Automatically validated to match
   the `schema`. Easily get/set data in here using the `singularity.db`
   versions of `get-in`, `update-in`, and `assoc-in`."
  (atom {} {:validator #(s/validate schema %)}))

; piece schema for future?
;        :color (s/enum :white :black)
;        :type (s/enum :rook :knight :bishop :pawn :queen :king)

(def starting-pieces (into {
  [0, 0]  "r" [1, 0] "n" [2, 0] "b" [3, 0]  "q"
  [4, 0]  "k" [5, 0] "b" [6, 0] "n" [7, 0]  "r"
  [0, 4]  "R" [1, 6] "N" [2, 8] "B" [3, 10] "Q"
  [4, 10] "K" [5, 8] "B" [6, 6] "N" [7, 4]  "R" }
  (conj (for [x (range 8)] [[x, 1] "p"])
        (for [x (range 8)] [[x, (-> x tools/col-len (- 2))] "P"]))))


(defn init
  "Initializes `state` atom, overwriting everything with default values.
   Should be called at least once on app startup."
  [] 
  (reset! state {
    :board-width 600
    :selected-space nil
    :board (into {} 
      (for [x (range 8)] (for [y (range (tools/col-len x))]
        [[x y] {:piece (starting-pieces [x y])}])))}))


; These functions wrap the `clojure.core` versions, preventing the user
; from having to think about derefs, swap!, etc.
(defn get [key] (clojure.core/get @state key))
(defn get-in [path] (clojure.core/get-in @state path))
(defn update-in [path fn] (swap! state clojure.core/update-in path fn))
(defn assoc-in [path fn] (swap! state clojure.core/assoc-in path fn))