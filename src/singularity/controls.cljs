(ns ^{:doc "Contains logic for making control panel and handling input therefrom."}
  singularity.controls
  (:require [singularity.db :as db]))

(defn- input-group
  "Component defining an input that is automatically mapped to
   given `atom`."
  ([atom] (input-group atom "text"))
  ([atom type] (input-group atom type identity))
  ([atom type converter]
   (let [handler #(reset! atom (converter (-> % .-target .-value)))]
     [:input {:type type :value @atom :on-change handler}])))

(defn- toggle-link
  "Component defining an 'link' that actually updates `atom`.
   `converter` must be a function which returns a string when given the
   value in the db."
  [atom options]
  (let [opt-keys (keys options)
        current-value @atom
        display-text (get options current-value)
        value-next (zipmap opt-keys (conj (vec (rest opt-keys)) (first opt-keys)))
        handler #(reset! atom (value-next current-value))]
    [:a {:class "input" :on-click handler} display-text]))

(defn color-select []
  [toggle-link (db/me) {:white "white" :black "black"}])

(defn opponent-select [] 
  [toggle-link (db/them) {:ai "an AI" :player "another player" :me "yourself"}])

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