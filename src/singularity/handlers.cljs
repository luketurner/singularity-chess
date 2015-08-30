(ns singularity.handlers
  (:require [singularity.db :as db]))

(defn- toggle-select!
  "Toggles selection of a given space"
  [coords]
  (db/update-in [:selected-space] #(if (= % coords) nil coords)))

(defn- valid-move?
  "Move validity predicate accepts 2 coords, returns bool"
  [s1 s2]
  (let [p1 (db/get-in [:board s1 :piece])
        p2 (db/get-in [:board s2 :piece])]
    (and
      (not (nil? p1))
      (nil? p2)
      (= (db/get :me) (:color p1)))))

(defn- move! [s1 s2]
  "Move function accepts 2 coords, moves from s1 to s2"
  (print "move" s1 s2)
  (db/assoc-in [:board s2 :piece] (db/get-in [:board s1 :piece]))
  (db/assoc-in [:board s1 :piece] nil))

(defn space
  [x y]
  "Event handler for when user clicks a space"
  (let [coords [x y]
        piece (db/get-in [:board coords :piece])
        selected (db/get :selected-space)]
    (if (or (nil? selected)
            (= selected coords)
            (not (valid-move? selected coords)))
      (toggle-select! coords)
      (do
        (move! selected coords)
        (toggle-select! nil)))))