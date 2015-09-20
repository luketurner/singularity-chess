(ns ^{:doc "Contains logic for making control panel and handling input therefrom."}
  singularity.controls
  (:require [singularity.db :as db]))

(defn- input-group
  "Component defining an input that is automatically mapped to
   given `path` in the `db`."
  ([path] (input-group path "text"))
  ([path type] (input-group path type identity))
  ([path type converter]
    (let [value (db/get-in path)
          handler #(db/assoc-in path (converter (-> % .-target .-value)))]
    [:input {:type type :value value :on-change handler}])))

(defn- toggle-link
  "Component defining an 'link' that actually updates a path in the db.
   `converter` must be a function which returns a string when given the
   value in the db."
  [path options] 
    (let [opt-keys (keys options)
          value (db/get-in path)
          text (options value)
          value-next (zipmap opt-keys (conj (vec (rest opt-keys)) (first opt-keys)))
          handler #(db/assoc-in path (value-next value))]
      [:a {:class "input" :on-click handler} text]))

(defn color-select []
  [toggle-link [:me] {:white "white" :black "black"}])

(defn opponent-select [] 
  [toggle-link [:them] {:ai "an AI" :player "another player" :me "yourself"}])

(defn- control-panel
  "Component defining the config/info panel that appears alongside the board."
  []
  [:div.panel
    [:h1 "Singularity Chess"]
    [:p "A Chess variant. As you can see, the board is quite unusual. "
        "This can have interesting effects on how pieces can move. "
        "Other than that, the variant has no special characteristics, "
        "and play proceeds identically to normal chess."]
    [:p "The current match is between you and " [opponent-select] ". "
        "You are the " [color-select] " player. "]])