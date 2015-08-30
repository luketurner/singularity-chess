(ns singularity.db
  (:refer-clojure :exclude [get get-in update-in assoc-in])
  (:require [reagent.core :refer [atom]]
            [singularity.tools :as tools]
            [schema.core :as s :include-macros true]))


(def schema 
  "Defines the expected shape (i.e. data model) for `state`."
  (let [piece-colors (s/enum :white :black)
        piece-types (s/enum :rook :knight :bishop :pawn :queen :king)]
    {:board-width s/Num
     :selected-space (s/maybe [s/Num])
     :me piece-colors
     :them piece-colors
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

; piece schema for future?
;        :color (s/enum :white :black)
;        :type (s/enum :rook :knight :bishop :pawn :queen :king)

(def starting-pieces 
  (let [w (fn [t] {:color :white :type t})
        b (fn [t] {:color :black :type t})]
    (into {
      [0, 0]  (w :rook) [1, 0] (w :knight) [2, 0] (w :bishop) [3, 0]  (w :queen)
      [4, 0]  (w :king) [5, 0] (w :bishop) [6, 0] (w :knight) [7, 0]  (w :rook)
      [0, 4]  (b :rook) [1, 6] (b :knight) [2, 8] (b :bishop) [3, 10] (b :queen)
      [4, 10] (b :king) [5, 8] (b :bishop) [6, 6] (b :knight) [7, 4]  (b :rook)}
      (conj (for [x (range 8)] [[x, 1] (w :pawn)])
            (for [x (range 8)] [[x, (-> x tools/col-len (- 2))] (b :pawn)])))))


(defn init
  "Initializes `state` atom, overwriting everything with default values.
   Should be called at least once on app startup."
  [] 
  (reset! state {
    :board-width 600
    :selected-space nil
    :me :white
    :them :black
    :board (into {} 
      (for [x (range 8)] (for [y (range (tools/col-len x))]
        [[x y] {:piece (starting-pieces [x y])}])))}))


; These functions wrap the `clojure.core` versions, preventing the user
; from having to think about derefs, swap!, etc.
(defn get [key] (clojure.core/get @state key))
(defn get-in [path] (clojure.core/get-in @state path))
(defn update-in [path fn] (swap! state clojure.core/update-in path fn))
(defn assoc-in [path fn] (swap! state clojure.core/assoc-in path fn))