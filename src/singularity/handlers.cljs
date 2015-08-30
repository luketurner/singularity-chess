(ns singularity.handlers
  (:require [singularity.db :as db]))

(defn- toggle-select
  "Toggles selection of a given space"
  [x y]
  (db/update-in [:selected-space] #(if (= % [x y]) nil [x y])))

(defn space
  [x y]
  "Event handler for when user clicks a space"
  (toggle-select x y))